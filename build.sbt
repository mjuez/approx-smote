name          := "approx-smote"
version       := "1.0.0"
organization  := "ubu.admirable"
licenses      := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

scalaVersion  := "2.11.12"

credentials   += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")

//sbt-spark-package
spName          := "mjuez/approx-smote"
sparkVersion    := "2.4.5"

sparkComponents += "mllib"

//include provided dependencies in sbt run task
run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))
