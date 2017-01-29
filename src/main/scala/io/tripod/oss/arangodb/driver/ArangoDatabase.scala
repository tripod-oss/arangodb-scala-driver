package io.tripod.oss.arangodb.driver

import io.circe.Encoder
import io.tripod.oss.arangodb.driver.entities.{CollectionStatus, CollectionType}
import io.tripod.oss.arangodb.driver.http._

import scala.concurrent.Future

case class DatabaseInfo(name: String, id: String, path: String, isSystem: Boolean)

class ArangoDatabase(dbName: String)(implicit val driver: ArangoDriver) {
  implicit val dbContext = DBContext(dbName)
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
                       indexBuckets: Option[Int] = None): Future[Either[ApiError, ArangoCollection]] = {
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
      .map {
        case Right(response) ⇒ Right(ArangoCollection(this, response.name))
        case Left(apiError)  ⇒ Left(apiError)
      }
  }

  def collection(name: String): Future[Either[ApiError, ArangoCollection]] = {
    driver.getCollection(name).map {
      case Right(response) ⇒ Right(ArangoCollection(this, response.name))
      case Left(apiError)  ⇒ Left(apiError)
    }
  }

  def collections: Future[Either[ApiError, Seq[ArangoCollection]]] = {
    driver.getCollections.map {
      case Right(response) ⇒ Right(response.result.map(collection ⇒ ArangoCollection(this, collection.name)))
      case Left(apiError)  ⇒ Left(apiError)
    }
  }

  def getInfo = {
    driver.currentDatabase.map {
      case Right(response) ⇒
        DatabaseInfo(
          name = response.result.name,
          id = response.result.id,
          path = response.result.path,
          isSystem = response.result.isSystem
        )
      case Left(apiError) ⇒ Left(apiError)
    }
  }

  def drop = {
    driver.removeDatabase(dbName).map {
      case Right(response) ⇒ Right(response.result)
      case Left(apiError)  ⇒ Left(apiError)
    }
  }
}

object ArangoDatabase {
  implicit val noneExtraEncoder = Encoder.encodeNone

  def apply(dbName: String)(implicit driver: ArangoDriver) = new ArangoDatabase(dbName)

  def create[T](dbName: String, options: Option[Seq[UserCreateOptions[T]]] = None)(
      implicit driver: ArangoDriver,
      extraEncoder: Encoder[T]): Future[Either[ApiError, ArangoDatabase]] = {
    implicit val ec = driver.ec
    driver.createDatabase(dbName, options).map {
      case Right(response) ⇒ Right(ArangoDatabase(dbName)(driver))
      case Left(error)     ⇒ Left(error)
    }
  }
}
