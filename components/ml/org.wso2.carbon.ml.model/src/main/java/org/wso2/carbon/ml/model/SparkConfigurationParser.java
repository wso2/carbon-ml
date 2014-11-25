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
import org.apache.spark.SparkConf;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class SparkConfigurationParser {
    private static final Log logger = LogFactory.getLog(SparkConfigurationParser.class);

    /**
     * This method generates a spark configuration according to configuration settings in
     * spark-config.xml
     *
     * @return Spark configuration
     * @throws SparkConfigurationParserException
     */
    SparkConf getSparkConfiguration() throws SparkConfigurationParserException {
        try {
            SparkConf sparkConf = new SparkConf();
            XMLParser xmlParser = new XMLParser();
            Document doc = xmlParser.getXMLDocument(MLModelConstants.SPARK_CONFIG_XML);
            NodeList nodes = doc.getElementsByTagName(MLModelConstants.PROPERTY);
            for (int i = 0; i < nodes.getLength(); i++) {
                String key = nodes.item(i).getAttributes().getNamedItem(MLModelConstants.NAME)
                        .getTextContent();
                String value = nodes.item(i).getTextContent();
                sparkConf.set(key, value);
            }

            return sparkConf;
        } catch (Exception e) {
            logger.error("An error occurred while generating spark configuration: " + e
                    .getMessage(), e);
            throw new SparkConfigurationParserException(e.getMessage(), e);
        }
    }
}
