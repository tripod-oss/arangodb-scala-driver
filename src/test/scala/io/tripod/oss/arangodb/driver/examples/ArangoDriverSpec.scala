package io.tripod.oss.arangodb.driver.examples

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import io.circe.Encoder
import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.concurrent.Future
import io.tripod.oss.arangodb.driver.http._
import io.tripod.oss.arangodb.driver._systemContext

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
  implicit val system       = ActorSystem("PbfStreamFlowSpec")
  implicit val materializer = ActorMaterializer()
  implicit val ec           = system.dispatcher

  "ArangodbDriver" should {
    val driver = ArangoDriver()
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

    "get current database" in {
      val result = driver.currentDatabase.futureValue
      result.right.value.error shouldEqual false
      logger.debug(result.right.value.toString)
    }

    "get database list" in {
      val result = driver.databaseList.futureValue
      result.right.value.error shouldEqual false
      result.right.value.result should not be empty
      logger.debug(result.right.value.toString)
    }

    "create database" in {
      implicit val noneExtraEncoder = Encoder.encodeNone
      val result                    = driver.createDatabase("testDB").futureValue
      result.right.value shouldEqual true
      logger.debug(result.right.value.toString)
      driver.removeDatabase("testDB").futureValue
    }

    "create database with extra" in {
      val users = Seq(
        UserCreateOptions("testUser", Some("testPassword"), extra = Some("ExtraString"))
      )
      val result =
        driver.createDatabase("testDBWithExtra", Some(users)).futureValue
      result.right.value shouldEqual true
      logger.debug(result.right.value.toString)
      driver.removeDatabase("testDBWithExtra").futureValue
    }

    "remove database" in {
      val result = driver
        .createDatabase("removeDB")
        .flatMap {
          case Right(true) => driver.removeDatabase("removeDB")
          case Left(x)     => Future.successful(Left(x))
        }
        .futureValue
      result.right.value.result shouldEqual true
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "create collection" in {
      //implicit val dbContext: Option[DBContext] = None

      val result = driver
        .createCollection("testCollection")
        .flatMap {
          case Right(_) => driver.dropCollection("testCollection")
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "create collection with options" in {
      val result = driver
        .createCollection("testCollectionWithOptions", waitForSync = Some(true))
        .flatMap {
          case Right(_) => driver.dropCollection("testCollectionWithOptions")
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "truncate collection" in {
      val result = driver
        .createCollection("testTruncate")
        .flatMap {
          case Right(_) =>
            driver.truncateCollection("testTruncate").flatMap {
              case Right(_) => driver.dropCollection("testTruncate")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "get collection" in {
      val result = driver
        .createCollection("testGetCollection")
        .flatMap {
          case Right(_) =>
            driver.getCollection("testGetCollection").flatMap {
              case Right(_) => driver.dropCollection("testGetCollection")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "get collection properties" in {
      val result = driver
        .createCollection("testGetCollectionProperties")
        .flatMap {
          case Right(_) =>
            driver.getCollectionProperties("testGetCollectionProperties").flatMap {
              case Right(_) => driver.dropCollection("testGetCollectionProperties")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "get collection count" in {
      val result = driver
        .createCollection("testGetCollectionCount")
        .flatMap {
          case Right(_) =>
            driver.getCollectionCount("testGetCollectionCount").flatMap {
              case Right(_) => driver.dropCollection("testGetCollectionCount")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "get collection figures" in {
      val result = driver
        .createCollection("testGetCollectionFigures")
        .flatMap {
          case Right(_) =>
            driver.getCollectionFigures("testGetCollectionFigures").flatMap {
              case Right(_) => driver.dropCollection("testGetCollectionFigures")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "get collection revision" in {
      val result = driver
        .createCollection("testGetCollectionRevision")
        .flatMap {
          case Right(_) =>
            driver.getCollectionRevision("testGetCollectionRevision").flatMap {
              case Right(_) => driver.dropCollection("testGetCollectionRevision")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

    "get collection checksum" in {
      val result = driver
        .createCollection("testGetCollectionChecksum")
        .flatMap {
          case Right(_) =>
            driver.getCollectionChecksum("testGetCollectionChecksum").flatMap {
              case Right(_) => driver.dropCollection("testGetCollectionChecksum")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }
    "get collections" in {
      val result = driver.getCollections.futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }
    "load / unload collection" in {
      val result = driver
        .createCollection("testCollectionLoadUnload")
        .flatMap {
          case Right(_) =>
            driver.unloadCollection("testCollectionLoadUnload").flatMap {
              case Right(_) =>
                driver.loadCollection("testCollectionLoadUnload").flatMap {
                  case Right(_) => driver.dropCollection("testCollectionLoadUnload")
                }
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }
    "change collection properties" in {
      val result = driver
        .createCollection("testChangeCollectionProperties")
        .flatMap {
          case Right(_) =>
            driver.changeCollectionProperties("testChangeCollectionProperties", Some(true)).flatMap {
              case Right(_) => driver.dropCollection("testChangeCollectionProperties")
            }
        }
        .futureValue
      result.right.value.error shouldEqual false
      result.right.value.code shouldEqual 200
      logger.debug(result.right.value.toString)
    }

  }
}
