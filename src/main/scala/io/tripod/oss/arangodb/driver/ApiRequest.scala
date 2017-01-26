package io.tripod.oss.arangodb.driver

sealed trait ApiRequest

case class DatabaseListRequest() extends ApiRequest
case class ServerVersionRequest(details: Boolean) extends ApiRequest
case class UserCreateOptions[T](username: String,
                                passwd: Option[String] = Some(""),
                                active: Option[Boolean] = Some(true),
                                extra: Option[T] = None)
case class CreateDatabaseRequest[T](name: String, users: Option[Seq[T]])
    extends ApiRequest
