val sparkVersion = "3.1.1"

lazy val root = project
    .withId("approx-smote")
    .in(file("."))
    .settings(
        name          := "approx-smote",
        organization  := "ubu.admirable",
        scalaVersion  := "2.12.10",
        version       := "1.1.2",
        licenses      := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
        resolvers     += "Spark Packages Repo" at "https://repos.spark-packages.org",
        libraryDependencies ++= Seq(
            "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
        )
    )