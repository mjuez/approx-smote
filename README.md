# Approximated SMOTE for Big Data under Apache Spark framework

This repository contains an approximated SMOTE (Approx-SMOTE) implementation for Apache Spark framework. It uses [saurfang's spark-knn](https://github.com/saurfang/spark-knn) for efficient aproximated neighbors search. This approach outperformed other existing SMOTE-based approaches for Apache Spark maintaining their advantages for some classification tasks.

SMOTE, or synthetic minority oversampling technique, creates synthetic instances belonging to the minority class of binary imbalanced classification problems. This could help overcoming biased classification issues provoked by the imbalance present in some datasets.

Approx-SMOTE was implemented in **Scala 2.11** for **Apache Spark 2.4.5**.

## Authors

- Mario Juez-Gil <<mariojg@ubu.es>>
- Álvar Arnaiz-González
- Juan J. Rodríguez
- César García-Osorio

**Affiliation:**\
Departamento de Ingeniería Informática\
Universidad de Burgos

## Installation

Approx-SMOTE is available on SparkPackages.

It can be installed as follows:

- **spark-shell**, **pyspark**, or **spark-submit**:
```bash
> $SPARK_HOME/bin/spark-shell --packages mjuez:approx-smote:1.0.0
```
- **sbt**:
```scala
resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

libraryDependencies += "mjuez" % "approx-smote" % "1.0.0"
```
- **Maven**:
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

## Basic Usage

Approx-SMOTE is a Spark [Transformer](https://spark.apache.org/docs/latest/ml-pipeline.html#transformers). It has a single public method (`transform()`) for transforming one imbalanced binary classification Dataset into a resampled Dataframe with a different imbalance ratio.

Following the Spark MLlib API, the configuration of Approx-SMOTE is done through [Parameters](https://spark.apache.org/docs/latest/ml-pipeline.html#parameters). The following parameters could be set:

- `k`: The number of nearest neighbors to find (default: 5).
- `percOver`: Oversampling percentage. The number of synthetic instances to be created, as a percentage of the number of instances belonging to the minority class (default: 100, i.e., the number of minority samples is doubled).
- `topTreeSize`: Number of instances used at the top level of hybrid spill trees used by saurfang's approximated *k*-NN (default: `num_minority_examples/500`)
- `seed`: A random seed for experiments repeatability.
- `labelCol`: The name of the column containing instance label (default: `label`).
- `featuresCol`: The name of the column containing instance features (default: `features`).

This example shows how to rebalance one dataset doubling the number of instances belonging to the minority class:

```scala
import org.apache.spark.ml.instance.ASMOTE

// read dataset
val ds = session.read
                .format("libsvm")
                .option("inferSchema", "true")
                .load("binary.libsvm")

// using ASMOTE
val asmote = new ASMOTE().setK(5)
                         .setPercOver(100)
                         .setSeed(46)
val newDF = asmote.transform(ds) // oversampled DataFrame
```
