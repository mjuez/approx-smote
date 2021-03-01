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

package org.apache.spark.ml.instance

import scala.util.Random
import org.apache.spark.annotation.Since
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.{Params, IntParam, BooleanParam, ParamValidators, ParamMap}
import org.apache.spark.ml.param.shared.{HasLabelCol, HasFeaturesCol, HasSeed}
import org.apache.spark.ml.util.{DefaultParamsWritable, Identifiable, MetadataUtils}
import org.apache.spark.ml.feature.{MinMaxScaler, MinMaxScalerModel}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.knn.KNN
import org.apache.spark.sql.{Dataset, DataFrame, Row}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.ml.param.DoubleParam
import org.apache.spark.ml.param.IntArrayParam

/**
  * Approximated SMOTE algorithm.
  * An SMOTE algorithm that uses Saurfang's approximated 
  * KNN algorithm for efficient neighbour search.
  * 
  * @author Mario Juez-Gil <mariojg@ubu.es>
  * @param uid
  */
@Since("2.4.5")
class ASMOTE @Since("2.4.5") (
    @Since("2.4.5") override val uid: String) 
  extends Transformer with ASMOTEParams with HasLabelCol
  with HasFeaturesCol with HasSeed with DefaultParamsWritable {

  @Since("2.4.5")
  def this() = this(Identifiable.randomUID("asmote"))

  /** @group setParam */
  @Since("2.4.5")
  def setK(value: Int): this.type = set(k, value)

  /** @group setParam */
  @Since("2.4.5")
  def setPercOver(value: Int): this.type = set(percOver, value)

  /** @group setParam */
  @Since("2.4.5")
  def setMaxDistance(value: Double): this.type = set(maxDistance, value)

  /** @group setParam */
  @Since("2.4.5")
  def setBufferSize(value: Double): this.type = set(bufferSize, value)

  /** @group setParam */
  @Since("2.4.5")
  def setTopTreeSize(value: Int): this.type = set(topTreeSize, value)

  /** @group setParam */
  @Since("2.4.5")
  def setTopTreeLeafSize(value: Int): this.type = set(topTreeLeafSize, value)

  /** @group setParam */
  @Since("2.4.5")
  def setSubTreeLeafSize(value: Int): this.type = set(subTreeLeafSize, value)

  /** @group setParam */
  @Since("2.4.5")
  def setBufferSizeSampleSizes(value: Array[Int]): this.type = set(bufferSizeSampleSizes, value)

  /** @group setParam */
  @Since("2.4.5")
  def setBalanceThreshold(value: Double): this.type = set(balanceThreshold, value)

  /** @group setParam */
  @Since("2.4.5")
  def setSeed(value: Long): this.type = set(seed, value)

  /**
    * Transforms the dataset into an oversampled one.
    *
    * @param ds dataset.
    * 
    * @return an oversampled DataFrame.
    */
  override def transform(ds: Dataset[_]): DataFrame = {
    val categoricalFeatures: Map[Int, Int] =
      MetadataUtils.getCategoricalFeatures(ds.schema($(featuresCol)))

    require(categoricalFeatures.isEmpty, "ASMOTE requires all features to be numeric.")
    
    val session = ds.sparkSession
    val rnd = new Random($(seed))
    val (minorityLabel, minoritySize) = ds
      .groupBy($(labelCol))
      .count
      .sort(asc("count"))
      .collect
      .map(r => (r.getAs[Double](0), r.getAs[Long](1)))
      .apply(0)

    val minorityDS = ds.filter(col($(labelCol)) === minorityLabel)

    val scalerModel = new MinMaxScaler()
      .setInputCol($(featuresCol))
      .setOutputCol("fs")
      .setMin(0).setMax(1)
      .fit(minorityDS)

    val normMinorityDF = scalerModel
      .transform(minorityDS)
      .drop($(featuresCol))
      .withColumnRenamed("fs", $(featuresCol))
      .cache
      .toDF

    minorityDS.unpersist

    val frac = $(percOver).toFloat / 100.0
    val creationFactor = frac.ceil.toInt

    val tts = if($(topTreeSize) > 0) $(topTreeSize) else ((normMinorityDF.count/500.0).ceil.toInt)

    val knnDF = new KNN()
      .setK($(k))
      .setMaxDistance($(maxDistance))
      .setBufferSize($(bufferSize))
      .setTopTreeSize(tts)
      .setTopTreeLeafSize($(topTreeLeafSize))
      .setSubTreeLeafSize($(subTreeLeafSize))
      .setBufferSizeSampleSizes($(bufferSizeSampleSizes))
      .setBalanceThreshold($(balanceThreshold))
      .setAuxCols(Array($(featuresCol)))
      .setSeed(rnd.nextLong)
      .fit(normMinorityDF)
      .transform(normMinorityDF)

    normMinorityDF.unpersist

    val synthSamples = knnDF.rdd.flatMap {
      case Row(label: Double, currentF: Vector, neighborsIter: Iterable[_]) =>
      val neighbors = neighborsIter.asInstanceOf[Iterable[Row]].toList
      (0 to (creationFactor - 1)).map{ case(_) =>
        val randomIndex = rnd.nextInt(neighbors.size)
        val neighbour = neighbors(randomIndex).get(0).asInstanceOf[Vector].asBreeze
        val currentFBreeze = currentF.asBreeze
        val difference = (neighbour - currentFBreeze) * rnd.nextDouble
        val synthSample = Vectors.fromBreeze(currentFBreeze + difference)
        Row.fromSeq(Seq(label, synthSample))
      }
    }.cache

    val sampleFrac = frac / creationFactor
    val synthSamplesDF = session
      .createDataFrame(synthSamples, ds.select($(labelCol), $(featuresCol)).schema)
      .sample(false, sampleFrac, rnd.nextLong)
    ds.select($(labelCol), $(featuresCol)).toDF.union(denormalize(synthSamplesDF, scalerModel))
  }

  /**
   * The schema of the output Dataset is the same as the input one.
   * 
   * @param schema Input schema.
   */
  @Since("2.4.5")
  override def transformSchema(schema: StructType): StructType = schema

  /**
   * Creates a copy of this instance.
   * 
   * @param extra  Param values which will overwrite Params in the copy.
   */
  @Since("2.4.5")
  override def copy(extra: ParamMap): Transformer = defaultCopy(extra)

  /**
   * De-normalize the dataset previously normalized by a MinMaxScaler.
   * 
   * @author Álvar Arnaiz-González <alvarag@ubu.es>
   * @param df DataFrame to de-normalize.
   * @param scaler MinMaxScaler model used previously to normalize.
   * 
   * @return De-normalized dataframe.
   */
  @Since("2.4.5")
  private def denormalize(df: DataFrame, scaler: MinMaxScalerModel): DataFrame = {

    val numFeatures = scaler.originalMax.size
    val scale = 1

    // transformed value for constant cols
    val minArray = scaler.originalMin.toArray

    val scaleArray = Array.tabulate(numFeatures) { 
      i => val range = scaler.originalMax(i) - scaler.originalMin(i)
      // scaleArray(i) == 0 iff i-th col is constant (range == 0)
      if (range != 0) range else 0.0
    }

    val transformer = udf { vector: Vector =>
      // 0 in sparse vector will probably be rescaled to non-zero
      val values = vector.toArray
      var i = 0
      while (i < numFeatures) {
        if (!values(i).isNaN) {
          if (scaleArray(i) != 0) {
            values(i) = values(i) * scaleArray(i) + minArray(i)
          }
          else {
            // scaleArray(i) == 0 means i-th col is constant
            values(i) = scaler.originalMin(i)
          }
        }
        i += 1
      }
      Vectors.dense(values).compressed
    }

    // Denormalize the features column and overwrite it.
    df.withColumn($(featuresCol), transformer(col($(featuresCol))))
  }
  

}

