package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.HttpMethods
import io.circe.Encoder
import io.circe.generic.auto._
import io.tripod.oss.arangodb.driver.{ApiError, ArangoDriver}

import scala.concurrent.Future

trait DatabaseApi { self: ArangoDriver ⇒

  def currentDatabase(implicit dbContext: Option[DBContext] = None): Future[CurrentDatabaseResponse] = {
    callApi[CurrentDatabaseResponse](dbContext, HttpMethods.GET, "/_api/database/current")
  }

  def userDatabase(implicit dbContext: Option[DBContext] = None): Future[DatabaseListResponse] = {
    callApi[DatabaseListResponse](dbContext, HttpMethods.GET, "/_api/database/user")
  }

  def databaseList(implicit dbContext: Option[DBContext] = None): Future[DatabaseListResponse] = {
    callApi[DatabaseListResponse](dbContext, HttpMethods.GET, "/_api/database")
  }

  def createDatabase[T: Encoder](dbName: String, users: Option[Seq[UserCreateOptions[T]]] = None)(
      implicit dbContext: Option[DBContext] = None): Future[CreateDatabaseResponse] = {

    val request = CreateDatabaseRequest(dbName, users)
    callApi[CreateDatabaseRequest[UserCreateOptions[T]], CreateDatabaseResponse](dbContext,
                                                                                 HttpMethods.POST,
                                                                                 "/_api/database",
                                                                                 request)
  }
  def removeDatabase(dbName: String): Future[DeleteDatabaseResponse] = {
    callApi[DeleteDatabaseResponse](None, HttpMethods.DELETE, s"/_api/database/$dbName")
  }
}
