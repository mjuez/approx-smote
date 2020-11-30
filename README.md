# Approximated SMOTE for Big Data under Apache Spark framework

This repository contains an approximated SMOTE (Approx-SMOTE) implementation for Apache Spark framework. It uses [saurfang's spark-knn](https://github.com/saurfang/spark-knn) for efficient aproximated neighbors search. This approach outperformed other existing SMOTE-based approaches for Apache Spark maintaining their advantages for some classification tasks.

Approx-SMOTE was implemented in **Scala 2.11** for **Apache Spark 2.4.5**.

## Authors

- Mario Juez-Gil <<mariojg@ubu.es>>
- Álvar Arnaiz-González
- Juan J. Rodríguez
- César García-Osorio

**Affiliation:**\
Departamento de Ingeniería Informática\
Universidad de Burgos\

## Installation

Approx-SMOTE is available on SparkPackages.

It can be installed as follows:

- **spark-shell**, **pyspark**, or **spark-submit**:\
```bash
> $SPARK_HOME/bin/spark-shell --packages mjuez:approx-smote:1.0.0
```
- **sbt**:\
```scala
resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

libraryDependencies += "mjuez" % "approx-smote" % "1.0.0"
```
- **Maven**:\
```xml
<dependencies>
  <!-- list of dependencies -->
  <dependency>
    <groupId>mjuez</groupId>
    <artifactId>approx-smote</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
<repositories>
  <!-- list of other repositories -->
  <repository>
    <id>SparkPackagesRepo</id>
    <url>http://dl.bintray.com/spark-packages/maven</url>
  </repository>
</repositories>
```


