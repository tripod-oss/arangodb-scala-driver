package io.tripod.oss.arangodb.driver.examples

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.tripod.oss.arangodb.driver.{ArangoDriver, ServerVersionResponse}
import io.tripod.oss.arangodb.driver.database.DatabaseApi
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
class ArangoDriverSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience {
  implicit val system = ActorSystem("PbfStreamFlowSpec")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  "ArangodbDriver" should {
    "get server version" in {
      val driver = new ArangoDriver() with DatabaseApi
      val result = driver.getServerVersion(true).futureValue
      result.isRight shouldBe true
      result.map(response => response.server shouldEqual "arango")
    }
  }
}