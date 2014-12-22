/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.model.internal;

import org.apache.spark.SparkConf;
import org.wso2.carbon.ml.model.exceptions.MLAlgorithmParserException;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.exceptions.SparkConfigurationParserException;
import org.wso2.carbon.ml.model.exceptions.XMLParserException;
import org.wso2.carbon.ml.model.internal.constants.MLModelConstants;
import org.wso2.carbon.ml.model.internal.dto.MLAlgorithms;
import org.wso2.carbon.ml.model.internal.dto.MLFeature;
import org.wso2.carbon.ml.model.internal.dto.MLWorkflow;
import org.wso2.carbon.ml.model.internal.dto.SparkProperty;
import org.wso2.carbon.ml.model.internal.dto.SparkSettings;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for various model related tasks
 */
public class MLModelUtils {
    /**
     * Private constructor to prevent any other class from instantiating.
     */
    private MLModelUtils() {
        //
    }

    /**
     * @param feature         Feature name
     * @param headerRow       HeaderFilter row
     * @param columnSeparator Column separator character
     * @return Index of the response variable
     */
    public static int getFeatureIndex(String feature, String headerRow,
            String columnSeparator) throws
            ModelServiceException {
        int featureIndex = 0;
        String[] headerItems = headerRow.split(columnSeparator);
        for (int i = 0; i < headerItems.length; i++) {
            if (feature.equals(headerItems[i])) {
                featureIndex = i;
                break;
            }
        }
        return featureIndex;
    }

    /**
     * @param datasetURL Dataset URL
     * @return Column separator character
     */
    public static String getColumnSeparator(String datasetURL) throws
            ModelServiceException {
        if (datasetURL.endsWith(MLModelConstants.CSV)) {
            return ",";
        } else if (datasetURL.endsWith(MLModelConstants.TSV)) {
            return "\t";
        } else {
            return "";
        }
    }

    /**
     * @param workflow     Machine learning workflow
     * @param imputeOption Impute option
     * @return Returns indices of features where discard row imputaion is applied
     */
    public static List<Integer> getImputeFeatureIndices(MLWorkflow workflow, String imputeOption)
            throws ModelServiceException {
        List<Integer> imputeFeatureIndices = new ArrayList();
        for (MLFeature feature : workflow.getFeatures()) {
            if (feature.getImputeOption().equals(imputeOption)) {
                imputeFeatureIndices.add(feature.getIndex());
            }
        }
        return imputeFeatureIndices;
    }

    /**
     * @param mlAlgorithmConfigXML Default configurations for machine learning algorithms
     * @return Returns MLAlgorithms object
     * @throws MLAlgorithmParserException
     */
    public static MLAlgorithms getMLAlgorithms(String mlAlgorithmConfigXML)
            throws MLAlgorithmParserException {
        try {
            return (MLAlgorithms) parseXML(mlAlgorithmConfigXML);
        } catch (XMLParserException e) {
            throw new MLAlgorithmParserException(
                    "An error occured while parsing ml algorithm configuration: " + e.getMessage(),
                    e);
        }

    }

    /**
     * @param sparkConfigXML Spark configuration parameters
     * @return Return SparkConf object
     * @throws SparkConfigurationParserException
     */
    public static SparkConf getSparkConf(String sparkConfigXML) throws
            SparkConfigurationParserException {
        try {
            SparkSettings sparkSettings = (SparkSettings) parseXML(sparkConfigXML);
            SparkConf sparkConf = new SparkConf();
            for (SparkProperty sparkProperty : sparkSettings.getProperties()) {
                sparkConf.set(sparkProperty.getName(), sparkProperty.getProperty());
            }
            return sparkConf;
        } catch (XMLParserException e) {
            throw new SparkConfigurationParserException(
                    "An error occured while parsing spark configuration: " + e.getMessage(), e);
        }

    }

    /**
     * @param xmlFilePath Absolute path to an xml file
     * @return Returns unmarshalled xml
     * @throws XMLParserException
     */
    public static Object parseXML(String xmlFilePath) throws XMLParserException {
        try {
            File file = new File(xmlFilePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(MLAlgorithms.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new XMLParserException("An error occured while parsing: " + xmlFilePath + ": " +
                                         e.getMessage(), e);
        }
    }


}
