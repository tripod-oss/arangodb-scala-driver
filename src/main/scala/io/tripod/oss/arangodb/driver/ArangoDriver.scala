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

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
class ArangoDriver(baseConfig: Config) {
  private val config = {
    val internalConfig =
      baseConfig.getConfig("arangodb-driver.internal-config")
    baseConfig.withoutPath("akka").withFallback(internalConfig)
  }

  implicit val system = ActorSystem("ArangoDriver", config)
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(5 seconds)
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system))

  private val router =
    system.actorOf(Props[RequestRouter], "requestRouter")

  def addEndPoint(endPointRoot: String): Unit =
    router ! AddEndpoint(endPointRoot)

  def removeEndPoint(endPointRoot: String): Unit =
    router ! RemoveEndpoint(endPointRoot)

  def getEndPoints: Future[List[String]] =
    (router ? GetEndPoints).mapTo[List[String]]

  def close = system.terminate()
}

object ArangoDriver {
  def apply(config: Config = ConfigFactory.load()) = new ArangoDriver(config)
}
