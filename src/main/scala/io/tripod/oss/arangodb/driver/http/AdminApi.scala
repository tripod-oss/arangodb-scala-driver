package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.generic.semiauto._
import io.tripod.oss.arangodb.driver.{ApiError, ArangoDriver}

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 27/01/17.
  */
trait AdminApi { self: ArangoDriver ⇒
  def getServerVersion(withDetails: Boolean = false)(
      implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, ServerVersionResponse]] = {

    implicit val requestEncoder  = deriveEncoder[ServerVersionRequest]
    implicit val responseDecoder = deriveDecoder[ServerVersionResponse]
    callApi[ServerVersionResponse](dbContext, HttpMethods.GET, s"/_api/version?details=$withDetails")
  }

  def getTargetVersion(withDetails: Boolean = false)(
      implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, TargetVersionResponse]] = {

    implicit val responseDecoder = deriveDecoder[TargetVersionResponse]
    callApi[TargetVersionResponse](dbContext, HttpMethods.GET, "/_admin/database/target-version")
  }
}
