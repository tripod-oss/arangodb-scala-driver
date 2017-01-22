package io.tripod.oss.arangodb.driver

trait ApiResponse

case class ApiError(errorCode: Int = 0,
                    errorMessage: String = "",
                    errorBody: String = "")
case class ServerVersionResponse(server: String,
                                 version: String,
                                 license: String,
                                 details: Option[Map[String, String]])
    extends ApiResponse
case class AuthResponse(jwt: String, must_change_password: Boolean)
    extends ApiResponse
