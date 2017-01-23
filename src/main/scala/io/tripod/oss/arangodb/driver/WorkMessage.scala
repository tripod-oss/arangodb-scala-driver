package io.tripod.oss.arangodb.driver

import io.circe.Encoder
import io.tripod.oss.arangodb.driver.database._

import scala.concurrent.Promise

sealed trait WorkMessage[R <: ApiRequest, Q <: ApiResponse] {
  def request: R
  def dbContext: Option[String]
  def resultPromise: Promise[Either[ApiError, Q]]
}

case class GetServerVersion(
    request: ServerVersionRequest,
    resultPromise: Promise[Either[ApiError, ServerVersionResponse]],
    dbContext: Option[String] = None)
    extends WorkMessage[ServerVersionRequest, ServerVersionResponse]

case class CurrentDatabase(
    resultPromise: Promise[Either[ApiError, CurrentDatabaseResponse]],
    dbContext: Option[String] = None)
//extends WorkMessage[CurCurrentDatabaseResponse]

case class UserDatabase(
    resultPromise: Promise[Either[ApiError, DatabaseListResponse]],
    dbContext: Option[String] = None)
//extends WorkMessage[DatabaseListResponse]

case class ListDatabase(
    resultPromise: Promise[Either[ApiError, DatabaseListResponse]],
    dbContext: Option[String] = None)
//extends WorkMessage[DatabaseListResponse]

case class CreateDatabase[T](
    dbName: String,
    users: Option[Seq[UserCreateOptions[T]]],
    resultPromise: Promise[Either[ApiError, CreateDatabaseResponse]],
    extraEncoder: Encoder[T],
    dbContext: Option[String] = None)
//extends WorkMessage[CreateDatabaseResponse]

case class RemoveDatabase(
    dbName: String,
    resultPromise: Promise[Either[ApiError, RemoveDatabaseResponse]],
    dbContext: Option[String] = None)
//extends WorkMessage[RemoveDatabaseResponse]
