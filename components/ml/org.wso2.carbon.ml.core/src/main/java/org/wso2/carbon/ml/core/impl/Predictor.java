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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LassoModel;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.RidgeRegressionModel;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants.SUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.constants.MLConstants.UNSUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.transformations.DoubleArrayToVector;
import org.wso2.carbon.ml.core.utils.MLUtils;

/**
 * Predict using a input data row.
 */
public class Predictor {

    private static final Log log = LogFactory.getLog(Predictor.class);
    private long id;
    private MLModelConfigurationContext ctxt;
    private MLModel model;

    public Predictor(long modelId, MLModel mlModel, MLModelConfigurationContext context) {
        id = modelId;
        ctxt = context;
        model = mlModel;
    }

    public List<?> predict() {
        try {
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            Workflow facts = ctxt.getFacts();
            String algorithmType = facts.getAlgorithmClass();
            if (MLConstants.CLASSIFICATION.equals(algorithmType)
                    || MLConstants.NUMERICAL_PREDICTION.equals(algorithmType)) {
                
                SUPERVISED_ALGORITHM supervisedAlgorithm = SUPERVISED_ALGORITHM.valueOf(facts.getAlgorithmName());
                
                JavaSparkContext context = ctxt.getSparkContext();
                List<double[]> dataList = new ArrayList<double[]>();
                dataList.add(MLUtils.toDoubleArray(ctxt.getDataToBePredicted()));
                context.parallelize(dataList);
                JavaRDD<double[]> dataRDD = context.parallelize(dataList);
                JavaRDD<Vector> dataVector = dataRDD.map(new DoubleArrayToVector());
                
                switch (supervisedAlgorithm) {
                case LOGISTIC_REGRESSION:
                    LogisticRegressionModel logisticModel = (LogisticRegressionModel) model.getModel();                    
                    JavaRDD<Double> predictedData = logisticModel.predict(dataVector);
                    List<Double> predictedDataList = predictedData.collect();
                    for (Double double1 : predictedDataList) {
                        log.info("Prediction: " + double1);
                    }
                    return predictedDataList;
                case DECISION_TREE:
                    break;
                case SVM:
                    break;
                case NAIVE_BAYES:
                    break;
                case LINEAR_REGRESSION:
                    LinearRegressionModel lrModel = (LinearRegressionModel)model.getModel();
                    JavaRDD<Double> testingData = lrModel.predict(dataVector);
                    List<Double> predictions = testingData.collect();
                    for (Double prediction : predictions) {
                        log.info("Prediction: " + prediction);
                    }
                    return predictions;                    
                case RIDGE_REGRESSION:
                    RidgeRegressionModel ridgeModel = (RidgeRegressionModel)model.getModel();
                    JavaRDD<Double> ridgeTestingData = ridgeModel.predict(dataVector);
                    List<Double> ridgePredictions = ridgeTestingData.collect();
                    for (Double prediction : ridgePredictions) {
                        log.info("Prediction: " + prediction);
                    }
                    return ridgePredictions;                    
                case LASSO_REGRESSION:
                    LassoModel lassoModel = (LassoModel)model.getModel();
                    JavaRDD<Double> lassoTestingData = lassoModel.predict(dataVector);
                    List<Double> lassoPredictions = lassoTestingData.collect();
                    for (Double prediction : lassoPredictions) {
                        log.info("Prediction: " + prediction);
                    }
                    return lassoPredictions; 
                default:
                    throw new AlgorithmNameException("Incorrect algorithm name");
                }
                
            } else if (MLConstants.CLUSTERING.equals((algorithmType))) {
                UNSUPERVISED_ALGORITHM unsupervised_algorithm = UNSUPERVISED_ALGORITHM.valueOf(facts.getAlgorithmName());
                switch (unsupervised_algorithm) {
                case K_MEANS:
                    //TODO
                    break;
                default:
                    throw new AlgorithmNameException("Incorrect algorithm name: "+facts.getAlgorithmName()+" for model id: "+id);
                }
            } else {
                throw new MLModelBuilderException(String.format(
                        "Failed to build the model [id] %s . Invalid algorithm type: %s", id, algorithmType));
            }

        } catch (Exception e) {
            log.error(String.format("Failed to predict from the model [id] %s ", id), e);
        }
        return new ArrayList<String>();
    }

}
