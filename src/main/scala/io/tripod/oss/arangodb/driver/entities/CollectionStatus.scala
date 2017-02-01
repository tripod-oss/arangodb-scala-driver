package io.tripod.oss.arangodb.driver.entities

sealed trait CollectionStatus

object CollectionStatus {
  case object NewBornCollection extends CollectionStatus // 1
  case object Unloaded extends CollectionStatus // 2
  case object Loaded extends CollectionStatus // 3
  case object InTheProcessOfBeingUnloaded extends CollectionStatus // 4
  case object Deleted extends CollectionStatus // 5
  case object Loading extends CollectionStatus // 6
}
