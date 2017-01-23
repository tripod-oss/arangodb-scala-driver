package io.tripod.oss.arangodb.driver

import io.tripod.oss.arangodb.driver.database.{
  CurrentDatabaseResponse,
  DatabaseListResponse
}

import scala.concurrent.Promise

sealed trait WorkMessage[T] {
  def resultPromise: Promise[Either[ApiError, T]]
}

case class GetServerVersion(
    withDetails: Boolean,
    resultPromise: Promise[Either[ApiError, ServerVersionResponse]])
    extends WorkMessage[ServerVersionResponse]

case class CurrentDatabase(
    resultPromise: Promise[Either[ApiError, CurrentDatabaseResponse]])
    extends WorkMessage[CurrentDatabaseResponse]

case class UserDatabase(
    resultPromise: Promise[Either[ApiError, DatabaseListResponse]])
    extends WorkMessage[DatabaseListResponse]

case class ListDatabase(
    resultPromise: Promise[Either[ApiError, DatabaseListResponse]])
    extends WorkMessage[DatabaseListResponse]
