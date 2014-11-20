/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;

public class TokensToLabeledPoints implements Function<String[], LabeledPoint> {
    private final int responseIndex;
    private static final Log logger = LogFactory.getLog(TokensToLabeledPoints.class);

    /**
     * @param index Index of the response variable
     */
    TokensToLabeledPoints(int index) {
        this.responseIndex = index;
    }

    /**
     * @param tokens String array of tokens
     * @return Labeled point
     * @throws ModelServiceException
     */
    @Override
    public LabeledPoint call(String[] tokens) throws ModelServiceException {
        try {
            double y = Double.parseDouble(tokens[responseIndex]);
            double[] x = new double[tokens.length];
            for (int i = 0; i < tokens.length; ++i) {
                if (responseIndex != i) {
                    x[i] = Double.parseDouble(tokens[i]);
                }
            }
            return new LabeledPoint(y, Vectors.dense(x));
        } catch (Exception e) {
            String msg = "An error occurred while converting tokens to labeled points\n" + e
                    .getMessage();
            logger.error(msg, e);
            throw new ModelServiceException(msg);
        }
    }
}
