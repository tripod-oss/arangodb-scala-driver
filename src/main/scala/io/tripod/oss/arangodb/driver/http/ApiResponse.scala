package io.tripod.oss.arangodb.driver.http

trait ApiResponse

case class ApiError(errorCode: Int = 0, errorMessage: String = "", errorBody: String = "")
case class ServerVersionResponse(server: String,
                                 version: String,
                                 license: String,
                                 details: Option[Map[String, String]])
    extends ApiResponse
case class AuthResponse(jwt: String, must_change_password: Boolean) extends ApiResponse

trait ErrorResult {
  def error: Boolean
  def code: Int
}

trait DatabaseApiResponse[T] extends ApiResponse with ErrorResult {
  def result: T
}

case class CurrentDatabaseResponseResult(name: String, id: String, path: String, isSystem: Boolean)

case class CurrentDatabaseResponse(result: CurrentDatabaseResponseResult, error: Boolean, code: Int)
    extends DatabaseApiResponse[CurrentDatabaseResponseResult]

case class DatabaseListResponse(result: Seq[String], error: Boolean, code: Int)
    extends DatabaseApiResponse[Seq[String]]

case class CreateDatabaseResponse(result: Boolean, error: Boolean, code: Int) extends DatabaseApiResponse[Boolean]

case class DeleteDatabaseResponse(result: Boolean, error: Boolean, code: Int) extends DatabaseApiResponse[Boolean]

trait CollectionStatus
case object Deleted                     extends CollectionStatus
case object InTheProcessOfBeingUnloaded extends CollectionStatus
case object Loaded                      extends CollectionStatus
case object NewBornCollection           extends CollectionStatus
case object Unloaded                    extends CollectionStatus

case class CreateCollectionResponse(id: String,
                                    name: String,
                                    waitForSync: Boolean,
                                    isVolatile: Boolean,
                                    isSystem: Boolean,
                                    status: CollectionStatus,
                                    `type`: CollectionType,
                                    error: Boolean,
                                    code: Int)
    extends ApiResponse
    with ErrorResult
