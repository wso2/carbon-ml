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
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.wso2.carbon.ml.core.spark.transformations.OneHotEncoder;
import org.wso2.carbon.ml.core.utils.MLUtils;

/**
 * Predict using input data rows.
 */
public class Predictor {

    private static final Log log = LogFactory.getLog(Predictor.class);
    private long id;
    private MLModel model;
    private List<Vector> dataToBePredicted;

    public Predictor(long modelId, MLModel mlModel, List<String[]> data) {
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
                    log.info("Predicted value before decoding: " + predictedData);
                }
                return decodePredictedValues(predictions);
            default:
                ClassificationModel classificationModel = (ClassificationModel) model.getModel();
                for (Vector vector : dataToBePredicted) {

                    double predictedData = classificationModel.predict(vector);
                    predictions.add(predictedData);
                    log.info("Predicted value before decoding: " + predictedData);
                }
                return decodePredictedValues(predictions);
            }

        } else if (MLConstants.NUMERICAL_PREDICTION.equals(algorithmType)) {
            GeneralizedLinearModel generalizedLinearModel = (GeneralizedLinearModel) model.getModel();
            List<Double> predictions = new ArrayList<Double>();
            for (Vector vector : dataToBePredicted) {

                double predictedData = generalizedLinearModel.predict(vector);
                predictions.add(predictedData);
                log.info("Predicted value before decoding: " + predictedData);
            }
            return decodePredictedValues(predictions);

        } else if (MLConstants.CLUSTERING.equals((algorithmType))) {
            UNSUPERVISED_ALGORITHM unsupervised_algorithm = UNSUPERVISED_ALGORITHM.valueOf(model.getAlgorithmName());
            switch (unsupervised_algorithm) {
            case K_MEANS:
                List<Integer> predictions = new ArrayList<Integer>();
                KMeansModel kMeansModel = (KMeansModel) model.getModel();
                for (Vector vector : dataToBePredicted) {

                    int predictedData = kMeansModel.predict(vector);
                    predictions.add(predictedData);
                    log.info("Predicted value before decoding: " + predictedData);
                }
                return decodePredictedValues(predictions);
            default:
                throw new AlgorithmNameException("Incorrect algorithm name: " + model.getAlgorithmName()
                        + " for model id: " + id);
            }
        } else {
            throw new MLModelBuilderException(String.format(
                    "Failed to build the model [id] %s . Invalid algorithm type: %s", id, algorithmType));
        }
    }

    private List<Vector> getVectors(List<String[]> data) {
        List<Vector> vectors = new ArrayList<Vector>();
        List<Map<String, Integer>> encodings = model.getEncodings();
        OneHotEncoder encoder = new OneHotEncoder(encodings);
        for (String[] dataEntry : data) {
            String[] encodedEntry;
            try {
                encodedEntry = encoder.call(dataEntry);
            } catch (Exception e) {
                log.warn("Data encoding failed. Cause: " + e.getMessage());
                encodedEntry = dataEntry;
            }
            double[] doubleValues = MLUtils.toDoubleArray(encodedEntry);
            Vector vector = new DenseVector(doubleValues);
            vectors.add(vector);
        }
        return vectors;
    }

    // write a method to decode the predicted value
    private List<?> decodePredictedValues(List<?> predictions) {
        int index = model.getResponseIndex();
        if (index == -1) {
            return predictions;
        }
        List<Map<String, Integer>> encodings = model.getEncodings();
        Map<String, Integer> encodingMap = encodings.get(index);
        if (encodingMap == null || encodingMap.isEmpty()) {
            // no change
            return predictions;
        } else {
            List<String> decodedPredictions = new ArrayList<String>();
            for (Object val : predictions) {
                int roundedValue;
                if (val instanceof Double) {
                    roundedValue = (int) Math.round((Double) val);
                } else if (val instanceof Integer) {
                    roundedValue = (Integer) val;
                } else {
                    // fail to recognize the value, stop decoding
                    return predictions;
                }
                String decodedValue = decode(encodingMap, roundedValue);
                log.info("Predicted value after decoding: " + decodedValue);
                decodedPredictions.add(decodedValue);
            }
            return decodedPredictions;
        }
    }

    private String decode(Map<String, Integer> encodingMap, int roundedValue) {
        // first try to find the exact matching entry
        String classVal = findClass(encodingMap, roundedValue);
        if (classVal != null) {
            return classVal;
        }
        // if it is not succeeded, try to find the closest entry
        roundedValue = closest(roundedValue, encodingMap.values());
        findClass(encodingMap, roundedValue);
        return String.valueOf(roundedValue);
    }

    private String findClass(Map<String, Integer> encodingMap, int roundedValue) {
        for (Map.Entry<String, Integer> entry : encodingMap.entrySet()) {
            if (roundedValue == entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    
    
    public int closest(int of, Collection<Integer> in) {
        int min = Integer.MAX_VALUE;
        int closest = of;

        for (int v : in) {
            final int diff = Math.abs(v - of);

            if (diff < min) {
                min = diff;
                closest = v;
            }
        }

        return closest;
    }

}
