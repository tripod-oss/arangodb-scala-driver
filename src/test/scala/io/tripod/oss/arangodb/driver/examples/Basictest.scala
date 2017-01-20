package io.tripod.oss.arangodb.driver.examples

import io.tripod.oss.arangodb.driver.ArangoDriver

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object Basictest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val driver = ArangoDriver()
  driver.addEndPoint("http://localhost:5439/")
  driver.getEndPoints.onComplete(println)
  driver.removeEndPoint("")
  driver.close
}
