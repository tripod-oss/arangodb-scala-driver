package io.tripod.oss.arangodb.driver.http

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveDecoder

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

  implicit val collectionTypeEncoder: Encoder[CollectionType] = Encoder.encodeInt.contramap[CollectionType] {
    case DocumentCollection => 2
    case EdgeCollection     => 3
  }

  implicit val collectionTypeDecoder: Decoder[CollectionType] = Decoder.decodeInt.emap[CollectionType] {
    case 2 => Right(DocumentCollection)
    case 3 => Right(EdgeCollection)
    case _ => Left("CollectionType")
  }

  implicit val keyTypeEncoder: Encoder[KeyType] = Encoder.encodeString.contramap[KeyType] {
    case TraditionalKey   => "traditional"
    case AutoIncrementKey => "autoincrement"
  }

  implicit val keyTypeDecoder: Decoder[KeyType] = Decoder.decodeString.emap[KeyType] {
    case "traditional"   => Right(TraditionalKey)
    case "autoincrement" => Right(AutoIncrementKey)
    case _               => Left("KeyType")
  }

  implicit val collectionStatusEncoder: Encoder[CollectionStatus] = Encoder.encodeInt.contramap[CollectionStatus] {
    case NewBornCollection           => 1
    case Unloaded                    => 2
    case Loaded                      => 3
    case InTheProcessOfBeingUnloaded => 4
    case Deleted                     => 5
  }

  implicit val collectionStatusDecoder: Decoder[CollectionStatus] = Decoder.decodeInt.emap[CollectionStatus] {
    case 1 => Right(NewBornCollection)
    case 2 => Right(Unloaded)
    case 3 => Right(Loaded)
    case 4 => Right(InTheProcessOfBeingUnloaded)
    case 5 => Right(Deleted)
    case _ => Left("CollectionStatus")
  }
}
