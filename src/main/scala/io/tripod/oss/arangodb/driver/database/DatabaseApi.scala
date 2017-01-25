package io.tripod.oss.arangodb.driver.database

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpMethods
import io.circe.Encoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.tripod.oss.arangodb.driver._
import io.tripod.oss.arangodb.driver.database.driver.ArangoDriver

import scala.concurrent.{Future, Promise}
import io.tripod.oss.arangodb.driver.utils.FutureUtils._
import io.circe._, io.circe.generic.semiauto._
import CodecsImplicits._

trait DatabaseApi { self: ArangoDriver ⇒

  def currentDatabase(implicit dbContext: Option[DBContext] = None)
    : Future[Either[ApiError, CurrentDatabaseResponse]] = {
    callApi[CurrentDatabaseResponse](dbContext,
                                     HttpMethods.GET,
                                     "/_api/database/current")
  }

  def userDatabase(implicit dbContext: Option[DBContext] = None)
    : Future[Either[ApiError, DatabaseListResponse]] = {
    callApi[DatabaseListResponse](dbContext,
                                  HttpMethods.GET,
                                  "/_api/database/user")
  }
  /*
  def databaseList: Future[Either[ApiError, DatabaseListResponse]] = {
    completeWithPromise[DatabaseListResponse](promise ⇒
      router ! ListDatabase(promise))
  }

  def createDatabase[T](dbName: String,
                        users: Option[Seq[UserCreateOptions[T]]] = None)(
      implicit extraEncoder: Encoder[T]): Future[Either[ApiError, Boolean]] = {
    completeWithPromise[CreateDatabaseResponse](promise ⇒
      router ! CreateDatabase(dbName, users, promise, extraEncoder))
      .map(result => result.map(_.result))

  }

  def removeDatabase(
      dbName: String): Future[Either[ApiError, RemoveDatabaseResponse]] = {
    completeWithPromise[RemoveDatabaseResponse](promise ⇒
      router ! RemoveDatabase(dbName, promise))
  }
 */
}
