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

object EndpointClientWorker {
  def props(endPointRoot: String,
            driverConfig: Config,
            userName: String,
            password: String): Props =
    Props(
      new EndpointClientWorker(endPointRoot, driverConfig, userName, password))
  case class Authenticated(token: String)
  case class Enqueue(request: HttpRequest,
                     promise: Promise[Either[Error, ApiResponse]])
}

class EndpointClientWorker(endPointRoot: String,
                           driverConfig: Config,
                           userName: String,
                           password: String)
    extends Actor
    with Stash
    with LazyLogging {

  case class AuthRequest(username: String, password: String)

  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(context.system))
  implicit val ec = context.dispatcher
  implicit val system = context.system

  private val httpPool =
    Http(context.system)
      .superPool[Promise[Either[Error, ApiResponse]]](
        settings = ConnectionPoolSettings(driverConfig))

  private val httpRequestQueue =
    Source
      .queue(10, OverflowStrategy.backpressure)
      .via(httpPool)
      .toMat(Sink.actorRef(self, Kill))(Keep.left)
      .run

  private var jwtToken: Option[String] = None

  def receive = {
    case e: WorkMessage if jwtToken.isEmpty ⇒
      stash()
      authenticate.andThen {
        case Success(authResponse) ⇒
          authResponse.fold({ error =>
            logger.error(s"Authentication failed: $error")
            e.resultPromise.complete(Success(Left(error)))
          }, resp ⇒ self ! Authenticated(resp.jwt))
        case Failure(t) ⇒
          logger.error(s"Authentication failed: ${t.getMessage}")
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
    case GetServerVersion(withDetails, promise) =>
      self ! Enqueue(buildHttpRequest(s"/_api/version?details=$withDetails"),
                     promise)
    case Enqueue(request, promise) ⇒
      logger.debug(s"--(request)-> $request")
      httpRequestQueue.offer((request, promise))
    case (Success(response: HttpResponse),
          promise: Promise[Either[Error, ApiResponse]]) =>
      import de.heikoseeberger.akkahttpcirce.CirceSupport._
      logger.debug(s"<-(response)- $response")
      promise.completeWith(toApiResult[ServerVersionResponse](response))
  }

  private def authenticate(
      implicit system: ActorSystem): Future[Either[Error, AuthResponse]] = {
    import de.heikoseeberger.akkahttpcirce.CirceSupport._
    for {
      entity ← Marshal(AuthRequest(userName, password)).to[RequestEntity]
      request ← Http().singleRequest(
        HttpRequest(POST, s"$endPointRoot/_open/auth", entity = entity))
      response ← toApiResult[AuthResponse](request)
    } yield response
  }

  private def toApiResult[T <: ApiResponse](httpResponse: HttpResponse)(
      implicit um: Unmarshaller[ResponseEntity, T],
      materializer: ActorMaterializer): Future[Either[Error, T]] = {
    if (httpResponse.status.isFailure()) {
      val errorMessage = httpResponse.status match {
        case StatusCodes.Unauthorized ⇒
          httpResponse.header[WWWAuthenticate].map(_.value()).getOrElse("")
        case _ ⇒ "Undefined error"
      }
      Unmarshal(httpResponse.entity)
        .to[String]
        .map(errorBody =>
          Left(Error(httpResponse.status.intValue, errorMessage, errorBody)))
    } else {
      Unmarshal(httpResponse.entity)
        .to[T](um, ec, materializer)
        .map(result => Right(result))
        .recover {
          case f: DecodingFailure ⇒
            val errorBody = Await
              .result(Unmarshal(httpResponse.entity).to[String], 30 seconds)
              .asInstanceOf[String]
            Left(Error(httpResponse.status.intValue, f.getMessage, errorBody))
        }
    }
  }

  private def buildHttpRequest(uri: String): HttpRequest = {
    val request = HttpRequest(GET, s"$endPointRoot$uri")
    if (jwtToken.isDefined)
      request.withHeaders(
        List(Authorization(GenericHttpCredentials("bearer", jwtToken.get))))
    else
      request
  }
}
