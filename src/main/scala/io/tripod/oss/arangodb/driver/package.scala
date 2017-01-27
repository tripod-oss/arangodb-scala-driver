package io.tripod.oss.arangodb

import io.tripod.oss.arangodb.driver.http.DBContext

/**
  * Created by nicolas.jouanin on 27/01/17.
  */
package object driver {
  implicit val _systemContext: Option[DBContext] = None
}
