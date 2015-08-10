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

package org.wso2.carbon.ml.core.impl;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.spark.SparkConf;
import org.wso2.carbon.ml.core.exceptions.SparkConfigurationParserException;
import org.wso2.carbon.ml.core.spark.SparkProperty;
import org.wso2.carbon.ml.core.spark.SparkSettings;

/**
 * Class contains methods for parsing configurations from machine-learner.xml XML file.
 */
public class SparkConfigurationParser {

    public SparkConfigurationParser() {
    }
    
    /**
     * Retrieve the Spark configurations from the repository/conf/etc/spark-config.xml
     * 
     * @param sparkConfigXML    Spark configuration parameters
     * @return                  Returns SparkConf object
     * @throws                  SparkConfigurationParserException
     */
    public SparkConf getSparkConf(String sparkConfigXML) throws SparkConfigurationParserException {
        try {
            File file = new File(sparkConfigXML);
            JAXBContext jaxbContext = JAXBContext.newInstance(SparkSettings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SparkSettings sparkSettings = (SparkSettings) jaxbUnmarshaller.unmarshal(file);
            SparkConf sparkConf = new SparkConf();
            for (SparkProperty sparkProperty : sparkSettings.getProperties()) {
                sparkConf.set(sparkProperty.getName(), sparkProperty.getProperty());
            }
            return sparkConf;
        } catch (JAXBException e) {
            throw new SparkConfigurationParserException("An error occurred while parsing: " + sparkConfigXML + ": " +
                    e.getMessage(), e);
        }

    }
    
}
