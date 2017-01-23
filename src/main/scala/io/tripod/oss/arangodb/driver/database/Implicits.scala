package io.tripod.oss.arangodb.driver.database

import io.circe.Encoder

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
object Implicits {
  implicit val noneExtraEncoder = Encoder.encodeNone

}
