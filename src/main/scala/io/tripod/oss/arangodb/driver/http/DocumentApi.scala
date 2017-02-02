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
    * @tparam D
    * @return
    */
  def getDocument[D <: DocumentApiResponse: Decoder](handle: String, matchTag: Option[Either[String, String]] = None)(
      implicit dbContext: Option[DBContext]) = {

    val headers = Utils.etagHeader(matchTag) match {
      case Some(httpHeader) ⇒ List(httpHeader)
      case None             ⇒ List.empty
    }
    callApi[D](dbContext, HttpMethods.GET, s"/_api/document/$handle", headers)
  }

  def readDocumentHeader[D <: DocumentHeaderResponse: Decoder](
      handle: String,
      matchTag: Option[Either[String, String]])(implicit dbContext: Option[DBContext]) = {

    val headers = Utils.etagHeader(matchTag) match {
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
                                 document: D,
                                 waitForSync: Option[Boolean] = None,
                                 returnNew: Option[Boolean] = None,
                                 silent: Option[Boolean] = None)(implicit dbContext: Option[DBContext],
                                                                 decoder: Decoder[D]): Future[DocumentResponse] = {

    val params = Utils.zipParams(Seq("waitForSync", "returnNew", "silent"), Seq(waitForSync, returnNew, silent))
    logger.debug(s"(waitForSync=$waitForSync, returnNew=$returnNew, silent=$silent) => url params='$params'")

    silent match {
      case Some(true) =>
        callApi[D, SilentDocumentResponse](dbContext,
                                           HttpMethods.POST,
                                           s"/_api/document/$collectionName$params",
                                           document)
      case _ =>
        callApi[D, CreateDocumentResponse[D]](dbContext,
                                              HttpMethods.POST,
                                              s"/_api/document/$collectionName$params",
                                              document)
    }
  }

  def replaceDocument[D: Encoder](collectionName: String,
                                  documentHandle: String,
                                  newDocument: D,
                                  waitForSync: Option[Boolean] = None,
                                  ignoreRevs: Option[Boolean] = None,
                                  returnOld: Option[Boolean] = None,
                                  returnNew: Option[Boolean] = None,
                                  silent: Option[Boolean] = None,
                                  ifMatch: Option[String] = None)(implicit dbContext: Option[DBContext],
                                                                  decoder: Decoder[D]): Future[DocumentResponse] = {
    val params = Utils.zipParams(Seq("waitForSync", "ignoreRevs", "returnNew", "silent"),
                                 Seq(waitForSync, ignoreRevs, returnNew, silent))
    val headers = ifMatch.map(etag ⇒ `If-Match`(EntityTag(etag))).toList

    silent match {
      case Some(true) =>
        callApi[D, SilentDocumentResponse](dbContext,
                                           HttpMethods.PUT,
                                           s"/_api/document/$collectionName/$documentHandle$params",
                                           newDocument,
                                           headers)
      case _ =>
        callApi[D, ReplaceDocumentResponse[D]](dbContext,
                                               HttpMethods.PUT,
                                               s"/_api/document/$collectionName/$documentHandle$params",
                                               newDocument,
                                               headers)
    }
  }

  def replaceDocuments[D: Encoder](collectionName: String,
                                   newDocuments: List[D],
                                   waitForSync: Option[Boolean] = None,
                                   ignoreRevs: Option[Boolean] = None,
                                   returnOld: Option[Boolean] = None,
                                   returnNew: Option[Boolean] = None,
                                   silent: Option[Boolean] = None,
                                   ifMatch: Option[String] = None)(implicit dbContext: Option[DBContext],
                                                                   decoder: Decoder[D]): Future[DocumentResponse] = {
    val params = Utils.zipParams(Seq("waitForSync", "ignoreRevs", "returnNew", "silent"),
                                 Seq(waitForSync, ignoreRevs, returnNew, silent))
    val headers = ifMatch.map(etag ⇒ `If-Match`(EntityTag(etag))).toList

    silent match {
      case Some(true) =>
        callApi[List[D], SilentDocumentResponse](dbContext,
                                                 HttpMethods.PUT,
                                                 s"/_api/document/$collectionName$params",
                                                 newDocuments,
                                                 headers)
      case _ =>
        callApi[List[D], ReplaceDocumentResponse[List[D]]](dbContext,
                                                           HttpMethods.PUT,
                                                           s"/_api/document/$collectionName$params",
                                                           newDocuments,
                                                           headers)
    }
  }

  def updateDocument[D: Encoder](collectionName: String,
                                 documentHandle: String,
                                 updateDocument: D,
                                 keepNull: Option[Boolean] = None,
                                 mergeObjects: Option[Boolean] = None,
                                 waitForSync: Option[Boolean] = None,
                                 ignoreRevs: Option[Boolean] = None,
                                 returnOld: Option[Boolean] = None,
                                 returnNew: Option[Boolean] = None,
                                 silent: Option[Boolean] = None,
                                 ifMatch: Option[String] = None)(implicit dbContext: Option[DBContext],
                                                                 decoder: Decoder[D]): Future[DocumentResponse] = {
    val params = Utils.zipParams(Seq("keepNull", "mergeObjects", "waitForSync", "ignoreRevs", "returnNew", "silent"),
                                 Seq(keepNull, mergeObjects, waitForSync, ignoreRevs, returnNew, silent))
    val headers = ifMatch.map(etag ⇒ `If-Match`(EntityTag(etag))).toList
    silent match {
      case Some(true) =>
        callApi[D, SilentDocumentResponse](dbContext,
                                           HttpMethods.PATCH,
                                           s"/_api/document/$collectionName/$documentHandle$params",
                                           updateDocument,
                                           headers)
      case _ =>
        callApi[D, UpdateDocumentResponse[D]](dbContext,
                                              HttpMethods.PATCH,
                                              s"/_api/document/$collectionName/$documentHandle$params",
                                              updateDocument,
                                              headers)
    }
  }
  def updateDocuments[D: Encoder](collectionName: String,
                                  updateDocuments: List[D],
                                  keepNull: Option[Boolean] = None,
                                  mergeObjects: Option[Boolean] = None,
                                  waitForSync: Option[Boolean] = None,
                                  ignoreRevs: Option[Boolean] = None,
                                  returnOld: Option[Boolean] = None,
                                  returnNew: Option[Boolean] = None,
                                  silent: Option[Boolean] = None,
                                  ifMatch: Option[String] = None)(implicit dbContext: Option[DBContext],
                                                                  decoder: Decoder[D]): Future[DocumentResponse] = {
    val params = Utils.zipParams(Seq("keepNull", "mergeObjects", "waitForSync", "ignoreRevs", "returnNew", "silent"),
                                 Seq(keepNull, mergeObjects, waitForSync, ignoreRevs, returnNew, silent))
    val headers = ifMatch.map(etag ⇒ `If-Match`(EntityTag(etag))).toList
    silent match {
      case Some(true) =>
        callApi[List[D], SilentDocumentResponse](dbContext,
                                                 HttpMethods.PATCH,
                                                 s"/_api/document/$collectionName$params",
                                                 updateDocuments,
                                                 headers)
      case _ =>
        callApi[List[D], UpdateDocumentResponse[List[D]]](dbContext,
                                                          HttpMethods.PATCH,
                                                          s"/_api/document/$collectionName$params",
                                                          updateDocuments,
                                                          headers)
    }
  }
}
