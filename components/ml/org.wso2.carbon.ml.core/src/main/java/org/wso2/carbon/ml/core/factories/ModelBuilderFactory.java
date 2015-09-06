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
package org.wso2.carbon.ml.core.factories;

import org.wso2.carbon.ml.core.exceptions.MLInputValidationException;
import org.wso2.carbon.ml.core.interfaces.MLModelBuilder;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.algorithms.SupervisedSparkModelBuilder;
import org.wso2.carbon.ml.core.spark.algorithms.UnsupervisedSparkModelBuilder;

/**
 * This factory class is responsible for generating a {@link MLModelBuilder} for a given algorithm type.
 */
public class ModelBuilderFactory {

    public static MLModelBuilder buildModelBuilder(String algorithmType, MLModelConfigurationContext context)
            throws MLInputValidationException {
        // common transformations
        AlgorithmType type = AlgorithmType.getAlgorithmType(algorithmType);

        MLModelBuilder modelBuilder = null;
        switch (type) {
        case CLASSIFICATION:
        case NUMERICAL_PREDICTION:
            modelBuilder = new SupervisedSparkModelBuilder(context);
            break;
        case CLUSTERING:
            modelBuilder = new UnsupervisedSparkModelBuilder(context);
            break;
        default:
            throw new MLInputValidationException("Invalid algorithm type: " + type.name());
        }
        return modelBuilder;
    }

}
