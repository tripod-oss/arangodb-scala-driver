package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.{HttpHeader, HttpMethods}
import io.circe.{Decoder, Encoder}
import akka.http.scaladsl.model.headers.{EntityTag, `If-Match`, `If-None-Match`}
import com.typesafe.scalalogging.LazyLogging
import io.tripod.oss.arangodb.driver.{ArangoDriver, Utils}
import io.tripod.oss.arangodb.driver.entities.ReadDocumentsRequestType.{Id, Key, Path}
import io.tripod.oss.arangodb.driver.entities.ReadDocumentsRequestType
import io.circe.generic.auto._

import scala.concurrent.Future

trait DocumentApi extends LazyLogging { self: ArangoDriver ⇒

  /**
    *
    * @param handle
    * @param matchTag optional etag matcher. If Left, a If-None-Match header will be added to the request. If Right, a If-Match header will be added
    * @param dbContext
    * @param responseDecoder
    * @tparam D
    * @return
    */
  def getDocument[D <: DocumentApiResponse: Decoder](handle: String, matchTag: Option[Either[String, String]] = None)(
      implicit dbContext: Option[DBContext]) = {

    val headers = etagHeader(matchTag) match {
      case Some(httpHeader) ⇒ List(httpHeader)
      case None             ⇒ List.empty
    }
    callApi[D](dbContext, HttpMethods.GET, s"/_api/document/$handle", headers)
  }

  def readDocumentHeader[D <: DocumentHeaderResponse: Decoder](
      handle: String,
      matchTag: Option[Either[String, String]])(implicit dbContext: Option[DBContext]) = {

    val headers = etagHeader(matchTag) match {
      case Some(httpHeader) ⇒ List(httpHeader)
      case None             ⇒ List.empty
    }
    callApi[D](dbContext, HttpMethods.HEAD, s"/_api/document/$handle", headers)
  }

  def readAllDocuments(collectionName: String, requestType: ReadDocumentsRequestType = Path)(
      implicit dbContext: Option[DBContext]): Future[ReadDocumentsResponse] = {
    val request = ReadDocumentsRequest(collection = collectionName, `type` = requestType match {
      case Id   => "id"
      case Key  => "key"
      case Path => "path"
    })
    callApi[ReadDocumentsRequest, ReadDocumentsResponse](dbContext, HttpMethods.PUT, s"/_api/simple/all-keys", request)
  }

  def createDocument[D: Encoder](collectionName: String,
                                 data: D,
                                 waitForSync: Option[Boolean] = None,
                                 returnNew: Option[Boolean] = None,
                                 silent: Option[Boolean] = None)(implicit dbContext: Option[DBContext],
                                                                 decoder: Decoder[D]): Future[DocumentResponse] = {

    val params = Utils.zipParams(Seq("waitForSync", "returnNew", "silent"), Seq(waitForSync, returnNew, silent))
    logger.debug(s"(waitForSync=$waitForSync, returnNew=$returnNew, silent=$silent) => url params='$params'")

    silent match {
      case Some(true) =>
        callApi[D, SilentDocumentResponse](dbContext, HttpMethods.POST, s"/_api/document/$collectionName$params", data)
      case _ =>
        callApi[D, CreateDocumentResponse[D]](dbContext,
                                              HttpMethods.POST,
                                              s"/_api/document/$collectionName$params",
                                              data)
    }
  }

  def replaceDocument[D: Encoder](
      collectionName: String,
      documentHandle: String,
      data: D,
      waitForSync: Option[Boolean] = None,
      ignoreRevs: Option[Boolean] = None,
      returnOld: Option[Boolean] = None,
      returnNew: Option[Boolean] = None,
      silent: Option[Boolean] = None,
      ifMatch: Option[String] = None)(implicit dbContext: Option[DBContext], decoder: Decoder[D]) = {
    val params = Utils.zipParams(Seq("waitForSync", "ignoreRevs", "returnNew", "silent"),
                                 Seq(waitForSync, ignoreRevs, returnNew, silent))
    val headers = ifMatch.map(etag ⇒ `If-Match`(EntityTag(etag))).toList

    silent match {
      case Some(true) =>
        callApi[D, SilentDocumentResponse](dbContext,
                                           HttpMethods.PUT,
                                           s"/_api/document/$collectionName/$documentHandle$params",
                                           data,
                                           headers)
      case _ =>
        callApi[D, ReplaceDocumentResponse[D]](dbContext,
                                               HttpMethods.PUT,
                                               s"/_api/document/$collectionName/$documentHandle$params",
                                               data,
                                               headers)
    }

    //callApi[D, ]
  }

  private def etagHeader(matchTag: Option[Either[String, String]]): Option[HttpHeader] = {
    matchTag.map {
      case Left(etag)  => `If-None-Match`(EntityTag(etag))
      case Right(etag) => `If-Match`(EntityTag(etag))
    }
  }

}
