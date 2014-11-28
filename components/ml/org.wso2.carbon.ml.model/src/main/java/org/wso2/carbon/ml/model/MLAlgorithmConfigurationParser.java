/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.ml.model.constants.MLModelConstants;
import org.wso2.carbon.ml.model.dto.HyperParameter;
import org.wso2.carbon.ml.model.dto.MLAlgorithm;
import org.wso2.carbon.ml.model.dto.MLAlgorithms;
import org.wso2.carbon.ml.model.exceptions.MLAlgorithmConfigurationParserException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MLAlgorithmConfigurationParser {
    private MLAlgorithms mlAlgorithms;

    /**
     * @param xmlFilePath File path of ml-algorithms.xml file
     * @throws MLAlgorithmConfigurationParserException
     */
    protected MLAlgorithmConfigurationParser(String xmlFilePath) throws
            MLAlgorithmConfigurationParserException {
        try {
            File file = new File(xmlFilePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(MLAlgorithms.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            this.mlAlgorithms = (MLAlgorithms) jaxbUnmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException(
                    "An error occured while parsing " + MLModelConstants.ML_ALGORITHMS_CONFIG_XML
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * @param algorithm Machine learning algorithm name
     * @return Json array of hyper-parameters
     * @throws MLAlgorithmConfigurationParserException
     */
    protected JSONArray getHyperParameters(String algorithm)
            throws MLAlgorithmConfigurationParserException {
        try {
            JSONArray jsonArray = new JSONArray();
            for (MLAlgorithm mlAlgorithm : mlAlgorithms.getAlgorithms()) {
                if (algorithm.equals(mlAlgorithm.getName())) {
                    List<HyperParameter> hyperParameters = mlAlgorithm.getParameters();
                    for (HyperParameter hyperParameter : hyperParameters) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("parameter", hyperParameter.getParameter());
                        jsonObject.put("value", hyperParameter.getValue());
                        jsonArray.put(jsonObject);
                    }
                    break;
                }
            }
            return jsonArray;
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException(
                    "An error occurred while retrieving hyperparameters : " + e.getMessage(), e);
        }
    }

    /**
     * @param algorithmType Machine learning algorithm type - e.g. Classification
     * @return Machine learning algorithm names
     * @throws MLAlgorithmConfigurationParserException
     */
    protected List<String> getAlgorithms(String algorithmType) throws
            MLAlgorithmConfigurationParserException {
        try {
            List<String> algorithms = new ArrayList();
            for (MLAlgorithm algorithm : mlAlgorithms.getAlgorithms()) {
                if (algorithmType.equals(algorithm.getType())) {
                    algorithms.add(algorithm.getName());
                }
            }
            return algorithms;
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException(
                    "An error occurred while retrieving algorithm names: " + e.getMessage(), e);
        }
    }

    /**
     * @param algorithmType Machine learning algorithm type - e.g. Classification
     * @return Algorithm ratings
     * @throws MLAlgorithmConfigurationParserException
     */
    Map<String, List<Integer>> getAlgorithmRatings(String algorithmType)
            throws MLAlgorithmConfigurationParserException {
        try {
            Map<String, List<Integer>> ratings = new HashMap<String, List<Integer>>();
            for (MLAlgorithm algorithm : mlAlgorithms.getAlgorithms()) {
                if (algorithmType.equals(algorithm.getType())) {
                    List<Integer> scores = new ArrayList<Integer>();
                    scores.add(algorithm.getInterpretability());
                    scores.add(algorithm.getScalability());
                    scores.add(algorithm.getMulticollinearity());
                    scores.add(algorithm.getDimensionality());
                    ratings.put(algorithm.getName(), scores);
                }
            }
            return ratings;
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException(
                    "An error occurred while retrieving algorithm ratings: " + e.getMessage(), e);
        }
    }
}
