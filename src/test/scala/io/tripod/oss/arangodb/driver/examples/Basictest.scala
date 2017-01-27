package io.tripod.oss.arangodb.driver.examples

import com.typesafe.config.ConfigFactory
import io.tripod.oss.arangodb.driver.http.{ArangoDriver, DatabaseApi}

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object Basictest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val driver = ArangoDriver("root", "root", ConfigFactory.load())
  driver.addEndPoint("http://10.156.223.115:8529/")
  Thread.sleep(500)
  driver.getServerVersion(true).map(println)
  //driver.currentDatabase.map(println)
  driver.userDatabase.map(println)
  Thread.sleep(10000)
  driver.close
}
