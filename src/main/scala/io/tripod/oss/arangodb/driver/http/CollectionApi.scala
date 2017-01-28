package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.generic.semiauto._

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 27/01/17.
  */
trait CollectionApi extends CodecsImplicits { self: ArangoDriver ⇒
  def createCollection(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, CreateCollectionResponse]] = {
    createCollection(name, None, None, None, None, None, None, None, None, None, None, None)(dbContext)
  }

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
                       indexBuckets: Option[Int] = None)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, CreateCollectionResponse]] = {

    implicit val collectionKeyOptionsEncoder     = deriveEncoder[CollectionKeyOptions]
    implicit val createCollectionRequestEncoder  = deriveEncoder[CreateCollectionRequest]
    implicit val createCollectionResponseDecoder = deriveDecoder[CreateCollectionResponse]
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
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, DropCollectionResponse]] = {
    implicit val dropCollectionResponseDecoder = deriveDecoder[DropCollectionResponse]
    callApi[DropCollectionResponse](dbContext, HttpMethods.DELETE, s"/_api/collection/$name?isSystem=$isSystem")
  }

  def truncateCollection(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, TruncateCollectionResponse]] = {
    implicit val dropCollectionResponseDecoder = deriveDecoder[TruncateCollectionResponse]
    callApi[TruncateCollectionResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/truncate")
  }

  def getCollection(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionResponse]] = {
    implicit val getCollectionResponseDecoder = deriveDecoder[GetCollectionResponse]
    callApi[GetCollectionResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name")
  }

  def getCollectionProperties(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionPropertiesResponse]] = {
    implicit val getCollectionPropertiesResponseDecoder = deriveDecoder[GetCollectionPropertiesResponse]
    callApi[GetCollectionPropertiesResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/properties")
  }

  def getCollectionCount(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionCountResponse]] = {
    implicit val responseDecoder = deriveDecoder[GetCollectionCountResponse]
    callApi[GetCollectionCountResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/count")
  }

  def getCollectionFigures(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionFiguresResponse]] = {
    implicit val collectionFiguresEncoder = deriveDecoder[CollectionFigure]
    implicit val responseDecoder          = deriveDecoder[GetCollectionFiguresResponse]
    callApi[GetCollectionFiguresResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/figures")
  }

  def getCollectionRevision(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionRevisionResponse]] = {
    implicit val responseDecoder = deriveDecoder[GetCollectionRevisionResponse]
    callApi[GetCollectionRevisionResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/revision")
  }

  def getCollectionChecksum(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionChecksumResponse]] = {
    implicit val responseDecoder = deriveDecoder[GetCollectionChecksumResponse]
    callApi[GetCollectionChecksumResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/checksum")
  }

  def getCollections(implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionsResponse]] = {
    implicit val getCollectionResponseDecoder = deriveDecoder[GetCollectionsResponse]
    callApi[GetCollectionsResponse](dbContext, HttpMethods.GET, "/_api/collection")
  }

  def loadCollection(name: String, count: Boolean = true)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, LoadCollectionResponse]] = {
    implicit val responseDecoder = deriveDecoder[LoadCollectionResponse]
    callApi[LoadCollectionResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/load")
  }

  def unloadCollection(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, UnloadCollectionResponse]] = {
    implicit val responseDecoder = deriveDecoder[UnloadCollectionResponse]
    callApi[UnloadCollectionResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/unload")
  }

  def changeCollectionProperties(name: String, waitForSync: Option[Boolean] = None, journalSize: Option[Int] = None)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, ChangeCollectionPropertiesResponse]] = {
    implicit val responseDecoder = deriveDecoder[ChangeCollectionPropertiesResponse]
    implicit val requestEncoder  = deriveEncoder[ChangeCollectionPropertiesRequest]

    val request = ChangeCollectionPropertiesRequest(waitForSync, journalSize)
    callApi[ChangeCollectionPropertiesRequest, ChangeCollectionPropertiesResponse](
      dbContext,
      HttpMethods.PUT,
      s"/_api/collection/$name/properties",
      request)
  }

  def renameCollection(oldName: String, newName: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, RenameCollectionResponse]] = {
    implicit val requestEncoder  = deriveEncoder[RenameCollectionRequest]
    implicit val responseDecoder = deriveDecoder[RenameCollectionResponse]
    callApi[RenameCollectionRequest, RenameCollectionResponse](dbContext,
                                                               HttpMethods.PUT,
                                                               s"/_api/collection/$oldName/rename",
                                                               RenameCollectionRequest(newName))
  }

  def rotateCollectionJournal(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, RotateCollectionJournalResponse]] = {
    implicit val responseDecoder = deriveDecoder[RotateCollectionJournalResponse]
    callApi[RotateCollectionJournalResponse](dbContext, HttpMethods.PUT, s"/_api/collection/$name/rotate")
  }
}
