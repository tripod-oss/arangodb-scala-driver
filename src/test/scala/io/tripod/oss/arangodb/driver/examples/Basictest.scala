package io.tripod.oss.arangodb.driver.examples

import com.typesafe.config.ConfigFactory
import io.tripod.oss.arangodb.driver.{ArangoDatabase, ArangoDriver}
import io.tripod.oss.arangodb.driver._

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object Basictest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val driver = ArangoDriver("", "", ConfigFactory.load())
  driver.addEndPoint("http://10.156.223.115:8529/")
  Thread.sleep(500)
  ArangoDatabase.create("db").map(println)
  ArangoDatabase.create("db").map(println)
  Thread.sleep(10000)
  driver.close
}
