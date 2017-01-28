package io.tripod.oss.arangodb.driver

import io.tripod.oss.arangodb.driver.entities.{CollectionStatus, CollectionType}

import scala.concurrent.Future

case class CollectionInfo(id: String,
                          name: String,
                          waitForSync: Boolean,
                          journalSize: Int,
                          isVolatile: Boolean,
                          isSystem: Boolean,
                          status: CollectionStatus,
                          `type`: CollectionType)

class ArangoCollection(db: ArangoDatabase, name: String)(implicit val driver: ArangoDriver) {
  implicit val dbContext = db.dbContext
  implicit val ec        = driver.ec

  def count: Future[Either[ApiError, Int]] = {
    driver.getCollectionCount(name).map {
      case Right(response) ⇒ Right(response.code)
      case Left(error)     ⇒ Left(error)
    }
  }

  def changeProperty(waitForSync: Option[Boolean] = None,
                     journalSize: Option[Int]): Future[Either[ApiError, CollectionInfo]] = {
    driver.changeCollectionProperties(name, waitForSync, journalSize).map {
      case Right(response) ⇒
        Right(
          CollectionInfo(
            id = response.id,
            name = response.name,
            waitForSync = response.waitForSync,
            journalSize = response.journalSize,
            isVolatile = response.isVolatile,
            isSystem = response.isSystem,
            status = response.status,
            `type` = response.`type`
          ))
      case Left(error) ⇒ Left(error)
    }
  }

  def rename(newName: String): Future[Either[ApiError, ArangoCollection]] = {
    driver.renameCollection(name, newName).map {
      case Right(response) ⇒ Right(ArangoCollection(db, response.name))
      case Left(error)     ⇒ Left(error)
    }
  }

  def load: Future[Either[ApiError, CollectionStatus]] = {
    driver.loadCollection(name).map {
      case Right(response) ⇒ Right(response.status)
      case Left(error)     ⇒ Left(error)
    }
  }
  def unload: Future[Either[ApiError, CollectionStatus]] = {
    driver.unloadCollection(name).map {
      case Right(response) ⇒ Right(response.status)
      case Left(error)     ⇒ Left(error)
    }
  }
  def truncate: Future[Either[ApiError, Unit]] = {
    driver.truncateCollection(name).map {
      case Right(_)    ⇒ Right(())
      case Left(error) ⇒ Left(error)
    }
  }
  def drop: Future[Either[ApiError, Unit]] = {
    driver.dropCollection(name).map {
      case Right(_)    ⇒ Right(())
      case Left(error) ⇒ Left(error)
    }
  }
}

object ArangoCollection {
  def apply(db: ArangoDatabase, name: String)(implicit driver: ArangoDriver) = new ArangoCollection(db, name)
}