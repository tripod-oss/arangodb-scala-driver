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

trait DatabaseApi { self: ArangoDriver ⇒
  def currentDatabase: Future[Either[ApiError, CurrentDatabaseResponse]] = {
    completeWithPromise[CurrentDatabaseResponse](promise ⇒
      router ! CurrentDatabase(promise))
  }

  def userDatabase(implicit dbContext: Option[DBContext] = None)
    : Future[Either[ApiError, DatabaseListResponse]] = {
    implicit val encoder = None
    implicit val decoder = deriveDecoder[DatabaseListResponse]
    callApi(dbContext, HttpMethods.GET, "/_api/database/user")
  }

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
}
