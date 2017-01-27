package io.tripod.oss.arangodb.driver.http

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.{Decoder, Encoder}
import RequestRouter.{AddEndpoint, GetEndPoints, RemoveEndpoint}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
class ArangoDriver(baseConfig: Config = ConfigFactory.load(),
                   user: Option[String] = None,
                   password: Option[String] = None)
    extends CodecsImplicits
    with MiscApi
    with DatabaseApi
    with CollectionApi {

  private val config = {
    val internalConfig =
      baseConfig.getConfig("arangodb-driver.internal-config")
    baseConfig.withoutPath("akka").withFallback(internalConfig)
  }

  private val _userName =
    user.getOrElse(config.getString("arangodb-driver.auth.username"))
  private val _password =
    password.getOrElse(config.getString("arangodb-driver.auth.password"))

  implicit val system  = ActorSystem("ArangoDriver", config)
  implicit val ec      = system.dispatcher
  implicit val timeout = Timeout(5 seconds)

  private[http] val router =
    system.actorOf(Props(new RequestRouter(config, _userName, _password)), "requestRouter")

  // Auto add endpoints from configuration
  config.getStringList("arangodb-driver.endpoints").forEach(endpoint => addEndPoint(endpoint))

  def addEndPoint(endPointRoot: String): Unit =
    router ! AddEndpoint(endPointRoot)

  def removeEndPoint(endPointRoot: String): Unit =
    router ! RemoveEndpoint(endPointRoot)

  def getEndPoints: Future[List[String]] =
    (router ? GetEndPoints).mapTo[List[String]]

  def close = system.terminate()

  def callApi[R <: ApiResponse](dbContext: Option[DBContext], apiMethod: HttpMethod, apiUri: String)(
      implicit responseDecoder: Decoder[R]): Future[Either[ApiError, R]] = {
    val responsePromise = Promise[Either[ApiError, R]]
    router ! ApiCall(dbContext, apiMethod, apiUri, None, None, responseDecoder, responsePromise)
    responsePromise.future
  }

  def callApi[Q <: ApiRequest, R <: ApiResponse](
      dbContext: Option[DBContext],
      apiMethod: HttpMethod,
      apiUri: String,
      request: Q)(implicit requestEncoder: Encoder[Q], responseDecoder: Decoder[R]): Future[Either[ApiError, R]] = {
    val responsePromise = Promise[Either[ApiError, R]]
    router ! ApiCall(dbContext,
                     apiMethod,
                     apiUri,
                     Some(request),
                     Some(requestEncoder),
                     responseDecoder,
                     responsePromise)
    responsePromise.future
  }
}

object ArangoDriver {
  def apply()               = new ArangoDriver()
  def apply(config: Config) = new ArangoDriver(config)
  def apply(username: String, password: String, config: Config = ConfigFactory.load()) =
    new ArangoDriver(config, Some(username), Some(password))
}
