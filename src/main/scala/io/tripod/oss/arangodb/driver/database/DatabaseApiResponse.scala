package io.tripod.oss.arangodb.driver.database

import io.tripod.oss.arangodb.driver.ApiResponse

trait DatabaseApiResponse[T] extends ApiResponse {
  def result: T
  def error: Boolean
  def code: Int
}

case class CurrentDatabaseResponseResult(name: String,
                                         id: String,
                                         path: String,
                                         isSystem: Boolean)

case class CurrentDatabaseResponse(result: CurrentDatabaseResponseResult,
                                   error: Boolean,
                                   code: Int)
    extends DatabaseApiResponse[CurrentDatabaseResponseResult]

case class DatabaseListResponse(result: Seq[String], error: Boolean, code: Int)
    extends DatabaseApiResponse[Seq[String]]
