package io.tripod.oss.arangodb.driver

import akka.actor.ActorSystem
import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.concurrent.Future

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
          database.drop.futureValue shouldEqual Right(true)
        case Left(error) ⇒ fail(error.toString)
      }
    }

    "get info" in {
      implicit val driver = ArangoDriver()
      ArangoDatabase.create("testDbInfo").flatMap {
        case Right(database) ⇒
          database.info.map {
            case Right(info) ⇒
              info shouldBe a[DatabaseInfo]
              ArangoDatabase("testDbInfo").drop.futureValue
            case Left(error) ⇒ fail(error.toString)
          }
        case Left(error) ⇒ fail(error.toString)
      }
    }
    "create collection" in {
      implicit val driver = ArangoDriver()
      val result = ArangoDatabase
        .create("testDb")
        .flatMap {
          case Right(database) ⇒ database.createCollection("testCollection")
          case Left(error)     ⇒ fail(error.toString)

        }
        .futureValue

      result match {
        case Right(collection) ⇒
          collection shouldBe a[ArangoCollection]
          ArangoDatabase("testDb").drop.futureValue shouldEqual Right(true)
        case Left(error) ⇒ fail(error.toString)
      }
    }
    "get collection" in {
      implicit val driver = ArangoDriver()
      val result = ArangoDatabase
        .create("testDb")
        .flatMap {
          case Right(database) ⇒ database.createCollection("testCollection")
          case Left(error)     ⇒ fail(error.toString)

        }
        .futureValue

      result match {
        case Right(_) ⇒
          ArangoDatabase("testDb")
            .collection("testCollection")
            .futureValue shouldBe a[Right[_, ArangoCollection]]
          ArangoDatabase("testDb").drop.futureValue shouldEqual Right(true)
        case Left(error) ⇒ fail(error.toString)
      }
    }
    "get collections" in {
      implicit val driver = ArangoDriver()
      val result = ArangoDatabase
        .create("testDb")
        .flatMap {
          case Right(database) ⇒ database.createCollection("testCollection")
          case Left(error)     ⇒ fail(error.toString)

        }
        .futureValue

      result match {
        case Right(_) ⇒
          ArangoDatabase("testDb").collections.futureValue shouldBe a[Right[_, Seq[ArangoCollection]]]
          ArangoDatabase("testDb").drop.futureValue shouldEqual Right(true)
        case Left(error) ⇒ fail(error.toString)
      }
    }
  }
}
