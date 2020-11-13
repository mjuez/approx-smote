name          := "asmote-bd"
version       := "0.3"
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
