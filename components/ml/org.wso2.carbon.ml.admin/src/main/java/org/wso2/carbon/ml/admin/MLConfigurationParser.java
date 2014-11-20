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

package org.wso2.carbon.ml.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class MLConfigurationParser {

    private static final Log logger = LogFactory.getLog(MLConfigurationParser.class);

    /**
     * Parse and return default file uploading settings from ml-config.xml
     *
     * @return Data upload settings
     * @throws MLConfigurationParserException
     */
    protected DataUploadSettings getDataUploadSettings() throws MLConfigurationParserException {
        DataUploadSettings dataUploadSettings = new DataUploadSettings();
        try {
            Document doc = getXMLDocument(MLAdminConstants.ML_CONFIG_XML);
            NodeList nodes =
                    doc.getElementsByTagName(MLAdminConstants.UPLOAD_SETTINGS).item(0)
                            .getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals(MLAdminConstants.UPLOAD_LOCATION)) {
                    dataUploadSettings.setUploadLocation(nodes.item(i).getTextContent());
                }
                if (nodes.item(i).getNodeName().equals(MLAdminConstants.IN_MEMORY_THRESHOLD)) {
                    dataUploadSettings.setInMemoryThreshold(Integer.parseInt(nodes.item(i)
                                                                                     .getTextContent()));
                }
                if (nodes.item(i).getNodeName().equals(MLAdminConstants.UPLOAD_LIMIT)) {
                    dataUploadSettings.setUploadLimit(Long.parseLong(nodes.item(i).getTextContent()));
                }
            }
        } catch (Exception e) {
            String msg = "An error occurred while retrieving data upload settings\n" + e
                    .getMessage();
            logger.error(msg, e);
            throw new MLConfigurationParserException(msg);
        }
        return dataUploadSettings;
    }

    /**
     * Parse default summary satistics generation settings from ml-config.xml
     *
     * @return Summary statistics settings
     * @throws MLConfigurationParserException
     */
    protected SummaryStatisticsSettings getSummaryStatisticsSettings() throws

                                                                       MLConfigurationParserException {
        SummaryStatisticsSettings summaryStatisticsSettings = new SummaryStatisticsSettings();
        try {
            Document doc = getXMLDocument(MLAdminConstants.ML_CONFIG_XML);
            NodeList nodes =
                    doc.getElementsByTagName(MLAdminConstants.SUMMARY_STATISTICS_SETTINGS)
                            .item(0).getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals(MLAdminConstants.HISTOGRAM_BINS)) {
                    summaryStatisticsSettings.setHistogramBins(Integer.parseInt(nodes.item(i)
                                                                                        .getTextContent()));
                }
                if (nodes.item(i).getNodeName().equals(MLAdminConstants.CATEGORICAL_THRESHOLD)) {
                    summaryStatisticsSettings.setCategoricalThreshold(Integer.parseInt(nodes.item(i)
                                                                                               .getTextContent()));
                }
                if (nodes.item(i).getNodeName().equals(MLAdminConstants.SAMPLE_SIZE)) {
                    summaryStatisticsSettings.setSampleSize(Integer.parseInt(nodes.item(i)
                                                                                     .getTextContent()));
                }
            }
        } catch (Exception e) {
            String msg = "An error occurred while retrieving summary statistics settings\n" + e
                    .getMessage();
            logger.error(msg, e);
            throw new MLConfigurationParserException(msg);
        }
        return summaryStatisticsSettings;
    }

    /**
     * Reads from ml-config.xml
     *
     * @param xmlFilePath
     * @return XML document
     * @throws MLConfigurationParserException
     */
    private Document getXMLDocument(String xmlFilePath) throws MLConfigurationParserException {
        try {
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(xmlFile);
        } catch (Exception e) {
            String msg = "An error occurred while parsing XML\n" + e.getMessage();
            logger.error(msg, e);
            throw new MLConfigurationParserException(msg);
        }
    }
}