/**
  * Parameters of Approximated SMOTE algorithm.
  * - k: Number of nearest neighbours (for KNN algorithm).
  * - percOver: The oversampling percentaje 
  *   (100% means duplicate the number of instances).
  * - topTreeSize: Number of points to sample for top-level
  *   tree (for KNN algorithm).
  * 
  * @author Mario Juez-Gil <mariojg@ubu.es>
  */
@Since("2.4.5")
trait ASMOTEParams extends Params {

  /** @group param */
  @Since("2.4.5")
  final val k: IntParam = new IntParam(this, "k", 
    "Number of nearest neighbours, preferably an odd number.",
    ParamValidators.gtEq(3))

  /** @group param */
  @Since("2.4.5")
  final val percOver: IntParam = new IntParam(this, "percOver", 
    "Oversampling percentage.", ParamValidators.gtEq(1))

  /** @group param */
  @Since("2.4.5")
  final val maxDistance = new DoubleParam(this, "maxNeighbors", "maximum distance to find neighbors", // todo: maxDistance or maxNeighbors?
                                     ParamValidators.gt(0))

  /** @group param */
  @Since("2.4.5")
  final val bufferSize = new DoubleParam(this, "bufferSize",
    "size of buffer used to construct spill trees and top-level tree search", ParamValidators.gtEq(-1.0))

