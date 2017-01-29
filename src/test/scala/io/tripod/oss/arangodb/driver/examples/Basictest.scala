package io.tripod.oss.arangodb.driver.examples

import com.typesafe.config.ConfigFactory
import io.tripod.oss.arangodb.driver.{ArangoDatabase, ArangoDriver}
import io.tripod.oss.arangodb.driver._

import scala.util.{Failure, Success}

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object Basictest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val driver = ArangoDriver("", "", ConfigFactory.load())
  driver.addEndPoint("http://10.156.223.115:8529/")
  Thread.sleep(500)
  ArangoDatabase.create("db").onComplete {
    case Success(res)             ⇒ println(res)
    case Failure(t: ApiException) ⇒ println(t.errorMessage)
  }
  ArangoDatabase.create("db").onComplete {
    case Success(res)          ⇒ println(res)
    case Failure(t: Throwable) ⇒ println(t)
  }
  Thread.sleep(10000)
  driver.close
}
