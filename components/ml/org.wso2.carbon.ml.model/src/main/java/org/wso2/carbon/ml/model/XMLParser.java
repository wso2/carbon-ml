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

import org.w3c.dom.Document;
import org.wso2.carbon.ml.model.exceptions.XMLParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLParser {

    /**
     * This method parse an XML document
     *
     * @param xmlFilePath XML file path
     * @return XML document
     * @throws org.wso2.carbon.ml.model.exceptions.MLAlgorithmConfigurationParserException
     */
    Document getXMLDocument(String xmlFilePath) throws XMLParserException {
        try {
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(xmlFile);
        } catch (Exception e) {
            throw new XMLParserException("An error occurred while parsing " +
                                         "XML: " + e.getMessage(), e);
        }
    }
}
