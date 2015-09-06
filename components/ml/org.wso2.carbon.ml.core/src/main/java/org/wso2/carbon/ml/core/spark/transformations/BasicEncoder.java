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
import org.wso2.carbon.ml.core.spark.algorithms.SparkModelUtils;

import java.util.List;
import java.util.Map;

/**
 * This class performs one hot encoding on categorical features.
 */
public class BasicEncoder implements Function<String[], String[]> {

    private static final long serialVersionUID = -5025419727399292773L;
    private final List<Map<String, Integer>> encodings;

    private BasicEncoder(Builder builder) {
        this.encodings = builder.encodings;
    }

    @Override
    public String[] call(String[] tokens) {
        if (encodings == null || encodings.isEmpty()) {
            return tokens;
        }
        for (int i = 0; i < tokens.length; i++) {
            if (encodings.size() <= i) {
                continue;
            }
            Map<String, Integer> encoding = encodings.get(i);
            if (encoding != null && !encoding.isEmpty()) {
                // if we found an unknown string, we encode it from 0th mapping.
                String code = encoding.get(tokens[i]) == null ? String.valueOf(encoding.values().iterator().next())
                        : String.valueOf(encoding.get(tokens[i]));
                // replace the value with the encoded value
                tokens[i] = code;
            }
        }
        return tokens;
    }

    public static class Builder {
        private List<Map<String, Integer>> encodings;

        public Builder init(MLModelConfigurationContext ctx) {
            this.encodings = SparkModelUtils.buildEncodings(ctx);
            return this;
        }

        public Builder encodings(List<Map<String, Integer>> encodings) {
            this.encodings = encodings;
            return this;
        }

        public BasicEncoder build() {
            return new BasicEncoder(this);
        }
    }

}
