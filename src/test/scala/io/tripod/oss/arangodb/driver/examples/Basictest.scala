package io.tripod.oss.arangodb.driver.examples

import io.tripod.oss.arangodb.driver.ArangoDriver

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object Basictest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val driver = ArangoDriver(username = "root", password = "")
  driver.addEndPoint("http://localhost:8529/")
  Thread.sleep(500)
  driver.getServerVersion(true).map(println)
  //driver.getEndPoints.onComplete(println)
  Thread.sleep(10000)
  driver.close
}
