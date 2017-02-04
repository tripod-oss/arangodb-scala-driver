package io.tripod.oss.arangodb.driver.entities

case class DocumentDeleteInfo[D](key: String, id: String, rev: String, old: Option[D])
