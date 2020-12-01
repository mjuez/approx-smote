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

The following figure shows an example of how Approx-SMOTE resamples a bivariate synthetic dataset consisting of 7,000 instances: 6,000 belongs to the majority class and 1,000 to the minority class:

<img src="https://github.com/mjuez/assets/blob/main/approx-smote/smote_levels.jpg" width="100%">

## Experiments

Some experiments have been performed to prove algorithm's suitability for Big Data environments (execution speed and scalability), and to prove that using approximated SMOTE is equivalent to using traditional SMOTE approaches based on exact neighbor search. This equivalence has been proved using the resampled datasets for binary classification tasks, verifying that classification performance is the same or almost the same for both approaches.

The traditional SMOTE implementation chosen was [SMOTE-BD](https://github.com/majobasgall/smote-bd).

The experiments were launched on Google Cloud clusters composed of one master node (8 vCPU and 52 GB memory) and five different worker nodes configurations: **2, 4, 6, 8, and 10**.
Each worker node had 2 vCPU a 7.5 GB memory.
Thus, the biggest cluster configuration (1 master and 10 workers) had 28 vCPUs and 127 GB memory.
For comparing execution times all cluster configurations have been used.
The classification performance comparison was executed on the biggest one, using 2-fold stratified cross validation repeated 5 times.
[SUSY dataset](https://archive.ics.uci.edu/ml/datasets/SUSY) with an imbalance ratio of 16 was used ([download imbalanced dataset](https://github.com/mjuez/approx-smote/releases/download/0.1.2/susy_ir16_nonorm.libsvm)).
This dataset consists on 2,881,796 instances, 2,712,173 belonging to the majority class and 169,623 to the minority class.
Its size stored as LibSVM format is 1.04 GB.
For ensuring experiments to be repeatable, a random seed was fixed to **46**.
The experiments consisted in reducing the imbalance ratio to **1** (i.e., balancing the dataset). 
The number of neighbors (i.e., *k*) was fixed to **5**. 
The number of partitions for SMOTE-BD was set to **8** as their authors recommended in the [original publication](https://journal.info.unlp.edu.ar/JCST/article/view/1122). 
The rest of the params were the default ones.

### Execution times and Speedup

Approx-SMOTE demonstrated to be between 7.52 (on the smallest cluster) and 28.15 (on the biggest cluster) times faster than SMOTE-BD. Speedup, which was calculated using the smallest cluster configuration as baseline, reveals good scalability for Approx-SMOTE, and scalability issues for SMOTE-BD.

The following table shows the execution time results (times are measured in seconds):

| Approach | 2 w | 4 w | 6 w | 8 w | 10 w |
| -------- | --: | --: | --: | --: | ---: |
| SMOTE-BD | 1321.39 | 2218.60 | 2103.68 | 1587.29 | 2172.05 |
| Approx-SMOTE | 175.70 | 123.70 | 113.89 | 91.30 | 77.15 |

The following figure shows a graphical representation of the execution time comparative results (left plot) and the speedup comparative results (right plot):

<img src="https://github.com/mjuez/assets/blob/main/approx-smote/scalability_experiments.jpg" width="100%">

### Classification performance

For this experiments, a Spark ML Random Forest of 100 trees with default parameters was used. The models were trained using the imbalanced dataset as it is, the oversampled dataset through SMOTE-BD, and the oversampled dataset through Approx-SMOTE. Classification performance showed to be nearly the same for both SMOTE approaches.

The following table shows the classification performance in terms of AUC, Geometric Mean, and F1 Score:

| Approach | AUC | Geometric Mean | F1 Score |
| -------- | --- | -------------- | -------- |
| Do Nothing | 0.6223 (&plusmn; 0.0089) | 0.4970 (&plusmn; 0.0190) | **0.3797** (&plusmn; 0.0184) |
| SMOTE-BD | 0.7708 (&plusmn; 0.0007) | 0.7706 (&plusmn; 0.0007) | 0.2934 (&plusmn; 0.0028) |
| Approx-SMOTE | **0.7720** (&plusmn; 0.0010) | **0.7715** (&plusmn; 0.0009) | 0.2998 (&plusmn; 0.0040) |

## Contribute

Feel free to submit any pull requests 😊

## Acknowlegments

The project leading to these results has received funding from "la Caixa" Foundation, under agreement LCF/PR/PR18/51130007.
This work was supported through project BU055P20 (JCyL/FEDER, UE) of the *Junta de Castilla y León* (co-financed through European Union FEDER funds). It also was supported through *Consejería de Educación of the *Junta de Castilla y León* and the European Social Fund through a pre-doctoral grant (EDU/1100/2017).
This material is based upon work supported by Google Cloud.

## License

This work is licensed under [Apache-2.0](LICENSE).

## Citation policy

This work is currently under review process, and citation policy will be available after its publication.