  /** @group param */
  @Since("2.4.5")
  final val topTreeSize = new IntParam(this, "topTreeSize", 
    "Number of points to sample for top-level tree (KNN)", 
    ParamValidators.gtEq(0))

  /** @group param */
  @Since("2.4.5")
  final val topTreeLeafSize = new IntParam(this, "topTreeLeafSize",
    "number of points at which to switch to brute-force for top-level tree", ParamValidators.gt(0))

  /** @group param */
  @Since("2.4.5")
  final val subTreeLeafSize = new IntParam(this, "subTreeLeafSize",
    "number of points at which to switch to brute-force for distributed sub-trees", ParamValidators.gt(0))

  /** @group param */
  @Since("2.4.5")
  final val bufferSizeSampleSizes = new IntArrayParam(this, "bufferSizeSampleSize",  // todo: should this have an 's' at the end?
    "number of sample sizes to take when estimating buffer size", { arr: Array[Int] => arr.length > 1 && arr.forall(_ > 0) })

  /** @group param */
  @Since("2.4.5")
  final val balanceThreshold = new DoubleParam(this, "balanceThreshold",
    "fraction of total points at which spill tree reverts back to metric tree if either child contains more points",
    ParamValidators.inRange(0, 1))

  setDefault(k -> 5, percOver -> 100, topTreeSize -> 1000, topTreeLeafSize -> 10, subTreeLeafSize -> 30,
    bufferSize -> -1.0, bufferSizeSampleSizes -> (100 to 1000 by 100).toArray, balanceThreshold -> 0.7, maxDistance -> Double.PositiveInfinity)

  /** @group getParam */
  @Since("2.4.5")
  final def getK: Int = $(k)
  
  /** @group getParam */
  @Since("2.4.5")
  final def getPercOver: Int = $(percOver)

  /** @group getParam */
  @Since("2.4.5")
  final def getMaxDistance: Double = $(maxDistance)

  /** @group getParam */
  @Since("2.4.5")
  final def getBufferSize: Double = $(bufferSize)

  /** @group getParam */
  @Since("2.4.5")
  final def getTopTreeSize: Int = $(topTreeSize)

  /** @group getParam */
  @Since("2.4.5")
  final def getTopTreeLeafSize: Int = $(topTreeLeafSize)

  /** @group getParam */
  @Since("2.4.5")
  final def getSubTreeLeafSize: Int = $(subTreeLeafSize)

  /** @group getParam */
  @Since("2.4.5")
  final def getBufferSizeSampleSizes: Array[Int] = $(bufferSizeSampleSizes)

  /** @group getParam */
  @Since("2.4.5")
  final def getBalanceThreshold: Double = $(balanceThreshold)
}