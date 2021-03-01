name          := "approx-smote"
version       := "1.1.0"
organization  := "ubu.admirable"
licenses      := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

scalaVersion  := "2.12.10"

credentials   += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")

//sbt-spark-package
spName          := "mjuez/approx-smote"
sparkVersion    := "3.0.1"

sparkComponents += "mllib"

//include provided dependencies in sbt run task
run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))