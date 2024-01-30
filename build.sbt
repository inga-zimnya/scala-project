ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "project_qa"
  )

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "0.23.18",
  "org.http4s" %% "http4s-blaze-server" % "0.23.14",
  "org.typelevel" %% "cats-core" % "2.9.0",
  "org.slf4j" % "slf4j-nop" % "2.0.5",
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "org.typelevel" %% "cats-effect" % "3.5.2",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.9.0",
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.9.8",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.4.0",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.2.10"
)