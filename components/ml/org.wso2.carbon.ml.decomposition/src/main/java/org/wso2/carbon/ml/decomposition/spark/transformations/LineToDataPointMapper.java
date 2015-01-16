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
package org.wso2.carbon.ml.decomposition.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.decomposition.exceptions.DecompositionException;
import org.wso2.carbon.ml.decomposition.internal.DecompositionConstants;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * This mapper converts each line into a set of data points
 */
public class LineToDataPointMapper implements Function<String, LabeledPoint> {
    private Pattern tokenSeparator;
    private Set<Integer> featureIndices;
    private int labelIndex;

    public LineToDataPointMapper(Pattern pattern, Set<Integer> featureIndices, int labelIndex) {
        this.tokenSeparator = pattern;
        this.featureIndices = featureIndices;
        this.labelIndex = labelIndex;
    }

    @Override
    public LabeledPoint call(String line) throws DecompositionException {
        try {
            double[] features = new double[featureIndices.size()];
            String[] tokens = tokenSeparator.split(line);

            int index = 0;
            for (int i=0;  i < tokens.length; i++) {
                if (featureIndices.contains(i)) {
                    String token = tokens[i];
                    if ( token.equalsIgnoreCase(DecompositionConstants.EMPTY) ||
                            token.equalsIgnoreCase(DecompositionConstants.NA)) {
                        features[index] = 0.0;
                    } else {

                        features[index] = Double.parseDouble(token.trim());
                    }
                    index++;
                }
            }
            return new LabeledPoint(Double.parseDouble(tokens[labelIndex]),Vectors.dense(features));

        } catch (Exception e) {
            throw new DecompositionException(
                "An error occurred while transforming lines to tokens: " + e.getMessage(), e);
        }
    }
}
