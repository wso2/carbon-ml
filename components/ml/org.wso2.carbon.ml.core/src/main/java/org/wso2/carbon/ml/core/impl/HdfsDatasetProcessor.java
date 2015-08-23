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

import java.io.IOException;
import java.io.InputStream;

import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLInputValidationException;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.factories.DatasetType;
import org.wso2.carbon.ml.core.interfaces.DatasetProcessor;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;

/**
 * HDFS related dataset processor.
 */
public class HdfsDatasetProcessor extends DatasetProcessor {

    public HdfsDatasetProcessor(MLDataset dataset) throws MLInputValidationException {
        super(DatasetType.HDFS, dataset);
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
        InputStream inputStream = null;
        try {
            MLDataset dataset = getDataset();
            MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
            MLIOFactory ioFactory = new MLIOFactory(valueHolder.getMlProperties());
            MLInputAdapter inputAdapter = ioFactory
                    .getInputAdapter(dataset.getDataSourceType() + MLConstants.IN_SUFFIX);
            MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter(dataset.getDataTargetType()
                    + MLConstants.OUT_SUFFIX);
            inputStream = inputAdapter.read(dataset.getSourcePath());
            setTargetPath(ioFactory.getTargetPath(dataset.getName() + "." + dataset.getTenantId() + "."
                    + System.currentTimeMillis()));
            outputAdapter.write(getTargetPath(), inputStream);
            setFirstLine(getTargetPath());
        } catch (Exception e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    handleIgnoreException("Failed to close the input stream.", e);
                }
            }
        }
    }

    @Override
    public SamplePoints takeSample() throws MLDataProcessingException {
        MLDataset dataset = getDataset();
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        try {
            return MLUtils.getSample(getTargetPath(), dataset.getDataType(), valueHolder.getSummaryStatSettings()
                    .getSampleSize(), dataset.isContainsHeader(), dataset.getDataSourceType(), dataset.getTenantId());
        } catch (MLMalformedDatasetException e) {
            throw new MLDataProcessingException(e.getMessage(), e);
        }
    }

}
