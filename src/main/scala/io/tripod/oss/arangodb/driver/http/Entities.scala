package io.tripod.oss.arangodb.driver.http

import java.time.Instant

sealed trait CollectionType
case object DocumentCollection extends CollectionType
case object EdgeCollection     extends CollectionType

sealed trait KeyType
case object AutoIncrementKey extends KeyType
case object TraditionalKey   extends KeyType

case class CollectionKeyOptions(allowUsersKey: Option[Boolean] = None,
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
                            compactionStatusFigure: CompactionStatusFigure,
                            compactors: CompactorsFigure,
                            dead: DeadFigure,
                            indexes: IndexFigure,
                            readcache: ReadCacheFigure,
                            waitingFor: String,
                            alive: AliveFigure,
                            documentReferences: Int,
                            journals: JournalFigure,
                            maxTick: Int,
                            revisions: RevisionsFigure)
