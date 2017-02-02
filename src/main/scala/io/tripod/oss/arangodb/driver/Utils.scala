package io.tripod.oss.arangodb.driver

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.{EntityTag, `If-Match`, `If-None-Match`}

object Utils {
  def zipParams(paramNames: Seq[String], paramValues: Seq[Option[_]]) = {
    paramValues
      .zip(paramNames)
      .map {
        case (Some(value), param) => s"$param=$value"
        case _                    => ""
      }
      .filterNot(_.isEmpty)
      .mkString("&") match {
      case "" => ""
      case p  => "?" + p
    }
  }

  def etagHeader(matchTag: Option[Either[String, String]]): Option[HttpHeader] = {
    matchTag.map {
      case Left(etag)  => `If-None-Match`(EntityTag(etag))
      case Right(etag) => `If-Match`(EntityTag(etag))
    }
  }
}
