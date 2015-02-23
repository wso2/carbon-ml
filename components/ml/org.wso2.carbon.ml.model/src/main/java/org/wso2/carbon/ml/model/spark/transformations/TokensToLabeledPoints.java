/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.model.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;

import java.util.Map;

/**
 * This class transforms double array of tokens to labeled point
 */
public class TokensToLabeledPoints implements Function<double[], LabeledPoint> {
    private final int responseIndex;
    private Map<Integer, Double> meanImputation;

    /**
     * @param index Index of the response variable
     */
    public TokensToLabeledPoints(int index) {
        this.responseIndex = index;
    }

    /**
     * Function to transform double array into labeled point
     *
     * @param tokens    Double array of tokens
     * @return          Labeled point
     * @throws          ModelServiceException
     */
    @Override
    public LabeledPoint call(double[] tokens) throws ModelServiceException {
        try {
            double response = tokens[responseIndex];
            double[] features = new double[tokens.length];
            for (int i = 0; i < tokens.length; ++i) {
                // if not response
                if (responseIndex != i) {
                    features[i] = tokens[i];
                }
            }
            return new LabeledPoint(response, Vectors.dense(features));
        } catch (Exception e) {
            throw new ModelServiceException("An error occurred while transforming tokens to labeled points: "
                    + e.getMessage(), e);
        }
    }
}
