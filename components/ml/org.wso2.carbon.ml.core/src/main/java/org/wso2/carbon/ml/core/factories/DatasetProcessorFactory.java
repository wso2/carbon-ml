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

import java.io.InputStream;

import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.core.exceptions.MLInputValidationException;
import org.wso2.carbon.ml.core.impl.DASDatasetProcessor;
import org.wso2.carbon.ml.core.impl.FileDatasetProcessor;
import org.wso2.carbon.ml.core.impl.HdfsDatasetProcessor;
import org.wso2.carbon.ml.core.interfaces.DatasetProcessor;

/**
 * This factory class is responsible for generating a {@link DatasetProcessor} for a given type.
 */
public class DatasetProcessorFactory {

    public static DatasetProcessor buildDatasetProcessor(DatasetType type, MLDataset dataset, InputStream inputStream)
            throws MLInputValidationException {
        DatasetProcessor datasetProcessor = null;
        switch (type) {
        case FILE:
            datasetProcessor = new FileDatasetProcessor(dataset, inputStream);
            break;
        case DAS:
            datasetProcessor = new DASDatasetProcessor(dataset);
            break;
        case HDFS:
            datasetProcessor = new HdfsDatasetProcessor(dataset);
            break;
        default:
            throw new MLInputValidationException("Invalid dataset source type: " + type.name());
        }
        return datasetProcessor;
    }

    public static DatasetProcessor buildDatasetProcessor(MLDataset dataset, InputStream inputStream)
            throws MLInputValidationException {
        String type = dataset.getDataSourceType();
        DatasetType datasetType = DatasetType.getDatasetType(type);
        if (datasetType == null) {
            throw new MLInputValidationException("Invalid dataset source type: " + type);
        }
        return buildDatasetProcessor(datasetType, dataset, inputStream);
    }
}
