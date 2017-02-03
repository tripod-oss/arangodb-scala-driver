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

  /**
    * Return the number of documents in the collection
    * @return collection count return from API call
    * @throws ApiException (future failure) if the API call fails
    */
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

  /**
    * Return informations about the collection
    * @return collection informations
    * @throws ApiException (future failure) if the API call fails
    */
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

  /**
    * Rename and return a new ArangoCollection instance
    * @param newName new name to set for the collection
    * @return a new ArangoCollection instance pointing to the the collection renamed
    * @throws ApiException (future failure) if the API call fails
    */
  def rename(newName: String): Future[ArangoCollection] = {
    driver.renameCollection(name, newName).map(response ⇒ ArangoCollection(db, response.name))
  }

  /**
    * Load the collection
    * @return the collection status after call completes
    * @throws ApiException (future failure) if the API call fails
    */
  def load: Future[CollectionStatus] = driver.loadCollection(name).map(_.status)

  /**
    * Unload the collection
    * @return the collection status after call completes
    * @throws ApiException (future failure) if the API call fails
    */
  def unload: Future[CollectionStatus] = driver.unloadCollection(name).map(_.status)

  /**
    * Truncate collection from its content
    * @throws ApiException (future failure) if the API call fails
    */
  def truncate: Future[Unit] = driver.truncateCollection(name).map(_ ⇒ ())

  /**
    * Drop collection
    * @throws ApiException (future failure) if the API call fails
    */
  def drop: Future[Unit] = driver.dropCollection(name).map(_ ⇒ ())

  /**
    * Rotate collection journal
    * @return true if the journal was rotated, false otherwise
    */
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
