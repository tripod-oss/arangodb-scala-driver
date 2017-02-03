package io.tripod.oss.arangodb.driver

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.circe.Encoder
import io.tripod.oss.arangodb.driver
import io.tripod.oss.arangodb.driver.http._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, Matchers, WordSpec}

import scala.concurrent.Future

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
class ArangoDriverSpec extends WordSpec with Matchers with ScalaFutures with EitherValues with IntegrationPatience {
  implicit val system = ActorSystem("ArangoDriverSpec")
  implicit val ec     = system.dispatcher

  "ArangodbDriver" should {
    val driver = ArangoDriver()
    "get server version" in {
      val result = driver.getServerVersion(true).futureValue
      result.server should not be empty
      result.version should not be empty
      result.license should not be empty
    }

    "get user database list" in {
      val result = driver.userDatabase.futureValue
      result.error shouldEqual false
      result.result should not be empty
    }

    "get current database" in {
      val result = driver.currentDatabase.futureValue
      result.error shouldEqual false
    }

    "get database list" in {
      val result = driver.databaseList.futureValue
      result.error shouldEqual false
      result.result should not be empty
    }

    "create database" in {
      val result = driver.createDatabase("testDB").futureValue
      result.result shouldEqual true
      driver.removeDatabase("testDB").futureValue
    }

    "create database with extra" in {
      val users = Seq(
        UserCreateOptions("testUser", Some("testPassword"), extra = Some("ExtraString"))
      )
      val result =
        driver.createDatabase("testDBWithExtra", Some(users)).futureValue
      result.result shouldEqual true
      driver.removeDatabase("testDBWithExtra").futureValue
    }

    "remove database" in {
      val future = for {
        _         ← driver.createDatabase("removeDB")
        resFuture ← driver.removeDatabase("removeDB")
      } yield resFuture

      val result = future.futureValue
      result.result shouldEqual true
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "create collection" in {
      val future = for {
        _         ← driver.createCollection("testCollection")
        resFuture ← driver.dropCollection("testCollection")
      } yield resFuture

      val result = future.futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "create collection with options" in {
      val future = for {
        _         ← driver.createCollection("testCollectionWithOptions", waitForSync = Some(true))
        resFuture ← driver.dropCollection("testCollectionWithOptions")
      } yield resFuture

      val result = future.futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "truncate collection" in {
      val future = for {
        _         ← driver.createCollection("testTruncate")
        _         ← driver.truncateCollection("testTruncate")
        resFuture ← driver.dropCollection("testTruncate")
      } yield resFuture

      val result = future.futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "get collection" in {
      val result = driver
        .createCollection("testGetCollection")
        .flatMap { _ ⇒
          driver.getCollection("testGetCollection").flatMap { _ =>
            driver.dropCollection("testGetCollection")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "get collection properties" in {
      val result = driver
        .createCollection("testGetCollectionProperties")
        .flatMap { _ ⇒
          driver.getCollectionProperties("testGetCollectionProperties").flatMap { _ =>
            driver.dropCollection("testGetCollectionProperties")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "get collection count" in {
      val result = driver
        .createCollection("testGetCollectionCount")
        .flatMap { _ ⇒
          driver.getCollectionCount("testGetCollectionCount").flatMap { _ =>
            driver.dropCollection("testGetCollectionCount")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "get collection figures" in {
      val result = driver
        .createCollection("testGetCollectionFigures")
        .flatMap { _ ⇒
          driver.getCollectionFigures("testGetCollectionFigures").flatMap { _ =>
            driver.dropCollection("testGetCollectionFigures")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "get collection revision" in {
      val result = driver
        .createCollection("testGetCollectionRevision")
        .flatMap { _ ⇒
          driver.getCollectionRevision("testGetCollectionRevision").flatMap { _ =>
            driver.dropCollection("testGetCollectionRevision")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "get collection checksum" in {
      val result = driver
        .createCollection("testGetCollectionChecksum")
        .flatMap { _ ⇒
          driver.getCollectionChecksum("testGetCollectionChecksum").flatMap { _ =>
            driver.dropCollection("testGetCollectionChecksum")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }
    "get collections" in {
      val result = driver.getCollections.futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }
    "load / unload collection" in {
      val result = driver
        .createCollection("testCollectionLoadUnload")
        .flatMap { _ ⇒
          driver.unloadCollection("testCollectionLoadUnload").flatMap { _ ⇒
            driver.loadCollection("testCollectionLoadUnload").flatMap { _ =>
              driver.dropCollection("testCollectionLoadUnload")
            }
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }
    "change collection properties" in {
      val result = driver
        .createCollection("testChangeCollectionProperties")
        .flatMap { _ ⇒
          driver.changeCollectionProperties("testChangeCollectionProperties", Some(true)).flatMap { _ =>
            driver.dropCollection("testChangeCollectionProperties")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }
    "rename collection properties" in {
      val result = driver
        .createCollection("testRenameCollection")
        .flatMap { _ ⇒
          driver.renameCollection("testRenameCollection", "newCollectionName").flatMap { _ =>
            driver.dropCollection("newCollectionName")
          }
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }
    "rotate collection journal" in {
      val result = driver
        .createCollection("testCollectionRotate")
        .flatMap { _ ⇒
          driver.rotateCollectionJournal("testCollectionRotate").onComplete {
            case _ ⇒ ()
          }
          driver.dropCollection("testCollectionRotate")
        }
        .futureValue
      result.error shouldEqual false
      result.code shouldEqual 200
    }

    "create document" in {
      import io.circe.generic.auto._
      case class Document(hello: String)
      val createDocument = for {
        _        <- driver.createCollection("testCollectionDocument")
        document <- driver.createDocument("testCollectionDocument", Document("world"))
        _        <- driver.dropCollection("testCollectionDocument")
      } yield document

      val result = createDocument.futureValue
      result shouldBe a[CreateDocumentResponse[_]]
      val docResp = result.asInstanceOf[CreateDocumentResponse[_]]
      docResp._key should not be empty
      docResp._id should not be empty
      docResp._rev should not be empty
    }

    "create silent document" in {
      import io.circe.generic.auto._
      case class Document(hello: String)
      val createDocument = for {
        _        <- driver.createCollection("testCollectionDocument")
        document <- driver.createDocument("testCollectionDocument", Document("world"), silent = Some(true))
        _        <- driver.dropCollection("testCollectionDocument")
      } yield document

      val result = createDocument.futureValue
      result shouldBe a[SilentDocumentResponse]
    }

    "create document with returnNew" in {
      import io.circe.generic.auto._
      case class Document(hello: String)
      val createDocument = for {
        _        <- driver.createCollection("testCollectionDocument")
        document <- driver.createDocument("testCollectionDocument", Document("world"), returnNew = Some(true))
        _        <- driver.dropCollection("testCollectionDocument")
      } yield document

      val result = createDocument.futureValue
      result shouldBe a[CreateDocumentResponse[Document]]
      val docResp = result.asInstanceOf[CreateDocumentResponse[Document]]
      docResp.`new` shouldEqual Some(Document("world"))
    }

    "replace document" in {
      import io.circe.generic.auto._
      case class Document(hello: String)
      val replaceDocument = for {
        _        <- driver.createCollection("testReplaceDocument")
        document <- driver.createDocument("testReplaceDocument", Document("world"))
        replacement ← driver.replaceDocument("testReplaceDocument",
                                             document.asInstanceOf[CreateDocumentResponse[Document]]._key,
                                             Document("everybody"))
        _ <- driver.dropCollection("testReplaceDocument")
      } yield replacement

      val result = replaceDocument.futureValue
      result shouldBe a[ReplaceDocumentResponse[_]]
      val docResp = result.asInstanceOf[ReplaceDocumentResponse[_]]
      docResp._key should not be empty
      docResp._id should not be empty
      docResp._rev should not be empty
      docResp._oldRev should not be empty
    }

    "update document" in {
      import io.circe.generic.auto._
      val updateDocument = for {
        _        <- driver.createCollection("testUpdateDocument")
        document <- driver.createDocument("testUpdateDocument", Map("hello" → "world"))
        update ← driver.updateDocument("testUpdateDocument",
                                       document.asInstanceOf[CreateDocumentResponse[Map[String, String]]]._key,
                                       Map("john" → "snow"),
                                       returnNew = Some(true))
        _ <- driver.dropCollection("testUpdateDocument")
      } yield update

      val result = updateDocument.futureValue
      result shouldBe a[UpdateDocumentResponse[_]]
      val docResp = result.asInstanceOf[UpdateDocumentResponse[_]]
      docResp._key should not be empty
      docResp._id should not be empty
      docResp._rev should not be empty
      docResp.`new` match {
        case Some(map: Map[_, _]) ⇒
          map should contain("hello" → "world")
          map should contain("john"  → "snow")
        case _ ⇒ fail()
      }
    }

    "delete document" in {
      import io.circe.generic.auto._
      case class Document(hello: String)
      val replaceDocument = for {
        _        <- driver.createCollection("testDeleteDocument")
        document <- driver.createDocument("testDeleteDocument", Document("world"))
        replacement ← driver.deleteDocument("testDeleteDocument",
                                            document.asInstanceOf[CreateDocumentResponse[Document]]._key)
        _ <- driver.dropCollection("testDeleteDocument")
      } yield replacement

      val result = replaceDocument.futureValue
      result shouldBe a[DeleteDocumentResponse[_]]
      val docResp = result.asInstanceOf[DeleteDocumentResponse[_]]
      docResp._key should not be empty
      docResp._id should not be empty
      docResp._rev should not be empty
    }

  }
}
