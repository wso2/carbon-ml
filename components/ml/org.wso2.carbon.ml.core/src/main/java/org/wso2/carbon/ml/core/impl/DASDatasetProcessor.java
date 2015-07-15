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
package org.wso2.carbon.ml.core.impl;

import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLInputValidationException;
import org.wso2.carbon.ml.core.factories.DatasetType;
import org.wso2.carbon.ml.core.interfaces.DatasetProcessor;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;

/**
 * WSO2 Data Analytics Server related dataset processor
 */
public class DASDatasetProcessor extends DatasetProcessor {

    public DASDatasetProcessor(MLDataset dataset) throws MLInputValidationException {
        super(DatasetType.DAS, dataset);
        this.validate();
    }

    public void validate() throws MLInputValidationException {
        super.validate();
        String sourcePath = getDataset().getSourcePath();
        if (sourcePath == null || sourcePath.isEmpty()) {
            String msg = "Dataset source path is missing for dataset: " + getDataset().getName();
            handleValidationException(msg);
        }
    }

    public void process() throws MLDataProcessingException {
        try {
            MLDataset dataset = getDataset();
            setTargetPath(dataset.getSourcePath());
            // extract sample points
            setSamplePoints(MLUtils.getSampleFromDAS(dataset.getSourcePath(), MLCoreServiceValueHolder.getInstance()
                    .getSummaryStatSettings().getSampleSize(), dataset.getDataSourceType(), dataset.getTenantId()));
        } catch (Exception e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }
}
