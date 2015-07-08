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

package org.wso2.carbon.ml.core.spark.transformations;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

import java.util.List;

/**
 * This class transforms string array of tokens to vectors
 */
public class TokensToVectors implements Function<String[], Vector> {

    private static final long serialVersionUID = 5185155928988547761L;
    private List<Integer> indices;

    public TokensToVectors(List<Integer> indices) {
        this.indices = indices;
    }

    @Override
    public Vector call(String[] tokens) throws MLModelBuilderException {
        try {
            double[] features = new double[indices.size()];
            int i = 0;
            for (int j : indices) {
                if (NumberUtils.isNumber(tokens[j])) {
                    features[i] = Double.parseDouble(tokens[j]);
                }
                i++;
            }
            return Vectors.dense(features);
        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while converting tokens to vectors: " + e.getMessage(), e);
        }
    }
}
