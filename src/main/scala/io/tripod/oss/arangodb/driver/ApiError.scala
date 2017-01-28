package io.tripod.oss.arangodb.driver

case class ApiError(errorCode: Int = 0, errorMessage: String = "", errorBody: String = "")
