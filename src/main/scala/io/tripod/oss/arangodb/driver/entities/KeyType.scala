package io.tripod.oss.arangodb.driver.entities

sealed trait KeyType
case object AutoIncrementKey extends KeyType
case object TraditionalKey extends KeyType
