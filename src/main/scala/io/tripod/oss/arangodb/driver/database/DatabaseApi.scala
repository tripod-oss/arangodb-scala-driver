package io.tripod.oss.arangodb.driver.database

import akka.actor.ActorRef
import io.tripod.oss.arangodb.driver.{
  ApiError,
  ArangoDriver,
  CurrentDatabase,
  UserDatabase
}

import scala.concurrent.{Future, Promise}

trait DatabaseApi { self: ArangoDriver ⇒
  def currentDatabase: Future[Either[ApiError, CurrentDatabaseResponse]] = {
    completeWithPromise[CurrentDatabaseResponse](promise ⇒
      router ! CurrentDatabase(promise))
  }

  def userDatabase: Future[Either[ApiError, UserDatabaseResponse]] = {
    completeWithPromise[UserDatabaseResponse](promise ⇒
      router ! UserDatabase(promise))
  }
}
