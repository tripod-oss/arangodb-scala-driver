package io.tripod.oss.arangodb.driver.database

import akka.actor.ActorRef
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
}
