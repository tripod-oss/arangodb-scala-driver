organization := "io.tripod"
name := "arangodb-scala-driver"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
startYear := Some(2016)
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

scalaVersion := "2.12.1"
crossScalaVersions := Seq(scalaVersion.value, "2.11.8", "2.12.1")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.16",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.8",
  "de.heikoseeberger" %% "akka-http-circe" % "1.11.0"
)
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.6.1")
