package io.tripod.oss.arangodb.driver.database.driver

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.{Decoder, Encoder}
import io.tripod.oss.arangodb.driver.RequestRouter.{
  AddEndpoint,
  GetEndPoints,
  RemoveEndpoint
}
import io.tripod.oss.arangodb.driver._
import io.tripod.oss.arangodb.driver.utils.FutureUtils._

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import io.circe._, io.circe.generic.semiauto._

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
class ArangoDriver(baseConfig: Config = ConfigFactory.load(),
                   user: Option[String] = None,
                   password: Option[String] = None) {
  private val config = {
    val internalConfig =
      baseConfig.getConfig("arangodb-driver.internal-config")
    baseConfig.withoutPath("akka").withFallback(internalConfig)
  }

  private val _userName =
    user.getOrElse(config.getString("arangodb-driver.auth.username"))
  private val _password =
    password.getOrElse(config.getString("arangodb-driver.auth.password"))

  implicit val system = ActorSystem("ArangoDriver", config)
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(5 seconds)

  private[database] val router =
    system.actorOf(Props(new RequestRouter(config, _userName, _password)),
                   "requestRouter")

  // Auto add endpoints from configuration
  config
    .getStringList("arangodb-driver.endpoints")
    .forEach(endpoint => addEndPoint(endpoint))

  def addEndPoint(endPointRoot: String): Unit =
    router ! AddEndpoint(endPointRoot)

  def removeEndPoint(endPointRoot: String): Unit =
    router ! RemoveEndpoint(endPointRoot)

  def getEndPoints: Future[List[String]] =
    (router ? GetEndPoints).mapTo[List[String]]

  def close = system.terminate()

  def getServerVersion(withDetails: Boolean = false)(
      implicit dbContext: Option[DBContext] = None)
    : Future[Either[ApiError, ServerVersionResponse]] = {
    implicit val encoder = Some(deriveEncoder[ServerVersionRequest])
    implicit val decoder = deriveDecoder[ServerVersionResponse]
    callApi(dbContext, HttpMethods.GET, s"/_api/version?details=$withDetails")
  }

  def callApi[Q <: ApiRequest, R <: ApiResponse](dbContext: Option[DBContext],
                                                 apiMethod: HttpMethod,
                                                 apiUri: String,
                                                 request: Option[Q] = None)(
      implicit requestEncoder: Option[Encoder[Q]],
      responseDecoder: Decoder[R]): Future[Either[ApiError, R]] = {
    val responsePromise = Promise[Either[ApiError, R]]
    router ! ApiCall(dbContext,
                     apiMethod,
                     apiUri,
                     request,
                     requestEncoder,
                     responseDecoder,
                     responsePromise)
    responsePromise.future
  }

//  def db(dbContext: String) = new ArangoDatabase(dbContext, self)
}

object ArangoDriver {
  def apply() = new ArangoDriver()
  def apply(config: Config) = new ArangoDriver(config)
  def apply(username: String,
            password: String,
            config: Config = ConfigFactory.load()) =
    new ArangoDriver(config, Some(username), Some(password))
}
