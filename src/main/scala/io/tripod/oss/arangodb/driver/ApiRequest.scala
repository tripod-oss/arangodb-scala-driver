package io.tripod.oss.arangodb.driver

sealed trait ApiRequest

case class DatabaseListRequest() extends ApiRequest
case class ServerVersionRequest(details: Boolean) extends ApiRequest
