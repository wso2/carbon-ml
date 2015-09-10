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
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.utils.MLUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A filter to remove discarded rows - Impute Option: Discard
 */
public class DiscardedRowsFilter implements Function<String[], Boolean> {

    private static final long serialVersionUID = -2903794636287515590L;
    private final List<Integer> indices;

    private DiscardedRowsFilter(Builder builder) {
        this.indices = builder.indices;
    }

    @Override
    public Boolean call(String[] tokens) {
        Boolean keep = true;
        for (Integer index : indices) {
            if (index >= tokens.length || MLConstants.MISSING_VALUES.contains(tokens[index])) {
                keep = false;
                break;
            }
        }
        return keep;
    }

    public static class Builder {
        private List<Integer> indices;

        public Builder init(MLModelConfigurationContext ctx) {
            this.indices = MLUtils.getImputeFeatureIndices(ctx.getFacts(), new ArrayList<Integer>(),
                    MLConstants.DISCARD);
            return this;
        }

        public Builder indices(List<Integer> indices) {
            this.indices = indices;
            return this;
        }

        public DiscardedRowsFilter build() {
            return new DiscardedRowsFilter(this);
        }
    }
}
