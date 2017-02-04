logLevel := Level.Warn

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.21"

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.4")
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
