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
package org.wso2.carbon.ml.core.interfaces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLInputValidationException;
import org.wso2.carbon.ml.core.factories.DatasetType;

/**
 * All dataset processor should extend this class.
 */
public abstract class DatasetProcessor {

    private static final Log log = LogFactory.getLog(DatasetProcessor.class);
    private DatasetType type = null;
    private MLDataset dataset = null;
    private String targetPath = null;
    private SamplePoints samplePoints = null;

    public DatasetProcessor(DatasetType type, MLDataset dataset) {
        this.setType(type);
        this.setDataset(dataset);
    }
    
    /**
     * Perform basic validation required by all dataset processors.
     * @throws MLInputValidationException
     */
    public void validate() throws MLInputValidationException {
        String datasetName = dataset.getName();
        String version = dataset.getVersion();
        String destination = dataset.getDataTargetType();
        String dataFormat = dataset.getDataType();
        if (datasetName == null || datasetName.isEmpty() || version == null || version.isEmpty()
                || destination == null || destination.isEmpty()
                || dataFormat == null || dataFormat.isEmpty()) {
            String msg = "Required parameters are missing.";
            handleValidationException(msg);
        }
    }
    
    /**
     * Process a given dataset and use {@link DatasetProcessor#setDataset(MLDataset)} and
     * {@link DatasetProcessor#setSamplePoints(SamplePoints))} to set expected data.
     * 
     * @throws MLDataProcessingException
     */
    public abstract void process() throws MLDataProcessingException;
    
    public void handleValidationException(String msg) throws MLInputValidationException {
        log.error(msg);
        throw new MLInputValidationException(msg);
    }
    
    public void handleValidationException(String msg, Exception e) throws MLInputValidationException {
        log.error(msg, e);
        throw new MLInputValidationException(msg, e);
    }
    
    public void handleIgnoreException(String msg, Exception e) {
        log.error(msg, e);
    }

    public MLDataset getDataset() {
        return dataset;
    }

    public void setDataset(MLDataset dataset) {
        this.dataset = dataset;
    }

    public DatasetType getType() {
        return type;
    }

    public void setType(DatasetType type) {
        this.type = type;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public SamplePoints getSamplePoints() {
        return samplePoints;
    }

    public void setSamplePoints(SamplePoints samplePoints) {
        this.samplePoints = samplePoints;
    }
}
