import Dependencies._

ThisBuild / scalaVersion     := "2.11.12"
ThisBuild / version          := "0.1-SNAPSHOT"
ThisBuild / organization     := "ubu.admirable"
ThisBuild / organizationName := "admirable"
ThisBuild / developers := List(
  Developer(
    id    = "mariojg",
    name  = "Mario Juez-Gil",
    email = "mariojg@ubu.es",
    url   = url("http://www.mjuez.com")
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "asmote-bd",
    resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += saurfangKNN, 
    libraryDependencies += sparkCore,
    libraryDependencies += sparkMLlib
  )
