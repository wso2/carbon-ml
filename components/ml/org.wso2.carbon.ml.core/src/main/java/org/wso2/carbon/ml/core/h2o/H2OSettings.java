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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.core.h2o;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO class for JAXB binding of H2OConfigurationParser
 */
@XmlRootElement(name = "h2oSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class H2OSettings {
    @XmlElement(name = "property")
    private List<H2OProperty> properties;

    /**
     * @return Returns h2o properties
     */
    public List<H2OProperty> getProperties() {
        return properties;
    }

    /**
     * @param properties Sets h2o properties
     */
    public void setProperties(List<H2OProperty> properties) {
        this.properties = properties;
    }
}
