package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.{HttpHeader, HttpMethods}
import io.circe.{Decoder, Encoder}
import akka.http.scaladsl.model.headers.{EntityTag, `If-Match`, `If-None-Match`}
import io.tripod.oss.arangodb.driver.ArangoDriver
import io.tripod.oss.arangodb.driver.entities.ReadDocumentsRequestType.{Id, Key, Path}
import io.tripod.oss.arangodb.driver.entities.ReadDocumentsRequestType
import io.circe.generic.semiauto._

import scala.concurrent.Future

trait DocumentApi extends CodecsImplicits { self: ArangoDriver ⇒

  /**
    *
    * @param handle
    * @param matchTag optional etag matcher. If Left, a If-None-Match header will be added to the request. If Right, a If-Match header will be added
    * @param dbContext
    * @param responseDecoder
    * @tparam D
    * @return
    */
  def getDocument[D <: DocumentApiResponse](handle: String, matchTag: Option[Either[String, String]] = None)(
      implicit dbContext: Option[DBContext],
      responseDecoder: Decoder[D]) = {

    val headers = etagHeader(matchTag) match {
      case Some(httpHeader) ⇒ List(httpHeader)
      case None             ⇒ List.empty
    }
    callApi[D](dbContext, HttpMethods.GET, s"/_api/document/$handle", headers)
  }

  def readDocumentHeader[D <: DocumentHeaderResponse](handle: String, matchTag: Option[Either[String, String]])(
      implicit dbContext: Option[DBContext],
      responseDecoder: Decoder[D]) = {

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
    implicit val responseDecoder = deriveDecoder[ReadDocumentsResponse]
    implicit val requestEncoder  = deriveEncoder[ReadDocumentsRequest]
    callApi[ReadDocumentsRequest, ReadDocumentsResponse](dbContext,
                                                         HttpMethods.HEAD,
                                                         s"/_api/simple/all-keys",
                                                         request)
  }

  def createDocument[D](collectionName: String,
                        data: D,
                        waitForSync: Option[Boolean] = None,
                        returnNew: Option[Boolean] = None,
                        silent: Option[Boolean] = None)(implicit dbContext: Option[DBContext],
                                                        dataEncoder: Encoder[D]): Future[CreateDocumentResponse] = {

    val params = List(waitForSync.map(w => s"waitForSync=$w").getOrElse(""),
                      returnNew.map(r => s"returnNew=$r").getOrElse(""),
                      silent.map(s => s"silent=$s").getOrElse("")).mkString("&") match {
      case "" => ""
      case p  => "?" + p
    }

    silent match {
      case Some(true) =>
        callApi[D, SilentCreateDocumentResponse](dbContext,
                                                 HttpMethods.POST,
                                                 s"/_api/document/$collectionName$params",
                                                 data)
      case _ =>
        returnNew match {
          case Some(true) =>
            callApi[D, CreateDocumentWithNewResponse[D]](dbContext,
                                                         HttpMethods.POST,
                                                         s"/_api/document/$collectionName$params",
                                                         data)
          case _ =>
            callApi[D, CreateDocumentSimpleResponse](dbContext,
                                                     HttpMethods.POST,
                                                     s"/_api/document/$collectionName$params",
                                                     data)
        }
    }
  }

  private def etagHeader(matchTag: Option[Either[String, String]]): Option[HttpHeader] = {
    matchTag.map {
      case Left(etag)  => `If-None-Match`(EntityTag(etag))
      case Right(etag) => `If-Match`(EntityTag(etag))
    }
  }

}
