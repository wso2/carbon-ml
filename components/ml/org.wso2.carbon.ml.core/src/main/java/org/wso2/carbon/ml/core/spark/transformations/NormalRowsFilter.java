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
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;

/**
 * A filter to remove anomaly rows
 */
public class NormalRowsFilter implements Function<String[], Boolean> {

    private static final long serialVersionUID = 8329428281317101710L;
    private final String[] normalLabels;
    private final int responseIndex;

    public NormalRowsFilter(Builder builder) {
        this.normalLabels = builder.normalLabels;
        this.responseIndex = builder.responseIndex;
    }

    // @Override
    // public Boolean call(String[] tokens) {
    // Boolean keep = false;
    // for (int i=0; i<tokens.length; i++) {
    // if (i == responseIndex) {
    // for(String label: normalLabels) {
    // if(tokens[i] == label)
    // keep = true;
    // break;
    // }
    // }
    // }
    // return keep;
    // }

    @Override
    public Boolean call(String[] tokens) {
        Boolean keep = false;
        for (String label : normalLabels) {
            if (tokens[responseIndex].equals(label)) {
                keep = true;
                break;
            }
        }
        return keep;
    }

    public static class Builder {
        private String[] normalLabels;
        private int responseIndex;

        public Builder init(MLModelConfigurationContext ctx) {
            normalLabels = ctx.getFacts().getNormalLabels().split(",");
            responseIndex = ctx.getResponseIndex();

            return this;
        }

        public NormalRowsFilter build() {
            return new NormalRowsFilter(this);
        }

    }
}
