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
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.metrics.manager.Timer.Context;
import org.wso2.carbon.ml.commons.constants.MLConstants.ANOMALY_DETECTION_ALGORITHM;
import org.wso2.carbon.ml.commons.constants.MLConstants.DEEPLEARNING_ALGORITHM;
import org.wso2.carbon.ml.commons.constants.MLConstants.SUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.constants.MLConstants.UNSUPERVISED_ALGORITHM;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.factories.AlgorithmType;
import org.wso2.carbon.ml.core.spark.models.*;
import org.wso2.carbon.ml.core.spark.transformations.BasicEncoder;
import org.wso2.carbon.ml.core.spark.transformations.Normalization;
import org.wso2.carbon.ml.core.utils.DeeplearningModelUtils;
import org.wso2.carbon.ml.core.utils.MLUtils;

import water.fvec.Frame;

/**
 * Predict using input data rows.
 */
public class Predictor {

    private static final Log log = LogFactory.getLog(Predictor.class);
    private long id;
    private MLModel model;
    private List<Vector> dataToBePredicted;
    // for K means anomaly detection
    private double percentileValue;

    public Predictor(long modelId, MLModel mlModel, List<String[]> data) {
        id = modelId;
        model = mlModel;
        dataToBePredicted = getVectors(data);
    }

    public Predictor(long modelId, MLModel mlModel, List<String[]> data, double percentile) {
        id = modelId;
        model = mlModel;
        dataToBePredicted = getVectors(data);
        percentileValue = percentile;
    }

