package io.tripod.oss.arangodb.driver.entities

/**
  * Created by nicolas.jouanin on 30/01/17.
  */
sealed trait ReadDocumentsRequestType

object ReadDocumentsRequestType {
  case object Id   extends ReadDocumentsRequestType
  case object Key  extends ReadDocumentsRequestType
  case object Path extends ReadDocumentsRequestType
}
