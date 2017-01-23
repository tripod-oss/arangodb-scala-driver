package io.tripod.oss.arangodb.driver.database

import akka.actor.Actor.Receive
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}
import io.tripod.oss.arangodb.driver._
import io.tripod.oss.arangodb.driver.EndpointClientWorker.Enqueue

import scala.concurrent.Promise
import io.circe.generic.auto._

trait DatabaseWorkerBehaviour { this: EndpointClientWorker ⇒
  import de.heikoseeberger.akkahttpcirce.CirceSupport._

  def databaseWorkerBehaviour: Receive = {
    case CurrentDatabase(promise) =>
      enqueue[CurrentDatabaseResponse](HttpMethods.GET,
                                       "/_api/database/current",
                                       promise)
    case UserDatabase(promise) ⇒
      enqueue[DatabaseListResponse](HttpMethods.GET,
                                    "/_api/database/user",
                                    promise)
    case ListDatabase(promise) ⇒
      enqueue[DatabaseListResponse](HttpMethods.GET, "/_api/database", promise)

    case CreateDatabase(dbName, users, promise, extraEncoder) =>
      implicit val encoder = extraEncoder
      Marshal(CreateDatabaseRequest(dbName, users))
        .to[RequestEntity]
        .map(
          request =>
            enqueue[CreateDatabaseResponse](HttpMethods.POST,
                                            "/_api/database",
                                            promise,
                                            Some(request)))
  }
}
