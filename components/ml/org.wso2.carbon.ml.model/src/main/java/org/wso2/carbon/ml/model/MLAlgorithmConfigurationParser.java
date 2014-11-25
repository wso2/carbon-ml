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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MLAlgorithmConfigurationParser {

    private Document mlAlgorithmsConfig;

    MLAlgorithmConfigurationParser() throws MLAlgorithmConfigurationParserException {
        try {
            XMLParser xmlParser = new XMLParser();
            mlAlgorithmsConfig = xmlParser.getXMLDocument(MLModelConstants.ML_ALGORITHMS_CONFIG_XML);
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException("An error occured while parsing " +
                                                              MLModelConstants
                                                                      .ML_ALGORITHMS_CONFIG_XML +
                                                              ": " + e.getMessage(), e);
        }
    }

    /**
     * @param algorithm Machine learning algorithm name
     * @return hyper-parameters
     * @throws MLAlgorithmConfigurationParserException
     */
    JSONArray getHyperParameters(String algorithm)
            throws MLAlgorithmConfigurationParserException {
        try {
            JSONArray hyperparameters = null;
            NodeList nodes = mlAlgorithmsConfig.getElementsByTagName(MLModelConstants.NAME);
            StreamResult xmlOutput;
            Transformer transformer;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node nNode = nodes.item(i);
                if (nNode.getTextContent().equals(algorithm)) {
                    String parameterString = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    Node parent = nNode.getParentNode();
                    for (int j = 0; j < parent.getChildNodes().getLength(); j++) {
                        Node child = parent.getChildNodes().item(j);
                        if (MLModelConstants.PARAMETERS.equals(child.getNodeName())) {
                            xmlOutput = new StreamResult(new StringWriter());
                            transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                                                          MLModelConstants.YES);
                            transformer.transform(new DOMSource(child), xmlOutput);
                            stringBuilder.append(xmlOutput.getWriter().toString());
                        }
                    }
                    parameterString = stringBuilder.toString();
                    JSONObject xmlJSONObject = XML.toJSONObject(parameterString);
                    Object parameters = xmlJSONObject.get(MLModelConstants.PARAMETERS);
                    if (parameters instanceof JSONArray) {
                        hyperparameters = (JSONArray) parameters;
                    } else {
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.put(0, parameters);
                        hyperparameters = jsonArray;
                    }
                    break;
                }
            }
            return hyperparameters;
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException("An error occurred while retrieving" +
                                                              " hyperparameters : " + e
                    .getMessage(), e);
        }
    }

    /**
     * @param algorithmType
     * @return Machine learning algorithm names
     * @throws MLAlgorithmConfigurationParserException
     */
    List<String> getAlgorithms(String algorithmType) throws
                                                     MLAlgorithmConfigurationParserException {
        try {
            List<String> algorithms = new ArrayList();
            NodeList nodes = mlAlgorithmsConfig.getElementsByTagName(MLModelConstants.TYPE);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node nNode = nodes.item(i);
                if (nNode.getTextContent().equals(algorithmType)) {
                    Node parent = nNode.getParentNode();
                    for (int j = 0; j < parent.getChildNodes().getLength(); j++) {
                        Node child = parent.getChildNodes().item(j);
                        if (MLModelConstants.NAME.equals(child.getNodeName())) {
                            algorithms.add(child.getTextContent());
                        }
                    }
                }
            }
            return algorithms;
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException("An error occurred while retrieving" +
                                                              " algorithm names: " + e.getMessage
                    (), e);
        }
    }

    /**
     * @param algorithmType
     * @return Algorithm ratings
     * @throws MLAlgorithmConfigurationParserException
     */
    Map<String, List<Integer>> getAlgorithmRatings(String algorithmType) throws
                                                                         MLAlgorithmConfigurationParserException {
        try {
            Map<String, List<Integer>> ratings = new HashMap<String, List<Integer>>();
            NodeList nodes = mlAlgorithmsConfig.getElementsByTagName(MLModelConstants.TYPE);
            String algorithm = "";
            for (int i = 0; i < nodes.getLength(); i++) {
                Node nNode = nodes.item(i);
                if (nNode.getTextContent().equals(algorithmType)) {
                    Node parent = nNode.getParentNode();
                    List<Integer> scores = new ArrayList<Integer>();
                    for (int j = 0; j < parent.getChildNodes().getLength(); j++) {
                        Node child = parent.getChildNodes().item(j);
                        if (MLModelConstants.NAME.equals(child.getNodeName())) {
                            algorithm = child.getTextContent();
                        }
                        if (MLModelConstants.INTERPRETABILITY.equals(child.getNodeName()) ||
                            MLModelConstants.SCALABILITY.equals(child.getNodeName()) ||
                            MLModelConstants.MULTICOLLINEARITY.equals(child.getNodeName()) ||
                            MLModelConstants.DIMENSIONALITY.equals(child.getNodeName())) {
                            scores.add(Integer.parseInt(child.getTextContent()));
                        }
                    }
                    ratings.put(algorithm, scores);
                }
            }
            return ratings;
        } catch (Exception e) {
            throw new MLAlgorithmConfigurationParserException("An error occurred while retrieving" +
                                                              " algorithm ratings: " + e
                    .getMessage(), e);
        }
    }
}
