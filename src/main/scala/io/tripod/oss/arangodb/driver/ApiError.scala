package io.tripod.oss.arangodb.driver

case class ApiError(error: Boolean, code: Int, errorNum: Int, errorMessage: String)
class ApiException(val error: Boolean, code: Int, val errorNum: Int, val errorMessage: String) extends Throwable
//                errorCode: Int = 0, errorMessage: String = "", errorBody: String = "")
