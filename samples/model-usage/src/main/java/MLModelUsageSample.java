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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.Predictor;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This Sample explains how to use a downloaded ML model for predictions
 */
public class MLModelUsageSample {

    private static final Log logger = LogFactory.getLog(MLModelUsageSample.class);

    public static void main(String[] args) throws Exception {

        MLModelUsageSample modelUsageSample = new MLModelUsageSample();

        // Path to downloaded-ml-model (downloaded-ml-model can be found inside resources folder)
        URL resource = MLModelUsageSample.class.getClassLoader().getResource("downloaded-ml-model");
        String pathToDownloadedModel = new File(resource.toURI()).getAbsolutePath();

        // Deserialize
        MLModel mlModel = modelUsageSample.deserializeMLModel(pathToDownloadedModel);

        // Predict
        String[] featureValueArray1 = new String[] { "2", "84", "0", "0", "0", "0.0", "0.304", "21" };
        String[] featureValueArray2 = new String[] { "0", "101", "80", "40", "0", "26", "0.5", "33" };
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(featureValueArray1);
        list.add(featureValueArray2);
        modelUsageSample.predict(list, mlModel);
    }

    /**
     * Deserialize to MLModel object
     * @return A {@link org.wso2.carbon.ml.commons.domain.MLModel} object
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    private MLModel deserializeMLModel(String pathToDownloadedModel) throws IOException, ClassNotFoundException, URISyntaxException {

        FileInputStream fileInputStream = new FileInputStream(pathToDownloadedModel);
        ObjectInputStream in = new ObjectInputStream(fileInputStream);
        MLModel mlModel = (MLModel) in.readObject();

        logger.info("Algorithm Type : " + mlModel.getAlgorithmClass());
        logger.info("Algorithm Name : " + mlModel.getAlgorithmName());
        logger.info("Response Variable : " + mlModel.getResponseVariable());
        logger.info("Features : " + mlModel.getFeatures());
        return mlModel;
    }

    /**
     * Make Predictions using Predictor
     * @param featureValuesList Feature values to be used for the prediction
     * @param mlModel           MLModel to be used for predictions
     * @throws MLModelHandlerException
     */
    public void predict(List<String[]> featureValuesList, MLModel mlModel) throws MLModelHandlerException {

        // Validate number of features
        // Number of feature values in the array should be same as the number of features in the model
        for (String[] featureValues : featureValuesList) {
            if (featureValues.length != mlModel.getFeatures().size()) {
                logger.error("Number of features in the array does not match the number of features in the model.");
                return;
            }
        }

        Predictor predictor = new Predictor(0, mlModel, featureValuesList);
        List<?> predictions = predictor.predict();

        logger.info("Feature values list 1 : " + Arrays.toString(featureValuesList.get(0)));
        logger.info("Prediction : " + predictions.get(0));

        logger.info("Feature values list 2 : " + Arrays.toString(featureValuesList.get(1)));
        logger.info("Prediction : " + predictions.get(1));
    }
}
