/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.core.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

/**
 * This class transforms double array to labeled point
 */
public class DoubleArrayToVector implements Function<double[], Vector> {

    private static final long serialVersionUID = 8329428281317101710L;

    /**
     * Function to transform double array into labeled point.
     *
     * @param features  Double array of tokens
     * @return          Vector
     * @throws          ModelServiceException
     */
    @Override
    public Vector call(double[] features) throws MLModelBuilderException {
        try {
            return Vectors.dense(features);
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while transforming double array to vector: "
                    + e.getMessage(), e);
        }
    }
}
