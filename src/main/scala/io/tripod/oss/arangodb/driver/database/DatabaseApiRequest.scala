package io.tripod.oss.arangodb.driver.database

/**
  * Created by nicolas.jouanin on 23/01/17.
  */
trait DatabaseApiRequest

case class UserCreateOptions[T](username: String,
                                passwd: Option[String] = Some(""),
                                active: Option[Boolean] = Some(true),
                                extra: Option[T] = None)
case class CreateDatabaseRequest[T](name: String, users: Option[Seq[T]])
    extends DatabaseApiRequest
