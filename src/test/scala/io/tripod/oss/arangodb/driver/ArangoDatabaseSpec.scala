package io.tripod.oss.arangodb.driver

import akka.actor.ActorSystem
import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

class ArangoDatabaseSpec extends WordSpec with Matchers with ScalaFutures with EitherValues with IntegrationPatience {
  implicit val system = ActorSystem("ArangoDatabaseSpec")
  implicit val ec     = system.dispatcher

  "ArangoDatabase" should {
    "create/drop database" in {
      implicit val driver = ArangoDriver()
      val result          = ArangoDatabase.create("testDb").futureValue
      result match {
        case Right(database) ⇒
          database shouldBe a[ArangoDatabase]
          database.drop.futureValue shouldEqual (Right(true))
        case Left(error) ⇒ fail(error.toString)
      }
    }
  }
}
