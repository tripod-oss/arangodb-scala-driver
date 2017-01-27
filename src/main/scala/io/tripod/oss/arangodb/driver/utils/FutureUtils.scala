package io.tripod.oss.arangodb.driver.utils

import io.tripod.oss.arangodb.driver.http.{ApiError, ApiResponse}

import scala.concurrent.{Future, Promise}

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
object FutureUtils {
  def completeWithPromise[T <: ApiResponse](
      request: Promise[Either[ApiError, T]] ⇒ Unit): Future[Either[ApiError, T]] = {
    val responsePromise = Promise[Either[ApiError, T]]
    request(responsePromise)
    responsePromise.future
  }
}
