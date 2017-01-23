package io.tripod.oss.arangodb.driver

import akka.actor.{Actor, ActorSystem, Kill, Props, Stash}
import akka.http.javadsl.model.headers.WWWAuthenticate
import akka.http.scaladsl.model.headers.{Authorization, GenericHttpCredentials}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{RequestEntity, _}
import akka.stream.{
  ActorMaterializer,
  ActorMaterializerSettings,
  OverflowStrategy
}
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.typesafe.config.Config
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import com.typesafe.scalalogging.LazyLogging
import io.circe.DecodingFailure

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import io.tripod.oss.arangodb.driver.EndpointClientWorker.{
  Authenticated,
  Enqueue
}
import io.circe.generic.auto._
import io.tripod.oss.arangodb.driver.database.DatabaseWorkerBehaviour

case class RequestContext[T <: ApiResponse](
    resultPromise: Promise[Either[ApiError, T]],
    responseParser: HttpEntity ⇒ Future[Either[ApiError, T]])

object EndpointClientWorker {
  def props(endPointRoot: String,
            driverConfig: Config,
            userName: String,
            password: String): Props =
    Props(
      new EndpointClientWorker(endPointRoot, driverConfig, userName, password))
  case class Authenticated(token: String)
  case class Enqueue[T <: ApiResponse](request: HttpRequest,
                                       context: RequestContext[T])
}

class EndpointClientWorker(endPointRoot: String,
                           driverConfig: Config,
                           userName: String,
                           password: String)
    extends Actor
    with Stash
    with DatabaseWorkerBehaviour
    with LazyLogging {
  import de.heikoseeberger.akkahttpcirce.CirceSupport._

  case class AuthRequest(username: String, password: String)

  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(context.system))
  implicit val ec = context.dispatcher
  implicit val system = context.system

  private val httpPool =
    Http(context.system).superPool[RequestContext[_]](
      settings = ConnectionPoolSettings(driverConfig))

  private val httpRequestQueue =
    Source
      .queue(10, OverflowStrategy.backpressure)
      .via(httpPool)
      .toMat(Sink.actorRef(self, Kill))(Keep.left)
      .run

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
    case e: WorkMessage[_] if jwtToken.isEmpty ⇒
      stash()
      authenticate.andThen {
        case Success(authResponse) ⇒
          authResponse.fold({ error =>
            logger.error(s"Authentication failed: $error")
            e.resultPromise.complete(Success(Left(error)))
          }, resp ⇒ self ! Authenticated(resp.jwt))
        case Failure(t) ⇒
          logger.error(s"Authentication request failed: ${t.getMessage}")
          logger.debug("cause", t)
          e.resultPromise.failure(t)

      }
    case Authenticated(token) ⇒
      logger.debug("Authentication successful")
      jwtToken = Some(token)
      unstashAll()
      context.become(authenticatedBehaviour)
  }

  def authenticatedBehaviour: Receive = {
    databaseWorkerBehaviour orElse defaultBehaviour
  }

  def defaultBehaviour: Receive = {
    case GetServerVersion(withDetails, promise) =>
      enqueue(HttpMethods.GET, s"/_api/version?details=$withDetails", promise)
    case Enqueue(request, context) ⇒
      logger.trace(s"--(request)-> $request")
      httpRequestQueue.offer((request, context))
    case (Success(response: HttpResponse),
          context: RequestContext[ApiResponse]) =>
      logger.trace(s"<-(response)- $response")
      context.resultPromise.completeWith(
        toApiResponse(response, context.responseParser))
  }

  private def authenticate(
      implicit system: ActorSystem): Future[Either[ApiError, AuthResponse]] = {
//    import de.heikoseeberger.akkahttpcirce.CirceSupport._
    for {
      entity ← Marshal(AuthRequest(userName, password)).to[RequestEntity]
      request ← Http().singleRequest(
        HttpRequest(POST, s"$endPointRoot/_open/auth", entity = entity))
      response ← toApiResponse(
        request,
        entity ⇒
          Unmarshal(entity).to[AuthResponse].map(result => Right(result)))
    } yield response
  }

  def enqueue[R <: ApiResponse](method: HttpMethod,
                                uri: String,
                                promise: Promise[Either[ApiError, R]],
                                requestEntity: Option[RequestEntity] = None)(
      implicit um: Unmarshaller[HttpEntity, R]) = {
    val request =
      if (requestEntity.isDefined)
        buildHttpRequest(method, uri).withEntity(requestEntity.get)
      else
        buildHttpRequest(method, uri)
    self ! Enqueue(
      request,
      RequestContext[R](
        promise.asInstanceOf[Promise[Either[ApiError, R]]],
        entity ⇒ Unmarshal(entity).to[R].map(result => Right(result))
      )
    )
  }

  private def toApiResponse[T <: ApiResponse](
      httpResponse: HttpResponse,
      parser: HttpEntity ⇒ Future[Either[ApiError, T]])
    : Future[Either[ApiError, T]] = {
    if (httpResponse.status.isFailure()) {
      val errorMessage: String = httpResponse.status match {
        case StatusCodes.Unauthorized ⇒
          httpResponse.header[WWWAuthenticate].map(_.value()).getOrElse("")
        case _ ⇒ "Undefined error"
      }
      Unmarshaller
        .stringUnmarshaller(httpResponse.entity)
        .map(
          errorBody =>
            Left(
              ApiError(httpResponse.status.intValue, errorMessage, errorBody)))
    } else {
      parser(httpResponse.entity).recover {
        case f: DecodingFailure ⇒
          val errorBody = Await
            .result(Unmarshal(httpResponse.entity).to[String], 30 seconds)
            .asInstanceOf[String]
          Left(ApiError(httpResponse.status.intValue, f.getMessage, errorBody))
      }
    }
  }

  def buildHttpRequest(method: HttpMethod, uri: String): HttpRequest = {
    val request = HttpRequest(method, s"$endPointRoot$uri")
    if (jwtToken.isDefined)
      request.withHeaders(
        List(Authorization(GenericHttpCredentials("bearer", jwtToken.get))))
    else
      request
  }
}
