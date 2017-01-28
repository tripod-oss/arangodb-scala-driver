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
import io.circe.{Decoder, DecodingFailure, Encoder}
import io.tripod.oss.arangodb.driver.ApiError
import io.tripod.oss.arangodb.driver.http.EndpointClientWorker.{Authenticated, Enqueue}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

case class DBContext(name: String)

case class ApiCall[Q <: ApiRequest, R <: ApiResponse](dbContext: Option[DBContext],
                                                      apiMethod: HttpMethod,
                                                      apiUri: String,
                                                      apiHeaders: List[HttpHeader] = List.empty,
                                                      request: Option[Q],
                                                      encoder: Option[Encoder[Q]],
                                                      decoder: Decoder[R],
                                                      responsePromise: Promise[Either[ApiError, R]])

object EndpointClientWorker {
  def props(endPointRoot: String, driverConfig: Config, userName: String, password: String): Props =
    Props(new EndpointClientWorker(endPointRoot, driverConfig, userName, password))
  case class Authenticated(token: String)
  case class Enqueue[Q <: ApiRequest, R <: ApiResponse](request: HttpRequest, apiCall: ApiCall[Q, R])
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
        case Success(authResponse) ⇒
          authResponse.fold({ error =>
            logger.error(s"Authentication failed: $error")
            e.responsePromise.complete(Success(Left(error)))
          }, resp ⇒ self ! Authenticated(resp.jwt))
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
      logger.debug(s"--(request)-> $request")
      httpRequestQueue.offer((request, apiCall))
    case (Success(response: HttpResponse), apiCall: ApiCall[_, ApiResponse]) =>
      logger.debug(s"<-(response)- $response")
      apiCall.responsePromise.completeWith(toApiResponse(response, apiCall.decoder))
  }

  private def authenticate(implicit system: ActorSystem): Future[Either[ApiError, AuthResponse]] = {
    import io.circe.generic.semiauto._
    for {
      entity   ← Marshal(AuthRequest(userName, password)).to[RequestEntity]
      request  ← Http().singleRequest(HttpRequest(POST, s"$endPointRoot/_open/auth", entity = entity))
      response ← toApiResponse(request, deriveDecoder[AuthResponse])
    } yield response
  }

  def enqueue[Q <: ApiRequest, R <: ApiResponse](apiCall: ApiCall[Q, R]) = {
    val requestEntity = apiCall.request.map { req ⇒
      implicit val encoder = apiCall.encoder.get
      Marshal[Q](req).to[RequestEntity]
    }
    val requestFuture = requestEntity match {
      case Some(entityFuture) ⇒
        entityFuture.map(entity ⇒
          buildHttpRequest(apiCall.apiMethod, apiCall.apiUri, apiCall.apiHeaders).withEntity(entity))
      case None ⇒
        Future.successful(buildHttpRequest(apiCall.apiMethod, apiCall.apiUri, apiCall.apiHeaders))
    }
    requestFuture.map(httpRequest ⇒ self ! Enqueue(httpRequest, apiCall))
  }

  private def toApiResponse[R <: ApiResponse](httpResponse: HttpResponse, entityDecoder: Decoder[R]) = {
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
    }
  }

  def buildHttpRequest(method: HttpMethod, uri: String, headers: List[HttpHeader] = List.empty): HttpRequest = {
    val request = HttpRequest(method, s"$endPointRoot$uri")
    if (jwtToken.isDefined)
      request.withHeaders(List(Authorization(GenericHttpCredentials("bearer", jwtToken.get))) ++ headers)
    else
      request
  }

  def buildUri(uri: String, dbContext: Option[DBContext]) = dbContext match {
    case Some(db) ⇒ s"/_db/${db.name}/$uri"
    case None     ⇒ uri
  }
}
