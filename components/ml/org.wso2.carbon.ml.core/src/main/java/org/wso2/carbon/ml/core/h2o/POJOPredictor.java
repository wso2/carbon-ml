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

package org.wso2.carbon.ml.core.h2o;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.utils.CarbonUtils;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.AbstractPredictException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

public class POJOPredictor {
    List<Feature> featureList;
    MLModel mlModel;
    private hex.genmodel.GenModel rawModel;
    private EasyPredictModelWrapper model;
    private int numberOfFeatures;

    public POJOPredictor(MLModel mlModel, String path) throws MLModelHandlerException {
        String dlModelName = extractModelName(path);
        dlModelName = dlModelName.replace('.', '_').replace('-', '_');
        String carbonHome = CarbonUtils.getCarbonHome();
        File f = new File(carbonHome + MLConstants.H2O_POJO_Path);

        try {
            URL[] cp = { f.toURI().toURL(),
                    new File(carbonHome + MLConstants.H2O_POJO_Path + "h2o-genmodel.jar").toURI().toURL() };
            URLClassLoader urlcl = new URLClassLoader(cp, this.getClass().getClassLoader());

            rawModel = (hex.genmodel.GenModel) urlcl.loadClass(dlModelName).newInstance();
            model = new EasyPredictModelWrapper(rawModel);

            numberOfFeatures = mlModel.getFeatures().size();
            featureList = mlModel.getFeatures();
            this.mlModel = mlModel;

        } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new MLModelHandlerException("Error occurred while initializing POHOPredictor.", e);
        }
    }

    public Object predict(String[] featureVector) throws MLModelHandlerException {
        RowData row = new RowData();
        for (int i = 0; i < numberOfFeatures; i++) {
            row.put(featureList.get(i).getName(), featureVector[i]);
        }

        if (model.getModelCategory().name().equalsIgnoreCase("multinomial")) {
            try {
                MultinomialModelPrediction p = model.predictMultinomial(row);
                return decodePredictedValue(Double.parseDouble(p.label), mlModel);
            } catch (AbstractPredictException abstractPredictionException) {
                throw new MLModelHandlerException("Error occurred while predicting.", abstractPredictionException);
            }
        } else if (model.getModelCategory().name().equalsIgnoreCase("binomial")) {
            try {
                BinomialModelPrediction p = model.predictBinomial(row);
                return decodePredictedValue(Double.parseDouble(p.label), mlModel);
            } catch (AbstractPredictException abstractPredictionException) {
                throw new MLModelHandlerException("Error occurred while predicting.", abstractPredictionException);
            }
        } else {
            throw new MLModelHandlerException("Unsupported deep learning model:" + model.getModelCategory());
        }
    }

    private Object decodePredictedValue(double prediction, MLModel model) {
        int index = model.getResponseIndex();
        if (index == -1) {
            return prediction;
        }
        List<Map<String, Integer>> encodings = model.getEncodings();
        // last index is response variable encoding
        Map<String, Integer> encodingMap = encodings.get(encodings.size() - 1);
        if (encodingMap == null || encodingMap.isEmpty()) {
            // no change
            return prediction;
        } else {
            int roundedValue;
            roundedValue = (int) Math.round(prediction);
            String decodedValue = decode(encodingMap, roundedValue);
            return decodedValue;
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

    private int closest(int of, Collection<Integer> in) {
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

    private String extractModelName(String modelStoragePath) {
        int index = modelStoragePath.lastIndexOf(File.separator);
        return modelStoragePath.substring(index + 1);
    }
}
