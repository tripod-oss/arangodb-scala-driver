package io.tripod.oss.arangodb.driver

import io.circe.Encoder
import io.tripod.oss.arangodb.driver.entities.{CollectionStatus, CollectionType}
import io.tripod.oss.arangodb.driver.http._

import scala.concurrent.Future

case class DatabaseInfo(name: String, id: String, path: String, isSystem: Boolean)

class ArangoDatabase(dbName: String)(implicit val driver: ArangoDriver) {
  implicit val dbContext = Some(DBContext(dbName))
  implicit val ec        = driver.ec

  def createCollection(name: String,
                       journalSize: Option[Int] = None,
                       replicationFactor: Option[Int] = None,
                       keyOptions: Option[CollectionKeyOptions] = None,
                       waitForSync: Option[Boolean] = None,
                       doCompact: Option[Boolean] = None,
                       isVolatile: Option[Boolean] = None,
                       shardKeys: Option[String] = None,
                       numberOfShards: Option[Int] = None,
                       isSystem: Option[Boolean] = None,
                       `type`: Option[CollectionType] = None,
                       indexBuckets: Option[Int] = None): Future[ArangoCollection] = {
    driver
      .createCollection(name,
                        journalSize,
                        replicationFactor,
                        keyOptions,
                        waitForSync,
                        doCompact,
                        isVolatile,
                        shardKeys,
                        numberOfShards,
                        isSystem,
                        `type`,
                        indexBuckets)
      .map(response ⇒ ArangoCollection(this, response.name))
  }

  def collection(name: String): Future[ArangoCollection] = {
    driver.getCollection(name).map(response ⇒ ArangoCollection(this, response.name))
  }

  def collections: Future[Seq[ArangoCollection]] = {
    driver.getCollections.map(response ⇒ response.result.map(collection ⇒ ArangoCollection(this, collection.name)))
  }

  def info: Future[DatabaseInfo] = {
    driver.currentDatabase.map(
      response ⇒
        DatabaseInfo(
          name = response.result.name,
          id = response.result.id,
          path = response.result.path,
          isSystem = response.result.isSystem
      ))
  }

  def drop: Future[Unit] = driver.removeDatabase(dbName).map(_ ⇒ ())
}

object ArangoDatabase {
  implicit val noneExtraEncoder = Encoder.encodeNone

  def apply(dbName: String)(implicit driver: ArangoDriver) = new ArangoDatabase(dbName)

  def create[T](dbName: String, options: Option[Seq[UserCreateOptions[T]]] = None)(
      implicit driver: ArangoDriver,
      extraEncoder: Encoder[T]): Future[ArangoDatabase] = {
    implicit val ec = driver.ec
    driver.createDatabase(dbName, options).map(_ ⇒ ArangoDatabase(dbName)(driver))
  }
}
