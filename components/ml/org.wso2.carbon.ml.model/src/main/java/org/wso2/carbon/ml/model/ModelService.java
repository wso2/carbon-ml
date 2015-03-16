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

package org.wso2.carbon.ml.model;

import org.wso2.carbon.ml.commons.domain.ClusterPoint;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import org.wso2.carbon.ml.commons.domain.config.HyperParameter;
import org.wso2.carbon.ml.core.spark.ConfusionMatrix;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.internal.dto.ModelSettings;

import java.util.List;
import java.util.Map;

public interface ModelService {
    /**
     * Retrieve the hyperparameters of an ML algorithm.
     * 
     * @param algorithm     Name of the machine learning algorithm
     * @return              List containing hyper parameters
     * @throws              ModelServiceException
     */
    public List<HyperParameter> getHyperParameters(String algorithm) throws ModelServiceException;

    /**
     * Retrieve ML algorithms belongs to a particular Category (i.e. Classification, Numerical Prediction, Clustering).
     * 
     * @param algorithmType     Type of the machine learning algorithm - e.g. Classification
     * @return                  List of algorithm names
     * @throws                  ModelServiceException
     */
    public List<String> getAlgorithmsByType(String algorithmType) throws ModelServiceException;

    /**
     * Retrieve recommended algorithms.
     * 
     * @param algorithmType     Type of the machine learning algorithm - e.g. Classification
     * @param userResponse      User's response to a questionnaire about machine learning task
     * @return                  Map containing names of recommended machine learning algorithms and
     *                          recommendation scores (out of 5) for each algorithm
     * @throws                  ModelServiceException
     */
    public Map<String, Double> getRecommendedAlgorithms(String algorithmType,
            Map<String, String> userResponse) throws ModelServiceException;

    /**
     * Build a model using the configuration of a workflow.
     * 
     * @param modelID       Model ID
     * @param workflowID    Workflow ID
     * @throws              ModelServiceException
     */
    public void buildModel(String modelID, String workflowID) throws ModelServiceException;
    
    /**
     * @param workflowID Workflow ID
     * @return model id.
     * @throws ModelServiceException
     */
    public String buildModel(String workflowID) throws ModelServiceException;

    /**
     * Retrieve a summary of a built model.
     * 
     * @param modelID   Model ID
     * @return          Model summary object
     * @throws          ModelServiceException
     */
    public ModelSummary getModelSummary(String modelID) throws ModelServiceException;

    /**
     * Retrieve the model using the ID.
     * 
     * @param modelID   Model ID
     * @return          {@link MLModel} object
     * @throws          ModelServiceException
     */
    public MLModel getModel(String modelID) throws ModelServiceException;

    /**
     * Save the model configurations.
     * 
     * @param modelSettings     Model settings
     * @throws                  ModelServiceException
     */
    public void insertModelSettings(ModelSettings modelSettings) throws ModelServiceException;
    
    /**
     * Save model configurations.
     * 
     * @param modelSettings     Model settings
     * @throws                  ModelServiceException
     */
    public void insertModelSettings(String workflowId, ModelSettings modelSettings) throws ModelServiceException;

    /**
     * This method checks whether model execution is completed or not.
     *
     * @param modelID   Unique Identifier of the Model
     * @return          Indicates whether model execution is completed or not
     * @throws          ModelServiceException
     */
    public boolean isExecutionCompleted(String modelID) throws ModelServiceException;

    /**
     * This method checks whether model execution is started or not.
     *
     * @param modelID   Unique Identifier of the Model
     * @return          Indicates whether model execution is started or not
     * @throws          ModelServiceException
     */
    public boolean isExecutionStarted(String modelID) throws ModelServiceException;

   /**
    * Returns a confusion matrix for a given threshold.
    *
    * @param modelID       Unique Identifier of the Model
    * @param threshold     Probability threshold
    * @return              Returns a confusion matrix object
    * @throws              ModelServiceException
    */
    public ConfusionMatrix getConfusionMatrix(String modelID, double threshold)
            throws ModelServiceException;

    /**
     * This method retuns a list of k-means cluster points.
     *
     * @param datasetURL    Dataset URL
     * @param features      List containing feature names
     * @param noOfClusters  Number of clusters
     * @return              Returns a list of cluster points
     * @throws              ModelServiceException
     */
    public List<ClusterPoint> getClusterPoints(String datasetURL, List<String> features, int noOfClusters)
            throws ModelServiceException;
}
