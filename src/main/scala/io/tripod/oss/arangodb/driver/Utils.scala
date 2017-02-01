package io.tripod.oss.arangodb.driver

object Utils {
  def zipParams(paramNames: Seq[String], paramValues: Seq[Option[_]]) = {
    paramValues
      .zip(paramNames)
      .map {
        case (Some(value), param) => s"$param=$value"
        case _                    => ""
      }
      .filterNot(_.isEmpty)
      .mkString("&") match {
      case "" => ""
      case p  => "?" + p
    }
  }
}
