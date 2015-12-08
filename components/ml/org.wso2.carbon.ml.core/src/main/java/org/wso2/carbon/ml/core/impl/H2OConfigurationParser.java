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
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.wso2.carbon.ml.core.exceptions.H2OConfigurationParserException;
import org.wso2.carbon.ml.core.h2o.H2OProperty;
import org.wso2.carbon.ml.core.h2o.H2OSettings;

/**
 * Class contains methods for parsing configurations for H2O Server.
 */
public class H2OConfigurationParser {

    public H2OConfigurationParser() {
    }

    /**
     * Retrieve the H2O configurations from the repository/conf/etc/h2o-config.xml
     *
     * @param h2oConfigXML H2O configuration parameters
     * @return Returns H2OSettings object
     * @throws H2OConfigurationParserException
     */
    public HashMap<String, String> getH2OConf(String h2oConfigXML) throws H2OConfigurationParserException {
        try {
            File file = new File(h2oConfigXML);
            JAXBContext jaxbContext = JAXBContext.newInstance(H2OSettings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            H2OSettings h2oSettings = (H2OSettings) jaxbUnmarshaller.unmarshal(file);
            HashMap<String, String> h2oConfigarations = new HashMap<String, String>();
            for (H2OProperty h2oProperty : h2oSettings.getProperties()) {
                h2oConfigarations.put(h2oProperty.getName(), h2oProperty.getProperty());
            }
            return h2oConfigarations;
        } catch (JAXBException e) {
            throw new H2OConfigurationParserException(
                    "An error occurred while parsing: " + h2oConfigXML + ": " + e.getMessage(), e);
        }

    }

}
