package io.tripod.oss.arangodb.driver.http

import java.time.Instant

import io.tripod.oss.arangodb.driver.entities.{CollectionStatus, CollectionType, KeyType}

case class CollectionKeyOptions(allowUserKeys: Option[Boolean] = None,
                                `type`: Option[KeyType] = None,
                                increment: Option[Int],
                                offset: Option[Int])

case class DataFilesFigure(count: Int, fileSize: Int)
case class CompactionStatusFigure(message: String, time: Instant)
case class CompactorsFigure(count: Int, fileSize: Int)
case class DeadFigure(count: Int, deletion: Int, size: Int)
case class IndexFigure(count: Int, size: Int)
case class ReadCacheFigure(count: Int, size: Int)
case class AliveFigure(count: Int, size: Int)
case class JournalFigure(count: Int, fileSize: Int)
case class RevisionsFigure(count: Int, size: Int)

case class CollectionFigure(datafiles: DataFilesFigure,
                            uncollectedLogfileEntries: Int,
                            compactionStatus: CompactionStatusFigure,
                            compactors: CompactorsFigure,
                            dead: DeadFigure,
                            indexes: IndexFigure,
                            readCache: ReadCacheFigure,
                            waitingFor: String,
                            alive: AliveFigure,
                            documentReferences: Int,
                            journals: JournalFigure,
                            lastTick: Int,
                            revisions: RevisionsFigure)

case class CollectionInfo(id: String,
                          name: String,
                          isSystem: Boolean,
                          status: CollectionStatus,
                          `type`: CollectionType)
