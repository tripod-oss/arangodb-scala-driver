package io.tripod.oss.arangodb.driver.database

import akka.actor.Actor.Receive
import akka.http.scaladsl.model.{
  HttpEntity,
  HttpMethod,
  HttpMethods,
  HttpRequest
}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import io.circe.{Decoder, DecodingFailure, HCursor}
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
      enqueue[UserDatabaseResponse](HttpMethods.GET,
                                    "/_api/database/user",
                                    promise)
  }
}
