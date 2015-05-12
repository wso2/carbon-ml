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

package org.wso2.carbon.ml.commons.domain.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

/**
 * DTO class for JAXB binding of MLAlgorithmConfigurationParser
 */
public class HyperParameter implements Serializable {
    private static final long serialVersionUID = -2314030361825726083L;
    private String parameter;
    private String value;

    /**
     * @return Returns hyper-parameter name
     */
    @XmlElement(name = "Name")
    public String getParameter() {
        return parameter;
    }

    /**
     * @param parameter Sets hyper-parameter name
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * @return Returns hyper-parameter value
     */
    @XmlElement(name = "Value")
    public String getValue() {
        return value;
    }

    /**
     * @param value Sets hyper-parameter value
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "HyperParameter [parameter=" + parameter + ", value=" + value + "]";
    }
}
