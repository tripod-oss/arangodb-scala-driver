package io.tripod.oss.arangodb.driver.http

sealed trait ApiRequest

case class ServerVersionRequest(details: Boolean) extends ApiRequest
case class UserCreateOptions[T](username: String,
                                passwd: Option[String] = Some(""),
                                active: Option[Boolean] = Some(true),
                                extra: Option[T] = None)
case class CreateDatabaseRequest[T](name: String, users: Option[Seq[T]]) extends ApiRequest

case class CreateCollectionRequest(name: String,
                                   journalSize: Option[Int] = None,
                                   replicationFactor: Option[Int] = None,
                                   keyOptions: Option[CollectionKeyOptions] = None,
                                   waitForSync: Option[Boolean] = None,
                                   doCompact: Option[Boolean] = None,
                                   isVolatile: Option[Boolean] = None,
                                   shardKeys: Option[String] = None,
                                   numberOfShards: Option[Int] = None,
                                   isSystem: Option[Boolean] = None,
                                   `type`: Option[CollectionType] = None,
                                   indexBuckets: Option[Int] = None)
    extends ApiRequest

case class ChangeCollectionPropertiesRequest(waitForSync: Option[Boolean], journalSize: Option[Int]) extends ApiRequest

case class RenameCollectionRequest(name: String) extends ApiRequest
