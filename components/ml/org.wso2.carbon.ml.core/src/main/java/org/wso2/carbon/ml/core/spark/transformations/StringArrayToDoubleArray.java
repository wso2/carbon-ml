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

import org.apache.spark.api.java.function.Function;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

public class StringArrayToDoubleArray implements Function<String[], double[]> {

    private static final long serialVersionUID = 6885775018049237606L;

    @Override
    public double[] call(String[] tokens) throws MLModelBuilderException {
        try {
            double[] features = new double[tokens.length];
            for (int i = 0; i < tokens.length; ++i) {
                features[i] = Double.parseDouble(tokens[i]);
            }
            return features;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while transforming tokens: " + e.getMessage(), e);
        }
    }
}
