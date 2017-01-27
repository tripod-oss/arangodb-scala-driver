package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.generic.semiauto._
import CodecsImplicits._

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 27/01/17.
  */
trait CollectionApi { self: ArangoDriver ⇒
  def createCollection(name: String,
                       journalSize: Option[Int] = None,
                       replicationFactor: Option[Int] = None,
                       keyOptions: Option[CreateCollectionRequestKeyOptions] = None,
                       waitForSync: Option[Boolean] = None,
                       doCompact: Option[Boolean] = None,
                       isVolatile: Option[Boolean] = None,
                       shardKeys: Option[String] = None,
                       numberOfShards: Option[Int] = None,
                       isSystem: Option[Boolean] = None,
                       `type`: Option[CollectionType] = None,
                       indexBuckets: Option[Int] = None)(
      implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, CreateCollectionResponse]] = {

    implicit val createCollectionRequestKeyEncoder = deriveEncoder[CreateCollectionRequestKeyOptions]
    implicit val createCollectionRequestEncoder    = deriveEncoder[CreateCollectionRequest]
    implicit val createCollectionResponseDecoder   = deriveDecoder[CreateCollectionResponse]
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

}
