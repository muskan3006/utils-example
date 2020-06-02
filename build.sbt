name := "utilities"

version := "0.1"

scalaVersion := "2.13.2"
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.0-alpha1"
libraryDependencies += "com.typesafe" % "config" % "1.4.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.0"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.5"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.3"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % "2.0.0"
libraryDependencies += "org.elasticsearch" % "elasticsearch" % "7.6.2"
