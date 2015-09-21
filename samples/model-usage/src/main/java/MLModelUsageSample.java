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
        String[] featureValueArray1 = new String[] { "6", "148", "72", "35", "0", "33.6", "0.627", "50" };
        String[] featureValueArray2 = new String[] { "0", "101", "80", "40", "0", "26", "0.5", "33" };
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(featureValueArray1);
        list.add(featureValueArray2);
        modelUsageSample.predict(list, mlModel);
    }

    /**
     * Deserialize to MLModel object
     * @return
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
     * @param featureValueLists Feature values to be used for the prediction
     * @param mlModel           MLModel to be used for predictions
     * @throws MLModelHandlerException
     */
    public void predict(List<String[]> featureValueLists, MLModel mlModel) throws MLModelHandlerException {
        Predictor predictor = new Predictor(0, mlModel, featureValueLists);
        List<?> predictions = predictor.predict();
        logger.info("Predictions : " + predictions);
    }
}
