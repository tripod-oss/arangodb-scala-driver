package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.generic.semiauto._

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 27/01/17.
  */
trait MiscApi { self: ArangoDriver ⇒
  def getServerVersion(withDetails: Boolean = false)(
      implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, ServerVersionResponse]] = {

    implicit val serverVersionRequestEncoder  = deriveEncoder[ServerVersionRequest]
    implicit val serverVersionResponseDecoder = deriveDecoder[ServerVersionResponse]
    callApi[ServerVersionResponse](dbContext, HttpMethods.GET, s"/_api/version?details=$withDetails")
  }
}
