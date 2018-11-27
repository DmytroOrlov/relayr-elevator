import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.dmytroorlov",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "elevator",
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )
