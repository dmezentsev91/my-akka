ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "my-akka"
//    idePackagePrefix := Some("com.my.sandbox")
  )

val KafkaVersion = "3.3.2"
val AkkaVersion = "2.7.0"
val CirceVersion = "0.14.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.1.0"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % KafkaVersion,
  "org.apache.kafka" % "kafka-streams" % KafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % KafkaVersion,
  "io.circe" %% "circe-core" % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-parser" % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceVersion
)
