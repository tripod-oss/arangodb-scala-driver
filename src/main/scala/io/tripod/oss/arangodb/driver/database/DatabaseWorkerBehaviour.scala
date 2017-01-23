package io.tripod.oss.arangodb.driver.database

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import io.tripod.oss.arangodb.driver._

import io.circe.generic.auto._

trait DatabaseWorkerBehaviour { this: EndpointClientWorker ⇒
  import de.heikoseeberger.akkahttpcirce.CirceSupport._

  def databaseWorkerBehaviour: Receive = {
    ???
    /*
    case CurrentDatabase(promise, dbContext) =>
      enqueue(HttpMethods.GET,
              buildUri("/_api/database/current", dbContext),
              promise)
    case UserDatabase(promise, dbContext) ⇒
      enqueue(HttpMethods.GET,
              buildUri("/_api/database/user", dbContext),
              promise)
    case ListDatabase(promise, dbContext) ⇒
      enqueue(HttpMethods.GET, buildUri("/_api/database", dbContext), promise)
    case CreateDatabase(dbName, users, promise, extraEncoder, dbContext) =>
      implicit val encoder = extraEncoder
      Marshal(CreateDatabaseRequest(dbName, users))
        .to[RequestEntity]
        .map(
          request =>
            enqueue(HttpMethods.POST,
                    buildUri("/_api/database", dbContext),
                    promise,
                    Some(request)))
    case RemoveDatabase(dbName, promise, dbContext) ⇒
      enqueue(HttpMethods.DELETE,
              buildUri(s"/_api/database/$dbName", dbContext),
              promise)
   */
  }
}
