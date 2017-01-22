package io.tripod.oss.arangodb.driver.database

import io.tripod.oss.arangodb.driver.ApiResponse

trait DatabaseApiResponse[T] extends ApiResponse {
  def result: T
  def error: Boolean
  def code: Int
}

case class CurrentDatabaseResponse(result: Map[String, String],
                                   error: Boolean,
                                   code: Int)
    extends DatabaseApiResponse[Map[String, String]]
