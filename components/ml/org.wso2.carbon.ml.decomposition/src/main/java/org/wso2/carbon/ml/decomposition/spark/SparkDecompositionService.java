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
package org.wso2.carbon.ml.decomposition.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.decomposition.DecompositionService;
import org.wso2.carbon.ml.decomposition.exceptions.DecompositionException;
import org.wso2.carbon.ml.decomposition.spark.dto.PCAResult;
import org.wso2.carbon.ml.decomposition.spark.transformations.DataPointToFeatureMapper;
import org.wso2.carbon.ml.decomposition.spark.transformations.DataPointToResponseMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the spark implementation of the services offered by
 * decomposition service.
 */
public class SparkDecompositionService implements DecompositionService {

    /**
     * This method performs PCA for a given dataset
     * @param workflowID The workflow ID associated with this dataset
     * @param dataSet DataSet on which PCA is performing (in JavaRDD<Vector> format)
     * @param noComponentsRetained Number of singular values retained after PCA operation
     * @throws DecompositionException
     */
    @Override
    public void fitPCA(String workflowID,JavaRDD<Vector> dataSet,  int noComponentsRetained)
            throws DecompositionException {
        if(workflowID == null || workflowID.length() == 0){
            throw new DecompositionException(
                "Argument: workflowId is either null or empty");
        }

        if(dataSet == null){
            throw new DecompositionException("Argument: dataSet is null for workflow Id: "+workflowID);
        }

        if(noComponentsRetained <= 0){
            throw new DecompositionException(
                "Argument: noComponentsRetained is either zero or negative for workflow ID: "+workflowID);
        }

        RowMatrix dataMatrix= new RowMatrix(dataSet.rdd());
        Matrix pcaTransFormedMatrix = dataMatrix.computePrincipalComponents(noComponentsRetained);
        SparkDecompositionServiceUtil.saveMatrix(workflowID, pcaTransFormedMatrix);
    }

    /**
     * This method transforms a given dataset using pre-calculated PCA
     * @param workflowID The workflow ID associated with this dataset
     * @param dataSet DataSet on which PCA is performing (in JavaRDD<Vector> format
     * @return Transformed dataset in JavaRDD<Vector> format
     * @throws DecompositionException
     */
    @Override
    public JavaRDD<Vector> transformPCA(String workflowID, JavaRDD<Vector> dataSet)
            throws DecompositionException {
        if(workflowID == null || workflowID.length() == 0){
            throw new DecompositionException(
                    "Argument: workflowId is either null or empty");
        }

        if(dataSet == null){
            throw new DecompositionException("Argument: dataSet is null for workflow ID: "+workflowID);
        }

        RowMatrix dataMatrix = new RowMatrix(dataSet.rdd());
        Matrix principleComponents = SparkDecompositionServiceUtil.loadMatrix(workflowID);
        if(principleComponents == null){
            throw new DecompositionException("" +
                    "PCA matrix is null for workflow ID: "+workflowID);
        }
        RowMatrix projectedMatrix = dataMatrix.multiply(principleComponents);
        return projectedMatrix.rows().toJavaRDD();
    }

    /**
     * This method is used to visualize a given dataset associated with
     * a given workflowID
     * @param workflowID The workflow ID associated with this dataset
     * @response Name of the response variable
     * @return Fits two principle components of the transformed dataset
     * @throws DecompositionException
     */
    @Override
    public List<PCAResult> visualizePCA(String workflowID, String response)
            throws DecompositionException {
        if(workflowID == null || workflowID.length() == 0){
            throw new DecompositionException(
                    "Argument: workflowId is either null or empty");
        }

        if(response == null || response.length() == 0){
            throw new DecompositionException(
                    "Argument: response is either null or empty for workflow ID: "+workflowID);
        }

        JavaRDD<LabeledPoint> dataSet = SparkDecompositionServiceUtil.getSamplePoints(workflowID, response);
        JavaRDD<Double> labelsRDD = dataSet.map(new DataPointToResponseMapper());
        JavaRDD<Vector> features = dataSet.map(new DataPointToFeatureMapper());
        RowMatrix dataMatrix = new RowMatrix(features.rdd());

        // extracting first two principle components for visualization
        Matrix pcaTransFormedMatrix = dataMatrix.computePrincipalComponents(2);
        RowMatrix projectedMatrix = dataMatrix.multiply(pcaTransFormedMatrix);

        List<Vector> pcaDataPoints = projectedMatrix.rows().toJavaRDD().toArray();
        List<Double> labels = labelsRDD.toArray();

        List<PCAResult> pcaResults = new ArrayList<PCAResult>();
        for(int i=0; i<pcaDataPoints.size();i++){
            double[] pcaData = pcaDataPoints.get(i).toArray();
            double label = labels.get(i);
            pcaResults.add(new PCAResult(pcaData[0], pcaData[1], label));
        }
        return pcaResults;
    }
}
