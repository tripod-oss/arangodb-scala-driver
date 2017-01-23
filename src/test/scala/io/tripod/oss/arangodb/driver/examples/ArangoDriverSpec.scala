package io.tripod.oss.arangodb.driver.examples

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, Encoder, Json}
import io.tripod.oss.arangodb.driver.database.driver.ArangoDriver
import io.tripod.oss.arangodb.driver.{ApiError, ServerVersionResponse}
import io.tripod.oss.arangodb.driver.database.{DatabaseApi, UserCreateOptions}
import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import shapeless.Lazy

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
class ArangoDriverSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with EitherValues
    with IntegrationPatience
    with LazyLogging {
  implicit val system = ActorSystem("PbfStreamFlowSpec")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  "ArangodbDriver" should {
    val driver = new ArangoDriver() with DatabaseApi
    "get server version" in {
      val result = driver.getServerVersion(true).futureValue
      result.right.value.server should not be empty
      result.right.value.version should not be empty
      result.right.value.license should not be empty
      logger.debug(result.right.value.toString)
    }
    "get user database list" in {
      val result = driver.userDatabase.futureValue
      result.right.value.error shouldEqual false
      result.right.value.result should not be empty
      logger.debug(result.right.value.toString)
    }
    "get database list" in {
      val result = driver.databaseList.futureValue
      result.right.value.error shouldEqual false
      result.right.value.result should not be empty
      logger.debug(result.right.value.toString)
    }
    "get current database" in {
      val result = driver.databaseList.futureValue
      result.right.value.error shouldEqual false
      result.right.value.result should not be empty
      logger.debug(result.right.value.toString)
    }
    "create database" in {
      import io.tripod.oss.arangodb.driver.database.Implicits._
      val result = driver.createDatabase("testDB").futureValue
      result.right.value shouldEqual true
      logger.debug(result.right.value.toString)
      driver.removeDatabase("testDB").futureValue
    }
    "create database with extra" in {
      val users = Seq(
        UserCreateOptions("testUser",
                          Some("testPassword"),
                          extra = Some("ExtraString"))
      )
      val result =
        driver.createDatabase("testDBWithExtra", Some(users)).futureValue
      result.right.value shouldEqual true
      logger.debug(result.right.value.toString)
      driver.removeDatabase("testDBWithExtra").futureValue
    }
    "remove database" in {
      import io.tripod.oss.arangodb.driver.database.Implicits._
      val result = driver
        .createDatabase("removeDB")
        .flatMap {
          case Right(true) => driver.removeDatabase("removeDB")
          case Left(x) => Future.successful(Left(x))
        }
        .futureValue
      result.right.value.result shouldEqual true
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }
  }
}