    public List<?> predict() throws MLModelHandlerException {
        String algorithmType = model.getAlgorithmClass();
        AlgorithmType type = AlgorithmType.getAlgorithmType(algorithmType);

        org.wso2.carbon.metrics.manager.Timer timer = getTimer(model.getAlgorithmName());

        if (AlgorithmType.CLASSIFICATION == type) {
            SUPERVISED_ALGORITHM supervised_algorithm = SUPERVISED_ALGORITHM.valueOf(model.getAlgorithmName());
            List<Double> predictions = new ArrayList<Double>();
            switch (supervised_algorithm) {
            case DECISION_TREE:
                DecisionTreeModel decisionTreeModel = ((MLDecisionTreeModel) model.getModel()).getModel();
                for (Vector vector : dataToBePredicted) {
                    Context context = startTimer(timer);

                    double predictedData = decisionTreeModel.predict(vector);
                    predictions.add(predictedData);

                    stopTimer(context);
                    if (log.isDebugEnabled()) {
                        log.debug("Predicted value before decoding: " + predictedData);
                    }
                }

                return decodePredictedValues(predictions);
            case RANDOM_FOREST:
                RandomForestModel randomForestModel = ((MLRandomForestModel) model.getModel()).getModel();
                for (Vector vector : dataToBePredicted) {
                    Context context = startTimer(timer);

                    double predictedData = randomForestModel.predict(vector);
                    predictions.add(predictedData);

                    stopTimer(context);
                    if (log.isDebugEnabled()) {

                        log.debug("Predicted value before decoding: " + predictedData);
                    }
                }
                return decodePredictedValues(predictions);
            default:
                ClassificationModel classificationModel = ((MLClassificationModel) model.getModel()).getModel();
                for (Vector vector : dataToBePredicted) {
                    Context context = startTimer(timer);

                    double predictedData = classificationModel.predict(vector);
                    predictions.add(predictedData);

                    stopTimer(context);

                    if (log.isDebugEnabled()) {

                        log.debug("Predicted value before decoding: " + predictedData);
                    }
                }
                return decodePredictedValues(predictions);
            }

        } else if (AlgorithmType.NUMERICAL_PREDICTION == type) {
            GeneralizedLinearModel generalizedLinearModel = ((MLGeneralizedLinearModel) model.getModel()).getModel();
            List<Double> predictions = new ArrayList<Double>();
            for (Vector vector : dataToBePredicted) {
                Context context = startTimer(timer);

                double predictedData = generalizedLinearModel.predict(vector);
                predictions.add(predictedData);

                stopTimer(context);

                if (log.isDebugEnabled()) {

                    log.debug("Predicted value before decoding: " + predictedData);
                }
            }
            return decodePredictedValues(predictions);

        } else if (AlgorithmType.CLUSTERING == type) {
            UNSUPERVISED_ALGORITHM unsupervised_algorithm = UNSUPERVISED_ALGORITHM.valueOf(model.getAlgorithmName());
            switch (unsupervised_algorithm) {
            case K_MEANS:
                List<Integer> predictions = new ArrayList<Integer>();
                KMeansModel kMeansModel = (KMeansModel) model.getModel();
                for (Vector vector : dataToBePredicted) {
                    Context context = startTimer(timer);

                    int predictedData = kMeansModel.predict(vector);
                    predictions.add(predictedData);

                    stopTimer(context);

                    if (log.isDebugEnabled()) {

                        log.debug("Predicted value before decoding: " + predictedData);
                    }
                }
                return decodePredictedValues(predictions);
            default:
                throw new AlgorithmNameException(
                        "Incorrect algorithm name: " + model.getAlgorithmName() + " for model id: " + id);
            }
        } else if (AlgorithmType.ANOMALY_DETECTION == type) {
            ANOMALY_DETECTION_ALGORITHM anomaly_detection_algorithm = ANOMALY_DETECTION_ALGORITHM
                    .valueOf(model.getAlgorithmName());
            switch (anomaly_detection_algorithm) {
            case K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA:
            case K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA:

                List<String> predictions = new ArrayList<String>();
                MLAnomalyDetectionModel mLAnomalyDetectionModel = (MLAnomalyDetectionModel) model.getModel();

                Normalization normalization = null;
                if (model.getNormalization()) {
                    normalization = new Normalization.Builder()
                            .minMax(model.getFeatures(), model.getSummaryStatsOfFeatures()).build();
                }

                for (Vector vector : dataToBePredicted) {

                    Context context = startTimer(timer);

                    if (model.getNormalization()) {
                        double[] data = vector.toArray();
                        double[] normalizedData;

                        try {
                            normalizedData = normalization.call(data);
                        } catch (MLModelBuilderException e) {
                            log.warn("Data normalization failed for data: " + data + " Cause: " + e.getMessage());
                            normalizedData = data;
                        }
                        vector = new DenseVector(normalizedData);
                    }

                    String predictedValue = mLAnomalyDetectionModel.getModel().predict(vector, percentileValue);
                    predictions.add(predictedValue);

                    stopTimer(context);
                    if (log.isDebugEnabled()) {

                        log.debug("Predicted value before decoding: " + predictedValue);
                    }
                }
                return predictions;
            default:
                throw new AlgorithmNameException(
                        "Incorrect algorithm name: " + model.getAlgorithmName() + " for model id: " + id);
            }
        } else if (AlgorithmType.DEEPLEARNING == type) {
            DEEPLEARNING_ALGORITHM deeplearning_algorithm = DEEPLEARNING_ALGORITHM.valueOf(model.getAlgorithmName());
            switch (deeplearning_algorithm) {
            case STACKED_AUTOENCODERS:
                List<Double> predictions = new ArrayList<Double>();
                MLDeeplearningModel saeModel = ((MLDeeplearningModel) model.getModel());
                List<double[]> tobePredictedList = new ArrayList<double[]>();
                for (Vector vector : dataToBePredicted) {
                    tobePredictedList.add(vector.toArray());
                }
                Frame predFrame = DeeplearningModelUtils.doubleArrayListToFrame(tobePredictedList);
                double[] predictedData = saeModel.predict(predFrame);

                for (double pVal : predictedData) {
                    predictions.add((Double) pVal);
                }
                return decodePredictedValues(predictions);
            default:
                throw new AlgorithmNameException(
                        "Incorrect algorithm name: " + model.getAlgorithmName() + " for model id: " + id);
            }
        } else {
            throw new MLModelHandlerException(
                    String.format("Failed to build the model [id] %s . Invalid algorithm type: %s", id, algorithmType));
        }
    }

    private void stopTimer(Context context) {
        if (context != null) {
            context.stop();
        }
    }

    private Context startTimer(Timer timer) {
        if (timer != null) {
            return timer.start();
        }
        return null;
    }

    private org.wso2.carbon.metrics.manager.Timer getTimer(String algorithmName) {
        try {
            org.wso2.carbon.metrics.manager.Timer timer = MetricManager.timer(Level.INFO,
                    "org.wso2.carbon.ml.prediction-time." + model.getAlgorithmName());
            return timer;
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    private List<Vector> getVectors(List<String[]> data) {
        List<Vector> vectors = new ArrayList<Vector>();
        List<Map<String, Integer>> encodings = model.getEncodings();
        BasicEncoder encoder = new BasicEncoder.Builder().encodings(encodings).build();
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
        // last index is response variable encoding
        Map<String, Integer> encodingMap = encodings.get(encodings.size() - 1);
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
                if (log.isDebugEnabled()) {
                    log.debug("Predicted value after decoding: " + decodedValue);
                }
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
