package io.tripod.oss.arangodb.driver.entities

case class CollectionInfo(id: String,
                          name: String,
                          waitForSync: Boolean,
                          journalSize: Int,
                          isVolatile: Boolean,
                          isSystem: Boolean,
                          status: CollectionStatus,
                          `type`: CollectionType)

