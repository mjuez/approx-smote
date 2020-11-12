import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val sparkCore = "org.apache.spark" %% "spark-core" % "2.4.5"
  lazy val sparkMLlib = "org.apache.spark" %% "spark-mllib" % "2.4.5"
  lazy val saurfangKNN = "saurfang" % "spark-knn" % "0.3.0"
}
