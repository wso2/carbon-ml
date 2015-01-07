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
package org.wso2.carbon.ml.decomposition;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.wso2.carbon.ml.decomposition.exceptions.DecompositionException;
import org.wso2.carbon.ml.decomposition.spark.dto.PCAResult;

import java.util.List;

public interface DecompositionService {

    /**
     * This method performs PCA for a given dataset
     * @param workflowID The workflow ID associated with this dataset
     * @param dataSet DataSet on which PCA is performing (in JavaRDD<Vector> format)
     * @param noComponentsRetained Number of singular values retained after PCA operation
     * @throws DecompositionException
     */
    public void fitPCA(String workflowID, JavaRDD<Vector> dataSet, int noComponentsRetained)
            throws DecompositionException;

    /**
     * This method transforms a given dataset using pre-calculated PCA
     * @param workflowID The workflow ID associated with this dataset
     * @param data DataSet on which PCA is performing (in JavaRDD<Vector> format
     * @return Transformed dataset in JavaRDD<Vector> format
     * @throws DecompositionException
     */
    public JavaRDD<Vector> transformPCA(String workflowID, JavaRDD<Vector> data)
            throws DecompositionException;

    /**
     * This method is used to visualize a given dataset associated with
     * a given workflowID
     * @param workflowID WorkflowID the workflow ID associated with this dataset
     * @param response This represents the response variable of the dataset
     * @return Fits two principle components of the transformed dataset
     * @throws DecompositionException
     */
    public List<PCAResult> visualizePCA(String workflowID, String response)
            throws DecompositionException;
}
