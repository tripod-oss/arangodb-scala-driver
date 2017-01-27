package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.generic.semiauto._
import CodecsImplicits._

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 27/01/17.
  */
trait CollectionApi { self: ArangoDriver ⇒
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
    implicit val collectionKeyOptionsEncoder            = deriveDecoder[CollectionKeyOptions]
    implicit val getCollectionPropertiesResponseDecoder = deriveDecoder[GetCollectionPropertiesResponse]
    callApi[GetCollectionPropertiesResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/properties")
  }

  def getCollectionCount(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionCountResponse]] = {
    implicit val collectionKeyOptionsEncoder = deriveDecoder[CollectionKeyOptions]
    implicit val responseDecoder             = deriveDecoder[GetCollectionCountResponse]
    callApi[GetCollectionCountResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/count")
  }

  def getCollectionFigures(name: String)(
      implicit dbContext: Option[DBContext]): Future[Either[ApiError, GetCollectionFiguresResponse]] = {
    implicit val collectionFiguresEncoder = deriveDecoder[CollectionFigure]
    implicit val responseDecoder          = deriveDecoder[GetCollectionFiguresResponse]
    callApi[GetCollectionFiguresResponse](dbContext, HttpMethods.GET, s"/_api/collection/$name/figures")
  }
}
