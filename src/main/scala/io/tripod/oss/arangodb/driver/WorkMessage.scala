package io.tripod.oss.arangodb.driver

import io.tripod.oss.arangodb.driver.database.CurrentDatabaseResponse

import scala.concurrent.Promise

sealed trait WorkMessage[T] {
  def resultPromise: Promise[Either[Error, T]]
}

case class GetServerVersion(
    withDetails: Boolean,
    resultPromise: Promise[Either[Error, ServerVersionResponse]])
    extends WorkMessage[ServerVersionResponse]

case class CurrentDatabase(
    resultPromise: Promise[Either[Error, CurrentDatabaseResponse]])
    extends WorkMessage[CurrentDatabaseResponse]
