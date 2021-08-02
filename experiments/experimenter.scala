/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ubu.admirable.exec
import org.apache.spark.ml.classification._
import org.apache.spark.ml.Estimator
import org.apache.spark.mllib.util.MLUtils
import org.ubu.admirable.util.LibSVMReader
import org.apache.spark.sql.functions.rand
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.param.Param
import java.lang.reflect.MalformedParametersException
import java.security.InvalidParameterException
import org.apache.spark.ml.param.ParamMap
import scala.util.Random
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.ml.Model
import org.apache.spark.sql.functions._
import org.apache.spark.ml.param.LongParam
import org.apache.spark.ml.param.IntParam
import org.apache.spark.ml.param.DoubleParam
import org.apache.spark.ml.param.BooleanParam
import java.util.Calendar
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.IndexToString
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.instance.ASMOTE
import java.time.Instant
import java.time.Duration
import org.apache.spark.ml.instance.SMOTE
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame

object ClassificationExperimentLauncher {

  def splitDataset(ds: Dataset[Row], seed: Long): (Dataset[Row], Dataset[Row]) = {
    val splits = ds.randomSplit(Array(0.5, 0.5), seed)
    (splits(0), splits(1))
  }
  
  def main(args: Array[String]) {
    val session = SparkSession.builder.appName("(A)SMOTE classification Launcher")
      .getOrCreate()
    session.sparkContext.setLogLevel("WARN")
    val rnd = new Random(46)

    val inputDataset = args(0)
    val outputPath = args(1)

    val dataset = LibSVMReader.libSVMToML(inputDataset, session)
      .select(col("label"), col("features")).cache()
    val schema = dataset.schema

    val labels = dataset
      .groupBy("label")
      .count
      .sort(asc("count"))
      .collect
      .map(r => (r.getAs[Double](0), r.getAs[Long](1)))
    val (minorityLabel, minoritySize) = labels(0)
    val (majorityLabel, majoritySize) = labels(1)

    val minorityDS = dataset.filter(col("label") === minorityLabel).cache
    val majorityDS = dataset.filter(col("label") === majorityLabel).cache

    (0 until 5).foreach(rep => {

      val (minoF1DS, minoF2DS) = splitDataset(minorityDS, rnd.nextLong)
      val (majoF1DS, majoF2DS) = splitDataset(majorityDS, rnd.nextLong)
      val f1DS = minoF1DS.union(majoF1DS).cache
      val f2DS = minoF2DS.union(majoF2DS).cache

      val resampleDatasets = (foldDS: Dataset[Row]) => {
        val foldMinoSize = foldDS.filter(col("label") === minorityLabel).count
        val foldMajoSize = foldDS.filter(col("label") === majorityLabel).count
        val asmotePercOver = (((foldMajoSize - foldMinoSize) / foldMinoSize.toDouble) * 100).ceil.toInt
        val smote = new SMOTE().setK(5).setPercOver(100).setSeed(rnd.nextLong)
        val smoteDS = smote.transform(foldDS).cache
        val asmote = new ASMOTE().setK(5).setPercOver(asmotePercOver).setSeed(rnd.nextLong)
        val asmoteDS = asmote.transform(foldDS).cache
        (smoteDS, asmoteDS)
      }

      val testClassifiers = (baseTrainDS: Dataset[Row], smoteTrainDS: Dataset[Row], 
                              asmoteTrainDS: Dataset[Row], testDS: Dataset[Row], fold: Int) => {
        val saveResults = (df: DataFrame, name: String) => {
          df
            .select(col("label"), col("prediction"))
            .withColumnRenamed("label", "true")
            .withColumnRenamed("prediction", "predicted")
            .withColumn("fold", lit(fold))
            .withColumn("repetition", lit(rep))
            .repartition(28).write.format("csv")
            .save(outputPath + name + "_r" + (rep+1) + "_f" + fold)
        }

        val rf = new RandomForestClassifier()
          .setNumTrees(100)
          .setSeed(rnd.nextLong)

        val baseModel = rf.fit(baseTrainDS)
        val smoteModel = rf.fit(smoteTrainDS)
        val asmoteModel = rf.fit(asmoteTrainDS)

        val baseResultsDF = baseModel.transform(testDS)
        val smoteResultsDF = smoteModel.transform(testDS)
        val asmoteResultsDF = asmoteModel.transform(testDS)

        saveResults(baseResultsDF, "base")
        saveResults(smoteResultsDF, "smote")
        saveResults(asmoteResultsDF, "asmote")
      }

      val (smoteF1DS, asmoteF1DS) = resampleDatasets(f1DS)
      val (smoteF2DS, asmoteF2DS) = resampleDatasets(f2DS)

      testClassifiers(f1DS, smoteF1DS, asmoteF1DS, f2DS, 1)
      smoteF1DS.unpersist
      asmoteF1DS.unpersist
      testClassifiers(f2DS, smoteF2DS, asmoteF2DS, f1DS, 2)
      smoteF2DS.unpersist
      asmoteF2DS.unpersist
      f1DS.unpersist
      f2DS.unpersist
    })
    
  }

}