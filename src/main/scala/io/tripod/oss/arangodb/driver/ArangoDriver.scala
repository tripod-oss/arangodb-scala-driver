package io.tripod.oss.arangodb.driver

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.routing._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.tripod.oss.arangodb.driver.RequestRouter.{
  AddEndpoint,
  GetEndPoints,
  RemoveEndpoint
}

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
class ArangoDriver(baseConfig: Config,
                   user: Option[String] = None,
                   password: Option[String] = None) {
  private val config = {
    val internalConfig =
      baseConfig.getConfig("arangodb-driver.internal-config")
    baseConfig
      .withoutPath("akka")
      .withFallback(internalConfig)
  }

  private val _userName =
    user.getOrElse(config.getString("arangodb-driver.auth.username"))
  private val _password =
    password.getOrElse(config.getString("arangodb-driver.auth.password"))

  implicit val system = ActorSystem("ArangoDriver", config)
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(5 seconds)

  protected val router =
    system.actorOf(Props(new RequestRouter(config, _userName, _password)),
                   "requestRouter")

  def addEndPoint(endPointRoot: String): Unit =
    router ! AddEndpoint(endPointRoot)

  def removeEndPoint(endPointRoot: String): Unit =
    router ! RemoveEndpoint(endPointRoot)

  def getEndPoints: Future[List[String]] =
    (router ? GetEndPoints).mapTo[List[String]]

  def close = system.terminate()

  def getServerVersion(
      withDetails: Boolean = false): Future[Either[ApiError, ApiResponse]] = {
    completeWithPromise[ServerVersionResponse](promise ⇒
      router ! GetServerVersion(withDetails, promise))
  }

  protected def completeWithPromise[T <: ApiResponse](
      request: Promise[Either[ApiError, T]] ⇒ Unit)
    : Future[Either[ApiError, T]] = {
    val responsePromise = Promise[Either[ApiError, T]]
    request(responsePromise)
    responsePromise.future
  }
}

object ArangoDriver {
  def apply(config: Config) = new ArangoDriver(config)
  def apply(username: String,
            password: String,
            config: Config = ConfigFactory.load()) =
    new ArangoDriver(config, Some(username), Some(password))
}
