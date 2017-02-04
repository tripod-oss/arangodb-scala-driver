package io.tripod.oss.arangodb.driver

import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.tripod.oss.arangodb.driver.entities._
import io.tripod.oss.arangodb.driver.http.DeleteDocumentResponse

import scala.concurrent.Future

class ArangoCollection(db: ArangoDatabase, name: String, defaultWaitForSync: Option[Boolean] = None)(
    implicit val driver: ArangoDriver)
    extends LazyLogging {
  implicit val dbContext = db.dbContext
  implicit val ec        = driver.ec

  /**
    * Return the number of documents in the collection
    * @return collection count return from API call
    * @throws ApiException (future failure) if the API call fails
    */
  def count: Future[Int] = driver.getCollectionCount(name).map(_.count)

  def changeProperty(waitForSync: Option[Boolean] = defaultWaitForSync,
                     journalSize: Option[Int]): Future[CollectionInfo] = {
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

  /**
    * Delete a document from the collection and returns its info
    * @param handle document handle key to delete
    * @tparam D type of the object returned
    * @return document information about the deleted document
    */
  def deleteDocument[D](handle: String): Future[DocumentDeleteInfo[D]] = {
    deleteDocument[D](handle, defaultWaitForSync, None, None, None).map(_.get)
  }

  /**
    * Delete a document from the collection
    * @param handle document handle key to delete
    * @param waitForSync waitForSync API parameter
    * @param returnOld returnOld API parameter
    * @param silent waitForSync API parameter
    * @param ifMatch ifMatch API parameter
    * @tparam D type of the object returned
    * @return document information about the deleted document or None if silent was set to Some(true)
    */
  def deleteDocument[D](handle: String,
                        waitForSync: Option[Boolean] = defaultWaitForSync,
                        returnOld: Option[Boolean] = None,
                        silent: Option[Boolean] = None,
                        ifMatch: Option[String] = None): Future[Option[DocumentDeleteInfo[D]]] = {
    silent match {
      case Some(true) ⇒ driver.deleteDocument(name, handle, waitForSync, returnOld, silent, ifMatch).map(_ ⇒ None)
      case _ ⇒
        driver
          .deleteDocument(name, handle, waitForSync, returnOld, silent, ifMatch)
          .mapTo[DeleteDocumentResponse[D]]
          .map(response ⇒
            Some(DocumentDeleteInfo(id = response._id, key = response._key, rev = response._rev, old = response.old)))
    }
  }

  /**
    * Delete documents given a list of document keys
    * @param documentKeys List of keys of documents to delete
    * @return A list of deleted document info
    */
  def deleteDocuments[D: Decoder](documentKeys: List[String]): Future[List[DocumentDeleteInfo[D]]] = {
    driver
      .deleteDocuments(name, documentKeys, defaultWaitForSync, Some(false))
      .map(response ⇒
        response.map(deleted =>
          DocumentDeleteInfo(id = deleted._id, key = deleted._key, rev = deleted._rev, old = deleted.old)))
  }

  /**
    * Delete documents given a list of document selector
    * @param documentKeys List of selector (key + rev) of documents to delete
    * @param waitForSync waitForSync API parameter
    * @param returnOld returnOld API parameter
    * @param ignoreRevs ignoreRevs API parameter
    * @tparam D Type of the object returned if returnedOld is set to Some(true)
    * @return A list of deleted document info
    */
  def deleteDocuments[D: Decoder](documentKeys: List[DocumentSelector],
                                  waitForSync: Option[Boolean] = defaultWaitForSync,
                                  returnOld: Option[Boolean] = None,
                                  ignoreRevs: Option[Boolean] = None): Future[List[DocumentDeleteInfo[D]]] = {
    import io.circe.generic.auto._
    driver
      .deleteDocuments(name, documentKeys, waitForSync, returnOld, ignoreRevs)
      .map(response ⇒
        response.map(deleted =>
          DocumentDeleteInfo(id = deleted._id, key = deleted._key, rev = deleted._rev, old = deleted.old)))
  }

}

object ArangoCollection {
  def apply(db: ArangoDatabase, name: String)(implicit driver: ArangoDriver) = new ArangoCollection(db, name)
}
