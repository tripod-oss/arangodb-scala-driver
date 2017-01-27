package io.tripod.oss.arangodb.driver.http

import akka.http.scaladsl.model.headers.CustomHeader

class ArangoAsync extends CustomHeader {
  override def value(): String = "true"

  override def name(): String = "x-arango-async"

  override def renderInResponses(): Boolean = false

  override def renderInRequests(): Boolean = true
}

object ArangoAsync {
  def apply() = new ArangoAsync()
}
