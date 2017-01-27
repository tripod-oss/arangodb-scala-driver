package io.tripod.oss.arangodb.driver

import akka.actor.ActorRef
import io.tripod.oss.arangodb.driver.http.{ArangoDriver, DBContext}
import io.tripod.oss.arangodb.driver.utils.FutureUtils._

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
class ArangoDatabase(dbName: String)(implicit driver: ArangoDriver) {
  implicit val dbContext = DBContext(dbName)

}
