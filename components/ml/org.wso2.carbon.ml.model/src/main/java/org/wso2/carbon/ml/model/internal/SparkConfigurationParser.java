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

package org.wso2.carbon.ml.model.internal;

import org.apache.spark.SparkConf;
import org.wso2.carbon.ml.model.internal.dto.SparkProperty;
import org.wso2.carbon.ml.model.internal.dto.SparkSettings;
import org.wso2.carbon.ml.model.exceptions.SparkConfigurationParserException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class SparkConfigurationParser {
    private String sparkConfigurationFilePath;

    public SparkConfigurationParser(String sparkConfigurationFilePath) {
        this.sparkConfigurationFilePath = sparkConfigurationFilePath;
    }

    /**
     * This method generates a spark configuration according to configuration settings in
     * spark-config.xml
     *
     * @return Spark configuration
     * @throws org.wso2.carbon.ml.model.exceptions.SparkConfigurationParserException
     */
    public SparkConf getSparkConf()
            throws SparkConfigurationParserException {
        try {
            File file = new File(this.sparkConfigurationFilePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(SparkSettings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SparkSettings sparkSettings = (SparkSettings) jaxbUnmarshaller.unmarshal(file);
            SparkConf sparkConf = new SparkConf();
            for (SparkProperty sparkProperty : sparkSettings.getProperties()) {
                sparkConf.set(sparkProperty.getName(), sparkProperty.getProperty());
            }
            return sparkConf;
        } catch (Exception e) {
            throw new SparkConfigurationParserException(
                    "An error occurred while generating spark configuration: " + e.getMessage(), e);
        }
    }
}
