package io.tripod.oss.arangodb.driver.http

import io.tripod.oss.arangodb.driver.entities.{CollectionStatus, CollectionType}

trait ApiResponse

trait ErrorResult {
  def error: Boolean
  def code: Int
}

trait DatabaseApiResponse[T] extends ApiResponse with ErrorResult {
  def result: T
}

case class ServerVersionResponse(server: String,
                                 version: String,
                                 license: String,
                                 details: Option[Map[String, String]])
    extends ApiResponse
case class TargetVersionResponse(version: String, error: Boolean, code: Int) extends ApiResponse
case class AuthResponse(jwt: String, must_change_password: Boolean)          extends ApiResponse

case class CurrentDatabaseResponseResult(name: String, id: String, path: String, isSystem: Boolean)

case class CurrentDatabaseResponse(result: CurrentDatabaseResponseResult, error: Boolean, code: Int)
    extends DatabaseApiResponse[CurrentDatabaseResponseResult]

case class DatabaseListResponse(result: Seq[String], error: Boolean, code: Int)
    extends DatabaseApiResponse[Seq[String]]

case class CreateDatabaseResponse(result: Boolean, error: Boolean, code: Int) extends DatabaseApiResponse[Boolean]

case class DeleteDatabaseResponse(result: Boolean, error: Boolean, code: Int) extends DatabaseApiResponse[Boolean]

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

case class GetCollectionsResponse(result: Seq[CollectionInfo], error: Boolean, code: Int)
    extends ApiResponse
    with ErrorResult

case class LoadCollectionResponse(id: String,
                                  name: String,
                                  isSystem: Boolean,
                                  status: CollectionStatus,
                                  `type`: CollectionType,
                                  count: Option[Int],
                                  error: Boolean,
                                  code: Int)
    extends ApiResponse
    with ErrorResult

case class UnloadCollectionResponse(id: String,
                                    name: String,
                                    isSystem: Boolean,
                                    status: CollectionStatus,
                                    `type`: CollectionType,
                                    error: Boolean,
                                    code: Int)
    extends ApiResponse
    with ErrorResult

case class ChangeCollectionPropertiesResponse(id: String,
                                              name: String,
                                              waitForSync: Boolean,
                                              journalSize: Int,
                                              status: CollectionStatus,
                                              `type`: CollectionType,
                                              isSystem: Boolean,
                                              isVolatile: Boolean,
                                              doCompact: Boolean,
                                              keyOptions: CollectionKeyOptions,
                                              error: Boolean,
                                              code: Int)
    extends ApiResponse
    with ErrorResult

case class RenameCollectionResponse(id: String,
                                    name: String,
                                    isSystem: Boolean,
                                    status: CollectionStatus,
                                    `type`: CollectionType,
                                    error: Boolean,
                                    code: Int)
    extends ApiResponse
    with ErrorResult

case class RotateCollectionJournalResponse(result: Boolean, error: Boolean, code: Int)
    extends DatabaseApiResponse[Boolean]

case class ReadDocumentsResponse(result: Seq[String],
                                 hasMore: Boolean,
                                 cached: Boolean,
                                 extra: ReadDocumentsExtra,
                                 error: Boolean,
                                 code: Int)
    extends DatabaseApiResponse[Seq[String]]

trait DocumentApiResponse extends ApiResponse {
  val _key: String
  val _id: String
  val _rev: String
}

case class DocumentHeaderResponse(_key: String, _id: String, _rev: String) extends DocumentApiResponse

trait DocumentResponse              extends ApiResponse
case class SilentDocumentResponse() extends DocumentResponse

case class CreateDocumentResponse[D](_key: String, _id: String, _rev: String, `new`: Option[D])
    extends DocumentResponse
    with DocumentApiResponse

case class ReplaceDocumentResponse[D](_key: String,
                                      _id: String,
                                      _rev: String,
                                      _oldRev: String,
                                      `new`: Option[D],
                                      old: Option[D])
    extends DocumentResponse
    with DocumentApiResponse

case class UpdateDocumentResponse[D](_key: String,
                                     _id: String,
                                     _rev: String,
                                     _oldRev: String,
                                     `new`: Option[D],
                                     old: Option[D])
    extends DocumentResponse
    with DocumentApiResponse
