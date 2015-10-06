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

/**
 * This class removes response columns
 * It is assume that the response column is the last column. If the response column is not the last column,
 * RemoveDiscardedFeatures transformation should be applied prior to this transformation to restructure the response
 * column as the last column
 */
public class RemoveResponseColumn implements Function<String[], String[]> {

    private static final long serialVersionUID = -3847503088002249546L;

    /**
     * Function to remove response column.
     *
     * @param tokens String array of tokens
     * @return String array
     */
    @Override
    public String[] call(String[] tokens) {

        String[] features = new String[tokens.length - 1];

        for (int i = 0; i < features.length; i++) {
            features[i] = tokens[i];
        }
        return features;
    }

}
