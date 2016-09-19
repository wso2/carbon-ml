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
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.algorithms.SparkModelUtils;
import org.wso2.carbon.ml.core.utils.MLUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeanImputation implements Function<String[], String[]> {

    private static final long serialVersionUID = 6936249532612016896L;
    private final Map<Integer, Double> meanImputation;

    public MeanImputation(Builder builder) {
        this.meanImputation = builder.meanImputation;
    }

    @Override
    public String[] call(String[] tokens) throws MLModelBuilderException {
        try {
            String[] features = new String[tokens.length];
            for (int i = 0; i < tokens.length; ++i) {
                if (MLConstants.MISSING_VALUES.contains(tokens[i])) {
                    // if mean imputation is set
                    if (meanImputation.containsKey(i)) {
                        features[i] = String.valueOf(meanImputation.get(i));
                    }
                } else {
                    features[i] = tokens[i];
                }
            }
            return features;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while applying mean imputation: " + e.getMessage(), e);
        }
    }

    public static class Builder {
        private Map<Integer, Double> meanImputation;

        public Builder init(MLModelConfigurationContext ctx) {
            meanImputation = new HashMap<Integer, Double>();
            // get feature indices for mean imputation
            List<Integer> oldIndices = ctx.getNewToOldIndicesList();
            List<Integer> meanImputeIndices = MLUtils.getImputeFeatureIndices(ctx.getFacts(),
                    ctx.getNewToOldIndicesList(), MLConstants.MEAN_IMPUTATION);
            List<Feature> features = ctx.getFacts().getFeatures();
            Map<String, String> stats = ctx.getSummaryStatsOfFeatures();
            for (Feature feature : features) {
                int newIndex = oldIndices.indexOf(feature.getIndex());
                if (meanImputeIndices.indexOf(newIndex) != -1) {
                    String featureStat = stats.get(feature.getName());
                    double mean = SparkModelUtils.getMean(featureStat);
                    meanImputation.put(newIndex, mean);
                }
            }
            return this;
        }

        public Builder imputations(Map<Integer, Double> meanImputation) {
            this.meanImputation = meanImputation;
            return this;
        }

        public MeanImputation build() {
            return new MeanImputation(this);
        }
    }
}
