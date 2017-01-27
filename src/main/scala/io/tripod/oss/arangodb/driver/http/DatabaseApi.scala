package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.Encoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.tripod.oss.arangodb.driver.http.CodecsImplicits._

import scala.concurrent.Future

trait DatabaseApi { self: ArangoDriver ⇒

  def currentDatabase(
      implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, CurrentDatabaseResponse]] = {
    callApi[CurrentDatabaseResponse](dbContext, HttpMethods.GET, "/_api/database/current")
  }

  def userDatabase(implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, DatabaseListResponse]] = {
    callApi[DatabaseListResponse](dbContext, HttpMethods.GET, "/_api/database/user")
  }

  def databaseList(implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, DatabaseListResponse]] = {
    callApi[DatabaseListResponse](dbContext, HttpMethods.GET, "/_api/database")
  }

  def createDatabase[T](dbName: String, users: Option[Seq[UserCreateOptions[T]]] = None)(
      implicit optionsEncoder: Encoder[T],
      dbContext: Option[DBContext] = None): Future[Either[ApiError, Boolean]] = {

    val request                               = CreateDatabaseRequest(dbName, users)
    implicit val userCreateOptionsEncoder     = deriveEncoder[UserCreateOptions[T]]
    implicit val createDatabaseRequestEncoder = deriveEncoder[CreateDatabaseRequest[UserCreateOptions[T]]]
    implicit val extraEncoder                 = optionsEncoder

    callApi[CreateDatabaseRequest[UserCreateOptions[T]], CreateDatabaseResponse](dbContext,
                                                                                 HttpMethods.POST,
                                                                                 "/_api/database",
                                                                                 request).map(result =>
      result.map(_.result))
  }
  def removeDatabase(dbName: String)(
      implicit dbContext: Option[DBContext] = None): Future[Either[ApiError, DeleteDatabaseResponse]] = {
    implicit val deleteDatabaseResponseDecoder = deriveDecoder[DeleteDatabaseResponse]
    callApi[DeleteDatabaseResponse](dbContext, HttpMethods.DELETE, s"/_api/database/$dbName")
  }
}
