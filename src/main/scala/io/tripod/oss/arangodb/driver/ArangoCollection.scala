package io.tripod.oss.arangodb.driver

import com.typesafe.scalalogging.LazyLogging
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

class ArangoCollection(db: ArangoDatabase, name: String)(implicit val driver: ArangoDriver) extends LazyLogging {
  implicit val dbContext = db.dbContext
  implicit val ec        = driver.ec

  def count: Future[Int] = driver.getCollectionCount(name).map(_.count)

  def changeProperty(waitForSync: Option[Boolean] = None, journalSize: Option[Int]): Future[CollectionInfo] = {
    driver.changeCollectionProperties(name, waitForSync, journalSize).map { response ⇒
      CollectionInfo(id = response.id,
                     name = response.name,
                     waitForSync = response.waitForSync,
                     journalSize = response.journalSize,
                     isVolatile = response.isVolatile,
                     isSystem = response.isSystem,
                     status = response.status,
                     `type` = response.`type`)
    }
  }

  def info: Future[CollectionInfo] = {
    driver.getCollectionProperties(name).map { response ⇒
      CollectionInfo(
        id = response.id,
        name = response.name,
        waitForSync = response.waitForSync,
        journalSize = response.journalSize,
        isVolatile = response.isVolatile,
        isSystem = response.isSystem,
        status = response.status,
        `type` = response.`type`
      )
    }
  }

  def rename(newName: String): Future[ArangoCollection] = {
    driver.renameCollection(name, newName).map(response ⇒ ArangoCollection(db, response.name))
  }

  def load: Future[CollectionStatus] = driver.loadCollection(name).map(_.status)

  def unload: Future[CollectionStatus] = driver.unloadCollection(name).map(_.status)

  def truncate: Future[Unit] = driver.truncateCollection(name).map(_ ⇒ ())

  def drop: Future[Unit] = driver.dropCollection(name).map(_ ⇒ ())

  def rotateJournal: Future[Boolean] = {
    driver.rotateCollectionJournal(name).map(response ⇒ response.result).recover {
      case e: ApiException ⇒
        logger.warn(s"Collection [$name] journal rotation failed: ${e.errorMessage}")
        false
    }
  }

}

object ArangoCollection {
  def apply(db: ArangoDatabase, name: String)(implicit driver: ArangoDriver) = new ArangoCollection(db, name)
}
