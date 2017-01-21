package io.tripod.oss.arangodb.driver

import scala.concurrent.Promise

private[driver] sealed trait WorkMessage {
  def resultPromise: Promise[Either[Error, ApiResponse]]
}

private[driver] case class GetServerVersion(
    withDetails: Boolean,
    resultPromise: Promise[Either[Error, ApiResponse]])
    extends WorkMessage
