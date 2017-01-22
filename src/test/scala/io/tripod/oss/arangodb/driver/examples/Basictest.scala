package io.tripod.oss.arangodb.driver.examples

import com.typesafe.config.ConfigFactory
import io.tripod.oss.arangodb.driver.ArangoDriver
import io.tripod.oss.arangodb.driver.database.DatabaseApi

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object Basictest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val driver = new ArangoDriver(ConfigFactory.load(), Some("root"), Some(""))
  with DatabaseApi
  driver.addEndPoint("http://localhost:8529/")
  Thread.sleep(500)
  driver.getServerVersion(true).map(println)
  driver.currentDatabase.map(println)
  Thread.sleep(10000)
  driver.close
}
