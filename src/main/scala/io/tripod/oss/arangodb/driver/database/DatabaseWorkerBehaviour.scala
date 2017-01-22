package io.tripod.oss.arangodb.driver.database

import akka.actor.Actor.Receive
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest}
import io.tripod.oss.arangodb.driver.{CurrentDatabase, EndpointClientWorker}
import io.tripod.oss.arangodb.driver.EndpointClientWorker.Enqueue

trait DatabaseWorkerBehaviour { this: EndpointClientWorker ⇒
  def databaseWorkerBehaviour: Receive = {
    case CurrentDatabase(promise) =>
      self ! Enqueue(
        buildHttpRequest(HttpMethods.GET, "/_api/database/current"),
        promise)
  }
}
