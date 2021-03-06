logLevel := Level.Warn

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.21"

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.4")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
