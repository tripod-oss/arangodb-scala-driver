package io.tripod.oss.arangodb.driver.entities

sealed trait CollectionType
case object DocumentCollection extends CollectionType
case object EdgeCollection extends CollectionType
