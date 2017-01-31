package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.generic.auto._
import io.tripod.oss.arangodb.driver.{ApiError, ArangoDriver}
import io.tripod.oss.arangodb.driver.entities.CollectionType

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 27/01/17.
  */
trait CollectionApi extends CodecsImplicits { self: ArangoDriver ⇒
  def createCollection(name: String)(implicit dbContext: Option[DBContext]): Future[CreateCollectionResponse] = {
    createCollection(name, None, None, None, None, None, None, None, None, None, None, None)(dbContext)
  }

  def createCollection(
      name: String,
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
      indexBuckets: Option[Int] = None)(implicit dbContext: Option[DBContext]): Future[CreateCollectionResponse] = {

    val request = CreateCollectionRequest(name,
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
    callApi[CreateCollectionRequest, CreateCollectionResponse](dbContext,
                                                               HttpMethods.POST,
                                                               "/_api/collection",
                                                               request)
  }

  def dropCollection(name: String, isSystem: Boolean = false)(
      implicit dbContext: Option[DBContext]): Future[DropCollectionResponse] = {
    callApi[DropCollectionResponse](dbContext, HttpMethods.DELETE, s"/_api/collection/$name?isSystem=$isSystem")
  }

  def truncateCollection(name: String)(implicit dbContext: Option[DBContext]): Future[TruncateCollectionResponse] = {
    callApi[TruncateCollectionResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/truncate")
  }

  def getCollection(name: String)(implicit dbContext: Option[DBContext]): Future[GetCollectionResponse] = {
    callApi[GetCollectionResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name")
  }

  def getCollectionProperties(name: String)(
      implicit dbContext: Option[DBContext]): Future[GetCollectionPropertiesResponse] = {
    callApi[GetCollectionPropertiesResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/properties")
  }

  def getCollectionCount(name: String)(implicit dbContext: Option[DBContext]): Future[GetCollectionCountResponse] = {
    callApi[GetCollectionCountResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/count")
  }

  def getCollectionFigures(name: String)(implicit dbContext: Option[DBContext]): Future[GetCollectionFiguresResponse] = {
    callApi[GetCollectionFiguresResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/figures")
  }

  def getCollectionRevision(name: String)(
      implicit dbContext: Option[DBContext]): Future[GetCollectionRevisionResponse] = {
    callApi[GetCollectionRevisionResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/revision")
  }

  def getCollectionChecksum(name: String)(
      implicit dbContext: Option[DBContext]): Future[GetCollectionChecksumResponse] = {
    callApi[GetCollectionChecksumResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/checksum")
  }

  def getCollections(implicit dbContext: Option[DBContext]): Future[GetCollectionsResponse] = {
    callApi[GetCollectionsResponse](dbContext, HttpMethods.GET, "/_api/collection")
  }

  def loadCollection(name: String, count: Boolean = true)(
      implicit dbContext: Option[DBContext]): Future[LoadCollectionResponse] = {
    callApi[LoadCollectionResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/load")
  }

  def unloadCollection(name: String)(implicit dbContext: Option[DBContext]): Future[UnloadCollectionResponse] = {
    callApi[UnloadCollectionResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/unload")
  }

  def changeCollectionProperties(name: String, waitForSync: Option[Boolean] = None, journalSize: Option[Int] = None)(
      implicit dbContext: Option[DBContext]): Future[ChangeCollectionPropertiesResponse] = {

    val request = ChangeCollectionPropertiesRequest(waitForSync, journalSize)
    callApi[ChangeCollectionPropertiesRequest, ChangeCollectionPropertiesResponse](
      dbContext,
      HttpMethods.PUT,
      s"/_api/collection/$name/properties",
      request)
  }

  def renameCollection(oldName: String, newName: String)(
      implicit dbContext: Option[DBContext]): Future[RenameCollectionResponse] = {
    callApi[RenameCollectionRequest, RenameCollectionResponse](dbContext,
                                                               HttpMethods.PUT,
                                                               s"/_api/collection/$oldName/rename",
                                                               RenameCollectionRequest(newName))
  }

  def rotateCollectionJournal(name: String)(
      implicit dbContext: Option[DBContext]): Future[RotateCollectionJournalResponse] = {
    callApi[RotateCollectionJournalResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/rotate")
  }
}
