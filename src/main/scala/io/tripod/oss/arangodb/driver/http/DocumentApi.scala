package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.{HttpHeader, HttpMethods}
import io.circe.Decoder
import akka.http.scaladsl.model.headers.{EntityTag, `If-Match`, `If-None-Match`}
import io.tripod.oss.arangodb.driver.ArangoDriver

trait DocumentApi extends CodecsImplicits { self: ArangoDriver ⇒
  def getDocument[D <: DocumentApiResponse](
      handle: String,
      ifNoneMatch: Option[String] = None,
      ifMatch: Option[String] = None)(implicit dbContext: Option[DBContext], responseDecoder: Decoder[D]) = {

    val headers = ifNoneMatch
      .map(etag ⇒ `If-None-Match`(EntityTag(etag)))
      .orElse(
        ifMatch.map(etag ⇒ `If-Match`(EntityTag(etag)))
      ) match {
      case Some(httpHeader) ⇒ List(httpHeader)
      case None             ⇒ List.empty
    }

    callApi[D](dbContext, HttpMethods.GET, s"/_api/document/$handle", headers)
  }
}
