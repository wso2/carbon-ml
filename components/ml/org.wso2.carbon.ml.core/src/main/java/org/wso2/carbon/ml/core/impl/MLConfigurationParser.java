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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.core.impl;

import java.io.File;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.ml.commons.domain.DataUploadSettings;
import org.wso2.carbon.ml.commons.domain.SummaryStatisticsSettings;
import org.wso2.carbon.ml.core.utils.MLConstants;
import org.wso2.carbon.ml.dataset.exceptions.MLConfigurationParserException;

/**
 * Class contains methods for parsing configurations from ml-config XML file.
 */
public class MLConfigurationParser {

    private static final Log logger = LogFactory.getLog(MLConfigurationParser.class);
    private Document document;

    public MLConfigurationParser(String path) throws MLConfigurationParserException {
        try {
            if (path == null || StringUtils.isEmpty(path)) {
                path = MLConstants.ML_CONFIG_XML;
            }
            File xmlFile = new File(path);
            if (xmlFile.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder;
                dBuilder = dbFactory.newDocumentBuilder();
                this.document = dBuilder.parse(xmlFile);
            } else {
                throw new MLConfigurationParserException("File not found : "+path);
            }
            
        } catch (Exception e) {
            throw new MLConfigurationParserException("An error occurred while parsing " +
                MLConstants.ML_CONFIG_XML + " : " + e.getMessage(), e);
        }
    }

    /**
     * Parse and return default file uploading settings from ml-config.xml.
     *
     * @return      Data upload settings
     * @throws      MLConfigurationParserException
     */
    protected DataUploadSettings getDataUploadSettings() throws MLConfigurationParserException {
        DataUploadSettings dataUploadSettings = new DataUploadSettings();
        try {
            NodeList nodes = this.document.getElementsByTagName(MLConstants
                .UPLOAD_SETTINGS).item(0) .getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals(MLConstants.UPLOAD_LOCATION)) {
                    dataUploadSettings.setUploadLocation(nodes.item(i).getTextContent());
                } else if (nodes.item(i).getNodeName().equals(MLConstants
                    .IN_MEMORY_THRESHOLD)) {
                        dataUploadSettings.setInMemoryThreshold(Integer.parseInt(nodes.item(i)
                            .getTextContent()));
                } else if (nodes.item(i).getNodeName().equals(MLConstants.UPLOAD_LIMIT)) {
                    dataUploadSettings.setUploadLimit(Long .parseLong(nodes.item(i)
                        .getTextContent()));
                }
            }
            if(logger.isDebugEnabled()){
                logger.info("Successfully parsed data uploading settings.");
            }
        } catch (Exception e) {
            throw new MLConfigurationParserException( "An error occurred while retrieving data " +
                "upload settings: " + e.getMessage(), e);
        }
        return dataUploadSettings;
    }

    /**
     * Parse default summary statistics generation settings from ml-config.xml.
     *
     * @return      Summary statistics settings
     * @throws      MLConfigurationParserException
     */
    protected SummaryStatisticsSettings getSummaryStatisticsSettings() 
            throws MLConfigurationParserException {
        SummaryStatisticsSettings summaryStatisticsSettings = new SummaryStatisticsSettings();
        try {
            NodeList nodes = this.document.getElementsByTagName(MLConstants
                .SUMMARY_STATISTICS_SETTINGS).item(0).getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals(MLConstants.HISTOGRAM_BINS)) {
                    summaryStatisticsSettings.setHistogramBins(Integer.parseInt(nodes.item(i)
                        .getTextContent()));
                } else if (nodes.item(i).getNodeName()
                        .equals(MLConstants.CATEGORICAL_THRESHOLD)) {
                    summaryStatisticsSettings.setCategoricalThreshold(Integer.parseInt(nodes
                        .item(i).getTextContent()));
                } else if (nodes.item(i).getNodeName().equals(MLConstants.SAMPLE_SIZE)) {
                    summaryStatisticsSettings.setSampleSize(Integer.parseInt(nodes.item(i)
                        .getTextContent()));
                }
            }
            if(logger.isDebugEnabled()){
                logger.info("Successfully parsed summary statistics settings.");
            }
        } catch (Exception e) {
            throw new MLConfigurationParserException( "An error occurred while retrieving " +
                "summary statistics settings: " + e.getMessage(), e);
        }
        return summaryStatisticsSettings;
    }
    
    /**
     * Parse properties from ml-config.xml.
     *
     * @return      Properties defined.
     * @throws      MLConfigurationParserException
     */
    protected Properties getProperties() 
            throws MLConfigurationParserException {
        Properties properties = new Properties();
        try {
            NodeList nodes = this.document .getElementsByTagName(MLConstants.PROPERTIES).item(0).getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeName().equals(MLConstants.PROPERTY)) {
                    NamedNodeMap attributes = node.getAttributes();
                    String name = attributes.getNamedItem("name").getNodeValue();
                    String value = attributes.getNamedItem("value").getNodeValue();
                    properties.put(name, value);
                } 
            }
            if(logger.isDebugEnabled()){
                logger.info("Successfully parsed properties.");
            }
        } catch (Exception e) {
            throw new MLConfigurationParserException( "An error occurred while retrieving " +
                "properties from ml config: " + e.getMessage(), e);
        }
        return properties;
    }
    
    /**
     * Parse the JNDI lookup name of the ML database from the ml-config.xml file
     * 
     * @return      JNDI lookup name of the ML database
     * @throws      MLConfigurationParserException 
     */
    protected String getDatabaseName() throws MLConfigurationParserException{
        try{
            return this.document.getElementsByTagName(MLConstants.DATABASE).item(0)
                    .getTextContent();
        } catch(Exception e){
            throw new MLConfigurationParserException( "An error occurred while retrieving ML " +
                "database name: " + e.getMessage(), e);
        }
    }
}
