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

package org.apache.spark.mllib.knn

import org.apache.spark.ml.{linalg => newlinalg}
import org.apache.spark.mllib.{linalg => oldlinalg}
import org.apache.spark.mllib.util.MLUtils

/**
  * This code has been extracted from Saurfang's spark-knn library
  * available on: https://github.com/saurfang/spark-knn
  * 
  * @author saurfang <https://github.com/saurfang>
  */
object KNNUtils {

  import oldlinalg.VectorImplicits._

  def fastSquaredDistance(
                           v1: newlinalg.Vector,
                           norm1: Double,
                           v2: newlinalg.Vector,
                           norm2: Double,
                           precision: Double = 1e-6): Double = {
    MLUtils.fastSquaredDistance(v1, norm1, v2, norm2, precision)
  }

}