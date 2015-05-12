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
import org.apache.spark.mllib.classification.ClassificationModel;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.GeneralizedLinearModel;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.constants.MLConstants.SUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.constants.MLConstants.UNSUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

/**
 * Predict using input data rows.
 */
public class Predictor {

    private static final Log log = LogFactory.getLog(Predictor.class);
    private long id;
    private MLModel model;
    private List<Vector> dataToBePredicted;

    public Predictor(long modelId, MLModel mlModel, List<double[]> data) {
        id = modelId;
        model = mlModel;
        dataToBePredicted = getVectors(data);
    }

    public List<?> predict() throws MLModelBuilderException {
        String algorithmType = model.getAlgorithmClass();

        if (MLConstants.CLASSIFICATION.equals(algorithmType)) {
            SUPERVISED_ALGORITHM supervised_algorithm = SUPERVISED_ALGORITHM.valueOf(model.getAlgorithmName());
            List<Double> predictions = new ArrayList<Double>();
            switch (supervised_algorithm) {
            case DECISION_TREE:
                DecisionTreeModel decisionTreeModel = (DecisionTreeModel) model.getModel();
                for (Vector vector : dataToBePredicted) {

                    double predictedData = decisionTreeModel.predict(vector);
                    predictions.add(predictedData);
                    log.info("Prediction: " + predictedData);
                }
            default:
                ClassificationModel classificationModel = (ClassificationModel) model.getModel();
                for (Vector vector : dataToBePredicted) {

                    double predictedData = classificationModel.predict(vector);
                    predictions.add(predictedData);
                    log.info("Prediction: " + predictedData);
                }
            }
            return predictions;

        } else if (MLConstants.NUMERICAL_PREDICTION.equals(algorithmType)) {
            GeneralizedLinearModel generalizedLinearModel = (GeneralizedLinearModel) model.getModel();
            List<Double> predictions = new ArrayList<Double>();
            for (Vector vector : dataToBePredicted) {

                double predictedData = generalizedLinearModel.predict(vector);
                predictions.add(predictedData);
                log.info("Prediction: " + predictedData);
            }
            return predictions;
        } else if (MLConstants.CLUSTERING.equals((algorithmType))) {
            UNSUPERVISED_ALGORITHM unsupervised_algorithm = UNSUPERVISED_ALGORITHM.valueOf(model.getAlgorithmName());
            switch (unsupervised_algorithm) {
            case K_MEANS:
                List<Integer> predictions = new ArrayList<Integer>();
                KMeansModel kMeansModel = (KMeansModel) model.getModel();
                for (Vector vector : dataToBePredicted) {

                    int predictedData = kMeansModel.predict(vector);
                    predictions.add(predictedData);
                    log.info("Prediction: " + predictedData);
                }
                return predictions;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name: " + model.getAlgorithmName()
                        + " for model id: " + id);
            }
        } else {
            throw new MLModelBuilderException(String.format(
                    "Failed to build the model [id] %s . Invalid algorithm type: %s", id, algorithmType));
        }
    }

    private List<Vector> getVectors(List<double[]> data) {
        List<Vector> vectors = new ArrayList<Vector>();
        for (double[] ds : data) {
            Vector vector = new DenseVector(ds);
            vectors.add(vector);
        }
        return vectors;
    }

}
