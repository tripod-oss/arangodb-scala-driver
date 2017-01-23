package io.tripod.oss.arangodb.driver.database

import akka.actor.ActorRef
import io.circe.Encoder
import io.tripod.oss.arangodb.driver._

import scala.concurrent.{Future, Promise}

trait DatabaseApi { self: ArangoDriver ⇒
  def currentDatabase: Future[Either[ApiError, CurrentDatabaseResponse]] = {
    completeWithPromise[CurrentDatabaseResponse](promise ⇒
      router ! CurrentDatabase(promise))
  }

  def userDatabase: Future[Either[ApiError, DatabaseListResponse]] = {
    completeWithPromise[DatabaseListResponse](promise ⇒
      router ! UserDatabase(promise))
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
}
