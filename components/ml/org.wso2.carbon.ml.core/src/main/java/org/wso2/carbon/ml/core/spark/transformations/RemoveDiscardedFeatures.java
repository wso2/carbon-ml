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
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;

import java.util.List;

/**
 * This class removes columns with discarded features and also will restructure the columns such that the response
 * column is the last column.
 */
public class RemoveDiscardedFeatures implements Function<String[], String[]> {

    private static final long serialVersionUID = -3847503088002249546L;
    private final List<Integer> newToOldIndicesList;
    private final int responseIndex;

    private RemoveDiscardedFeatures(Builder builder) {
        this.newToOldIndicesList = builder.newToOldIndicesList;
        this.responseIndex = builder.responseIndex;
    }

    /**
     * Function to remove discarded columns.
     *
     * @param tokens String array of tokens
     * @return String array
     */
    @Override
    public String[] call(String[] tokens) {
        int size = newToOldIndicesList.size() + 1;
        if (responseIndex == -1) {
            size = newToOldIndicesList.size();
        }
        String[] features = new String[size];
        for (int i = 0; i < tokens.length; i++) {
            int newIndex = newToOldIndicesList.indexOf(i);
            if (newIndex != -1) {
                features[newIndex] = tokens[i];
            } else if (i == responseIndex) {
                features[features.length - 1] = tokens[i];
            } else {
                // discarded feature
                continue;
            }
        }
        return features;
    }

    public static class Builder {
        private List<Integer> newToOldIndicesList;
        private int responseIndex;

        public Builder init(MLModelConfigurationContext ctx) {
            this.responseIndex = ctx.getResponseIndex();
            this.newToOldIndicesList = ctx.getNewToOldIndicesList();
            return this;
        }

        public Builder responseIndex(int index) {
            this.responseIndex = index;
            return this;
        }

        public Builder indices(List<Integer> indices) {
            this.newToOldIndicesList = indices;
            return this;
        }

        public RemoveDiscardedFeatures build() {
            return new RemoveDiscardedFeatures(this);
        }
    }
}
