package io.tripod.oss.arangodb.driver.database

import akka.actor.ActorRef
import io.tripod.oss.arangodb.driver.{ArangoDriver, CurrentDatabase, Error}

import scala.concurrent.{Future, Promise}

trait DatabaseApi { self: ArangoDriver ⇒
  def currentDatabase: Future[Either[Error, CurrentDatabaseResponse]] = {
    completeWithPromise[CurrentDatabaseResponse](promise ⇒
      router ! CurrentDatabase(promise))
  }
}
