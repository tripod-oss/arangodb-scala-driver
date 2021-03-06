package io.tripod.oss.arangodb.driver

import akka.actor.ActorSystem
import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ArangoDatabaseSpec extends WordSpec with Matchers with ScalaFutures with EitherValues with IntegrationPatience {
  implicit val system = ActorSystem("ArangoDatabaseSpec")
  implicit val ec     = system.dispatcher

  "ArangoDatabase" should {
    "create/drop database" in {
      implicit val driver = ArangoDriver()
      val future          = ArangoDatabase.create("testDbCreate")
      future.onComplete {
        case Success(result) =>
          result shouldBe a[ArangoDatabase]
          result.drop.futureValue shouldEqual ()
        case Failure(t: ApiException) =>
          fail(t)
      }
      future.futureValue
    }

    "get info" in {
      implicit val driver = ArangoDriver()
      val future = for {
        database ← ArangoDatabase.create("testDbInfo")
        info     ← database.info
        _        ← database.drop
      } yield info

      val result = future.futureValue shouldBe a[DatabaseInfo]
    }

    "create collection" in {
      implicit val driver = ArangoDriver()
      val future = for {
        database   <- ArangoDatabase.create("testDb")
        collection ← database.createCollection("testCollection")
        _          ← database.drop
      } yield collection

      future.futureValue shouldBe a[ArangoCollection]
    }

    "get collection" in {
      implicit val driver = ArangoDriver()

      val future = for {
        database   <- ArangoDatabase.create("testDb")
        _          ← database.createCollection("testCollection")
        collection ← database.collection("testCollection")
        _          ← database.drop
      } yield collection

      future.futureValue shouldBe a[ArangoCollection]
    }
    "get collections" in {
      implicit val driver = ArangoDriver()

      val future = for {
        database    <- ArangoDatabase.create("testDb")
        _           ← database.createCollection("testCollection")
        collections ← database.collections
        _           ← database.drop
      } yield collections

      future.futureValue shouldBe a[Seq[_]]
    }
  }
}
