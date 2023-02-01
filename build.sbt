ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "my-akka",
    idePackagePrefix := Some("com.my.sandbox")
  )

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.7.0"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.3.2",
  "org.apache.kafka" % "kafka-streams" % "3.3.2",
  "org.apache.kafka" %% "kafka-streams-scala" % "3.3.2",
  "io.circe" %% "circe-core" % "0.14.3",
  "io.circe" %% "circe-generic" % "0.14.3",
  "io.circe" %% "circe-parser" % "0.14.3"
)
