package io.tripod.oss.arangodb.driver.http

import akka.actor.{Actor, ActorSystem, Kill, Props, Stash}
import akka.http.javadsl.model.headers.WWWAuthenticate
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{Authorization, GenericHttpCredentials}
import akka.http.scaladsl.model.{RequestEntity, _}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.{Decoder, DecodingFailure, Encoder}
import io.tripod.oss.arangodb.driver.{ApiError, ApiException}
import io.tripod.oss.arangodb.driver.http.EndpointClientWorker.{Authenticated, Enqueue}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

case class DBContext(name: String)

case class ApiCall[Q, R <: ApiResponse](dbContext: Option[DBContext],
                                        apiMethod: HttpMethod,
                                        apiUri: String,
                                        apiHeaders: List[HttpHeader] = List.empty,
                                        request: Option[Q],
                                        encoder: Option[Encoder[Q]],
                                        decoder: Decoder[R],
                                        responsePromise: Promise[R])

object EndpointClientWorker {
  def props(endPointRoot: String, driverConfig: Config, userName: String, password: String): Props =
    Props(new EndpointClientWorker(endPointRoot, driverConfig, userName, password))
  case class Authenticated(token: String)
  case class Enqueue[Q, R <: ApiResponse](request: HttpRequest, apiCall: ApiCall[Q, R])
}

class EndpointClientWorker(endPointRoot: String, driverConfig: Config, userName: String, password: String)
    extends Actor
    with Stash
    with LazyLogging {
  import de.heikoseeberger.akkahttpcirce.CirceSupport._

  case class AuthRequest(username: String, password: String)

  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  implicit val ec           = context.dispatcher
  implicit val system       = context.system

  private val httpPool =
    Http(context.system).superPool[ApiCall[_, _]](settings = ConnectionPoolSettings(driverConfig))

  private val httpRequestQueue =
    Source.queue(10, OverflowStrategy.backpressure).via(httpPool).toMat(Sink.actorRef(self, Kill))(Keep.left).run

  private var jwtToken: Option[String] = None

  def receive = {
    if (userName.trim.equals("")) {
      //Assume authentification is not required
      authenticatedBehaviour
    } else {
      authenticationRequiredBehaviour
    }
  }

  def authenticationRequiredBehaviour: Receive = {
    case e: ApiCall[_, _] if jwtToken.isEmpty ⇒
      stash()
      authenticate.andThen {
        case Success(authResponse) ⇒ self ! Authenticated(authResponse.jwt)
        case Failure(t) ⇒
          logger.error(s"Authentication request failed: ${t.getMessage}")
          logger.debug("cause", t)
          e.responsePromise.failure(t)
      }
    case Authenticated(token) ⇒
      logger.debug("Authentication successful")
      jwtToken = Some(token)
      unstashAll()
      context.become(authenticatedBehaviour)
  }

  def authenticatedBehaviour: Receive = {
    defaultBehaviour
  }

  def defaultBehaviour: Receive = {
    case apiCall: ApiCall[_, _] ⇒ enqueue(apiCall)
    case Enqueue(request, apiCall) ⇒
      logger.trace(s"--(request)-> $request")
      httpRequestQueue.offer((request, apiCall))
    case (Success(response: HttpResponse), apiCall: ApiCall[_, ApiResponse]) =>
      logger.trace(s"<-(response)- $response")
      apiCall.responsePromise.completeWith(toApiResponse(response, apiCall.decoder))
  }

  private def authenticate(): Future[AuthResponse] = {
    import io.circe.generic.semiauto._
    for {
      entity   ← Marshal(AuthRequest(userName, password)).to[RequestEntity]
      request  ← Http().singleRequest(HttpRequest(POST, s"$endPointRoot/_open/auth", entity = entity))
      response ← toApiResponse(request, deriveDecoder[AuthResponse])
    } yield response
  }

  def enqueue[Q, R <: ApiResponse](apiCall: ApiCall[Q, R]) = {
    val requestEntity = apiCall.request.map { req ⇒
      implicit val encoder = apiCall.encoder.get
      Marshal[Q](req).to[RequestEntity]
    }
    val requestFuture = requestEntity match {
      case Some(entityFuture) ⇒
        entityFuture.map(entity ⇒
          buildHttpRequest(apiCall.apiMethod, buildUri(apiCall), apiCall.apiHeaders).withEntity(entity))
      case None ⇒
        Future.successful(buildHttpRequest(apiCall.apiMethod, buildUri(apiCall), apiCall.apiHeaders))
    }
    requestFuture.map(httpRequest ⇒ self ! Enqueue(httpRequest, apiCall))
  }

  private def toApiResponse[R <: ApiResponse](httpResponse: HttpResponse, entityDecoder: Decoder[R]): Future[R] = {
    implicit val decoder = entityDecoder
    Unmarshal(httpResponse.entity).to[R].recoverWith {
      case _ ⇒
        implicit val apiErrorDecoder = deriveDecoder[ApiError]
        Unmarshal(httpResponse.entity)
          .to[ApiError]
          .recoverWith {
            case t: Throwable ⇒
              val unAuth = httpResponse.status match {
                case StatusCodes.Unauthorized ⇒ httpResponse.header[WWWAuthenticate].map(_.value())
                case _                        ⇒ None
              }
              unAuth.map { message ⇒
                Future.successful(
                  new ApiError(error = true,
                               code = httpResponse.status.intValue,
                               errorNum = httpResponse.status.intValue,
                               errorMessage = message))
              }.getOrElse {
                Unmarshaller
                  .stringUnmarshaller(httpResponse.entity)
                  .map(
                    body =>
                      new ApiError(error = true,
                                   code = httpResponse.status.intValue,
                                   errorNum = httpResponse.status.intValue,
                                   errorMessage = s"${t.getLocalizedMessage}: $body"))
              }
          }
          .map(apiError ⇒
            throw new ApiException(apiError.error, apiError.code, apiError.errorNum, apiError.errorMessage))
    }
    /*
    if (httpResponse.status.isFailure()) {
      val errorMessage: String = httpResponse.status match {
        case StatusCodes.Unauthorized ⇒
          httpResponse.header[WWWAuthenticate].map(_.value()).getOrElse("")
        case _ ⇒ "Undefined error"
      }
      Unmarshaller
        .stringUnmarshaller(httpResponse.entity)
        .map(errorBody => Left(ApiError(httpResponse.status.intValue, errorMessage, errorBody)))
    } else {
      implicit val decoder = entityDecoder
      Unmarshal(httpResponse.entity).to[R].map(result => Right(result)).recover {
        case f: DecodingFailure ⇒
          val errorBody = Await.result(Unmarshal(httpResponse.entity).to[String], 30 seconds).asInstanceOf[String]
          Left(ApiError(httpResponse.status.intValue, f.getMessage, errorBody))
      }
    }*/
  }

  def buildHttpRequest(method: HttpMethod, uri: String, headers: List[HttpHeader] = List.empty): HttpRequest = {
    val request = HttpRequest(method, s"$endPointRoot$uri")
    if (jwtToken.isDefined)
      request.withHeaders(List(Authorization(GenericHttpCredentials("bearer", jwtToken.get))) ++ headers)
    else
      request
  }

  def buildUri(apiCall: ApiCall[_, _]) = apiCall.dbContext match {
    case Some(db) ⇒ s"/_db/${db.name}/${apiCall.apiUri}".replaceAll("//", "/")
    case None     ⇒ apiCall.apiUri
  }
}
