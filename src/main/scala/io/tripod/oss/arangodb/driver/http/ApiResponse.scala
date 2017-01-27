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
case object NewBornCollection           extends CollectionStatus // 1
case object Unloaded                    extends CollectionStatus // 2
case object Loaded                      extends CollectionStatus // 3
case object InTheProcessOfBeingUnloaded extends CollectionStatus // 4
case object Deleted                     extends CollectionStatus // 5
case object Loading                     extends CollectionStatus

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

case class DropCollectionResponse(id: String, error: Boolean, code: Int) extends ApiResponse with ErrorResult

case class TruncateCollectionResponse(id: String,
                                      name: String,
                                      isSystem: Boolean,
                                      status: CollectionStatus,
                                      `type`: CollectionType,
                                      error: Boolean,
                                      code: Int)
    extends ApiResponse
    with ErrorResult

case class GetCollectionResponse(id: String,
                                 name: String,
                                 status: CollectionStatus,
                                 `type`: CollectionType,
                                 isSystem: Boolean,
                                 error: Boolean,
                                 code: Int)
    extends ApiResponse
    with ErrorResult

case class GetCollectionPropertiesResponse(id: String,
                                           name: String,
                                           isSystem: Boolean,
                                           doCompact: Boolean,
                                           isVolatile: Boolean,
                                           journalSize: Int,
                                           keyOptions: CollectionKeyOptions,
                                           waitForSync: Boolean,
                                           indexBuckets: Int,
                                           status: CollectionStatus,
                                           `type`: CollectionType,
                                           error: Boolean,
                                           code: Int)
    extends ApiResponse
    with ErrorResult

case class GetCollectionCountResponse(id: String,
                                      name: String,
                                      isSystem: Boolean,
                                      doCompact: Boolean,
                                      isVolatile: Boolean,
                                      journalSize: Int,
                                      keyOptions: CollectionKeyOptions,
                                      waitForSync: Boolean,
                                      indexBuckets: Int,
                                      count: Int,
                                      status: CollectionStatus,
                                      `type`: CollectionType,
                                      error: Boolean,
                                      code: Int)
    extends ApiResponse
    with ErrorResult

case class GetCollectionFiguresResponse(id: String,
                                        name: String,
                                        isSystem: Boolean,
                                        doCompact: Boolean,
                                        isVolatile: Boolean,
                                        journalSize: Int,
                                        keyOptions: CollectionKeyOptions,
                                        waitForSync: Boolean,
                                        indexBuckets: Int,
                                        count: Int,
                                        figures: CollectionFigure,
                                        status: CollectionStatus,
                                        `type`: CollectionType,
                                        error: Boolean,
                                        code: Int)
    extends ApiResponse
    with ErrorResult

case class GetCollectionRevisionResponse(id: String,
                                         name: String,
                                         isSystem: Boolean,
                                         status: CollectionStatus,
                                         `type`: CollectionType,
                                         revision: Int,
                                         error: Boolean,
                                         code: Int)
    extends ApiResponse
    with ErrorResult

case class GetCollectionChecksumResponse(id: String,
                                         name: String,
                                         isSystem: Boolean,
                                         status: CollectionStatus,
                                         `type`: CollectionType,
                                         checksum: String,
                                         revision: String,
                                         error: Boolean,
                                         code: Int)
    extends ApiResponse
    with ErrorResult
