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
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.internal.constants.MLModelConstants;

import java.util.Map;

public class MeanImputation implements Function<String[], double[]> {
    private Map<Integer, Double> meanImputation;

    public MeanImputation(Map<Integer, Double> meanImputation) {
        this.meanImputation = meanImputation;
    }

    @Override
    public double[] call(String[] tokens) throws ModelServiceException {
        try {
            double[] features = new double[tokens.length];
            for (int i = 0; i < tokens.length; ++i) {
                if (MLModelConstants.EMPTY.equals(tokens[i]) || MLModelConstants.NA.equals(tokens[i])) {
                    // if mean imputation is set
                    if (meanImputation.containsKey(i)) {
                        features[i] = meanImputation.get(i);
                    }
                } else {
                    features[i] = Double.parseDouble(tokens[i]);
                }
            }
            return features;
        } catch (Exception e) {
            throw new ModelServiceException("An error occurred while applying mean imputation: " + e.getMessage(), e);
        }
    }
}
