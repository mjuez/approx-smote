name          := "asmote-bd"
version       := "0.2-SNAPSHOT"
organization  := "ubu.admirable"
licenses      := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

scalaVersion  := "2.11.12"

//sbt-spark-package
spName          := "admirable/asmote-bd"
sparkVersion    := "2.4.5"

sparkComponents += "mllib"
spDependencies  += "saurfang/spark-knn:0.3.0"

//include provided dependencies in sbt run task
run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

// import Dependencies._

// ThisBuild / scalaVersion     := "2.11.12"
// ThisBuild / version          := "0.2-SNAPSHOT"
// ThisBuild / organization     := "ubu.admirable"
// ThisBuild / organizationName := "admirable"
// ThisBuild / developers := List(
//   Developer(
//     id    = "mariojg",
//     name  = "Mario Juez-Gil",
//     email = "mariojg@ubu.es",
//     url   = url("http://www.mjuez.com")
//   )
// )

// lazy val root = (project in file("."))
//   .settings(
//     name := "asmote-bd",
//     resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven",
//     libraryDependencies += scalaTest % Test,
//     libraryDependencies += saurfangKNN, 
//     libraryDependencies += sparkCore,
//     libraryDependencies += sparkMLlib,
//     libraryDependencies += sbtSparkPackage
//   )
