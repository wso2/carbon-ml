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

package org.wso2.carbon.ml.model.internal.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * DTO class for JAXB binding of SparkConfigurationParser
 */
public class SparkProperty {
    private String property;
    private String name;

    /**
     * @return Retuns spark property name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Sets spark property name
     */
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns spark property value
     */
    public String getProperty() {
        return property;
    }

    /**
     * @param property Sets spark property value
     */
    @XmlValue
    public void setProperty(String property) {
        this.property = property;
    }
}
