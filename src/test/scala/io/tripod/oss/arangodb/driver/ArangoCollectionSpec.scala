package io.tripod.oss.arangodb.driver

import akka.actor.ActorSystem
import io.tripod.oss.arangodb.driver.entities.{CollectionInfo, CollectionStatus, CollectionType}
import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

/**
  * Created by nicolas.jouanin on 01/02/17.
  */
class ArangoCollectionSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with EitherValues
    with IntegrationPatience {
  implicit val system = ActorSystem("ArangoDatabaseSpec")
  implicit val ec     = system.dispatcher

  "ArangoCollection" should {
    "get collection info " in {
      implicit val driver = ArangoDriver()
      val future = for {
        database   <- ArangoDatabase.create("testDb")
        collection ← database.createCollection("testCollection")
        info       <- collection.info
        _          ← database.drop
      } yield info

      val result = future.futureValue
      result shouldBe a[CollectionInfo]
      result.status shouldEqual CollectionStatus.Loaded
      result.`type` shouldEqual CollectionType.DocumentCollection
    }
  }
}
