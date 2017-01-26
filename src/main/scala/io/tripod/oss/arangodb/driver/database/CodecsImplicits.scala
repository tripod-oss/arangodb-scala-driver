package io.tripod.oss.arangodb.driver.database

import io.circe.Encoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.tripod.oss.arangodb.driver.{
  CreateDatabaseRequest,
  ServerVersionRequest,
  ServerVersionResponse,
  UserCreateOptions
}

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
object CodecsImplicits {
  implicit val noneExtraEncoder = Encoder.encodeNone

  implicit val databaseListResponseDecoder          = deriveDecoder[DatabaseListResponse]
  implicit val currentDatabaseResponseResultDecoder = deriveDecoder[CurrentDatabaseResponseResult]
  implicit val currentDatabaseResponseDecoder       = deriveDecoder[CurrentDatabaseResponse]
  implicit val createDatabaseResponseDecoder        = deriveDecoder[CreateDatabaseResponse]
  implicit val ServerVersionDecoder                 = deriveDecoder[ServerVersionResponse]
}
