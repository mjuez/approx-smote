# Approx-SMOTE: fast SMOTE for Big Data on Apache Spark

This repository contains an approximated SMOTE (Approx-SMOTE) implementation for Apache Spark framework. It uses [saurfang's spark-knn](https://github.com/saurfang/spark-knn) for efficient aproximated neighbors search. This approach outperformed other existing SMOTE-based approaches for Apache Spark maintaining their advantages for some classification tasks.

SMOTE, or synthetic minority oversampling technique, creates synthetic instances belonging to the minority class of binary imbalanced classification problems. This could help overcoming biased classification issues provoked by the imbalance present in some datasets.

Approx-SMOTE was implemented in **Scala 2.12** for **Apache Spark 3.0.1**.

## Authors

- Mario Juez-Gil <<mariojg@ubu.es>>
- Álvar Arnaiz-González
- Juan J. Rodríguez
- Carlos López-Nozal
- César García-Osorio

**Affiliation:**\
Departamento de Ingeniería Informática\
Universidad de Burgos\
[ADMIRABLE Research Group](http://admirable-ubu.es/)

## Installation

Approx-SMOTE is available on SparkPackages.

It can be installed as follows:

- **spark-shell**, **pyspark**, or **spark-submit**:
```bash
> $SPARK_HOME/bin/spark-shell --packages mjuez:approx-smote:1.1.2
```
- **sbt**:
```scala
resolvers += "Spark Packages Repo" at "https://repos.spark-packages.org",

libraryDependencies += "mjuez" % "approx-smote" % "1.1.2"
```
- **Maven**:
```xml
<dependencies>
  <!-- list of dependencies -->
  <dependency>
    <groupId>mjuez</groupId>
    <artifactId>approx-smote</artifactId>
    <version>1.1.2</version>
  </dependency>
</dependencies>
<repositories>
  <!-- list of other repositories -->
  <repository>
    <id>SparkPackagesRepo</id>
    <url>https://repos.spark-packages.org</url>
  </repository>
</repositories>
```

## Basic Usage

Approx-SMOTE is a Spark [Transformer](https://spark.apache.org/docs/latest/ml-pipeline.html#transformers). It has a single public method (`transform()`) for transforming one imbalanced binary classification Dataset into a resampled Dataframe with a different imbalance ratio.

Following the Spark MLlib API, the configuration of Approx-SMOTE is done through [Parameters](https://spark.apache.org/docs/latest/ml-pipeline.html#parameters). The following parameters could be set:

- `percOver`: Oversampling percentage. The number of synthetic instances to be created, as a percentage of the number of instances belonging to the minority class (default: 100, i.e., the number of minority samples is doubled).
- `k`: The number of nearest neighbors to find (default: 5).
- `maxDistance`: Maximum distance to find neighbors,used by saurfang's approximated *k*-NN (default: +∞).
- `bufferSize`: Size of buffer used to construct spill trees and top-level tree search, used by saurfang's approximated *k*-NN (default: -1.0, which means automatic effective nearest neighbor distance estimation).
- `topTreeSize`: Number of instances used at the top level of hybrid spill trees, used by saurfang's approximated *k*-NN (default: `num_minority_examples/500`)
- `topTreeLeafSize`: Number of points at which to switch to brute-force for top-level tree, used by saurfang's approximated *k*-NN (default: 5)
- `subTreeLeafSize`: Number of points at which to switch to brute-force for distributed sub-trees, used by saurfang's approximated *k*-NN (default: 20)
- `bufferSizeSampleSizes`: Number of sample sizes to take when estimating buffer size, used by saurfang's approximated *k*-NN (default: 100 to 1000 by 100)
- `balanceThreshold`: Fraction of total points at which spill tree reverts back to metric tree if either child contains more points, used by saurfang's approximated *k*-NN (default: 70%)
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
Six imbalanced datasets were used for assessing the classification performance:

| Dataset | # Instances | # Attributes | # maj/min | IR | Size (GB) |
| -------- | --: | --: | --: | --: | ---: |
[SUSY IR4](https://github.com/mjuez/approx-smote/releases/download/1.1.0/susy_ir4_nonorm.zip) | 3,389,320 | 18 | 2,712,173/677,147 | 4.00 | 1.23 |
[SUSY IR16](https://github.com/mjuez/approx-smote/releases/download/1.1.0/susy_ir16_nonorm.zip)     | 2,881,796 | 18 | 2,712,173/169,623   | 15.99  | 1.04 |
[HIGGS IR4](https://github.com/mjuez/approx-smote/releases/download/1.1.0/higgs_ir4_nonorm.zip)     | 7,284,166 | 28 | 5,829,123/1,455,043  | 4.00   | 3.94 |
[HIGGS IR16](https://github.com/mjuez/approx-smote/releases/download/1.1.0/higgs_ir16_nonorm.zip)    | 6,194,093 | 28 | 5,829,123/364,970   | 15.97  | 3.26 |
[HEPMASS IR4](https://github.com/mjuez/approx-smote/releases/download/1.1.0/hepmass_ir4_nonorm.zip)   | 6,561,364 | 28 | 5,250,124/1,311,240  | 4.00   | 3.77 |
[HEPMASS IR16](https://github.com/mjuez/approx-smote/releases/download/1.1.0/hepmass_ir16_nonorm.zip)  | 5,578,586 | 28 | 5,250,124/328,462   | 15.98  | 3.20 |

For ensuring experiments to be repeatable, a random seed was fixed to **46**.
The experiments consisted in reducing the imbalance ratio to **1** (i.e., balancing the dataset). 
The number of neighbors (i.e., *k*) was fixed to **5**. 
The number of partitions for SMOTE-BD was set to **8** as their authors recommended in the [original publication](https://journal.info.unlp.edu.ar/JCST/article/view/1122). 
The rest of the parameters were the default ones.

The experiment scripts are available in the [experiments folder](experiments). The file [gcloud_scripts.sh](experiments/gcloud_scripts.sh) contains the commands used for launching the differents experiments in Google Cloud. The file [experimenter.scala](experiments/experimenter.scala) contains the `ClassificationExperimentLauncher` class, which performs the cross validation, instantiates Approx-SMOTE, and trains Random Forests.

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

The following table shows the classification performance in terms of AUC, and F1 Score. The best results appear within black boxes. The higher the blueness intensity, the better the performance. The value at the right of the ± sign, refers to the standard deviation between cross-validation folds.:

<img src="https://github.com/mjuez/assets/blob/main/approx-smote/classification_results_table.jpg" width="100%">

The following figure shows the Bayesian Hierarchical sign tests demonstrating the equivalence between SMOTE-BD and Approx-SMOTE.

<img src="https://github.com/mjuez/assets/blob/main/approx-smote/bayesian_tests.jpg" width="100%">

## Contribute

Feel free to submit any pull requests 😊

## Acknowlegments

The project leading to these results has received funding from "la Caixa" Foundation, under agreement LCF/PR/PR18/51130007.
This work was supported through project BU055P20 (JCyL/FEDER, UE) of the *Junta de Castilla y León* (co-financed through European Union FEDER funds). It also was supported through *Consejería de Educación* of the *Junta de Castilla y León* and the European Social Fund through a pre-doctoral grant (EDU/1100/2017).
This material is based upon work supported by Google Cloud.

## License

This work is licensed under [Apache-2.0](LICENSE).

## Citation policy

Please, cite this research as:

```
@article{juez2021approx,
title = {Approx-SMOTE: fast SMOTE for Big Data on Apache Spark},
journal = {Neurocomputing},
year = {2021},
issn = {0925-2312},
doi = {https://doi.org/10.1016/j.neucom.2021.08.086},
url = {https://www.sciencedirect.com/science/article/pii/S0925231221012832},
author = {Mario Juez-Gil and \'{A}lvar Arnaiz-Gonz\'alez and Juan J. Rodr\'iguez and Carlos L\'opez-Nozal and C\'esar Garc\'ia-Osorio}
keywords = {SMOTE, imbalance, Spark, big data, data mining},
abstract = {One of the main goals of Big Data research, is to find new data mining methods that are able to process large amounts of data in acceptable times. In Big Data classification, as in traditional classification, class imbalance is a common problem that must be addressed, in the case of Big Data also looking for a solution that can be applied in an acceptable execution time. In this paper we present Approx-SMOTE, a parallel implementation of the SMOTE algorithm for the Apache Spark framework. The key difference with the original SMOTE, besides parallelism, is that it uses an approximate version of k-Nearest Neighbor which makes it highly scalable. Although an implementation of SMOTE for Big Data already exists (SMOTE-BD), it uses an exact Nearest Neighbor search, which does not make it entirely scalable. Approx-SMOTE on the other hand is able to achieve up to 30 times faster run times without sacrificing the improved classification performance offered by the original SMOTE.}
}
```


