package io.tripod.oss.arangodb.driver

case class ApiError(error: Boolean,
                    code: Int,
                    errorNum: Int,
                    errorMessage: String,
                    _key: Option[String] = None,
                    _id: Option[String] = None,
                    _rev: Option[String] = None)
class ApiException(val error: Boolean,
                   code: Int,
                   val errorNum: Int,
                   val errorMessage: String,
                   _key: Option[String] = None,
                   _id: Option[String] = None,
                   _rev: Option[String] = None)
    extends Throwable
