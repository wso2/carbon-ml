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

package org.wso2.carbon.ml.model.dto;

import java.util.List;

/**
 * DTO class for JAXB binding of MLAlgorithmConfigurationParser
 */
public class MLAlgorithm {
    private String name;
    private String type;
    private int interpretability;
    private int scalability;
    private int multicollinearity;
    private int dimensionality;
    private List<HyperParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getInterpretability() {
        return interpretability;
    }

    public void setInterpretability(int interpretability) {
        this.interpretability = interpretability;
    }

    public int getScalability() {
        return scalability;
    }

    public void setScalability(int scalability) {
        this.scalability = scalability;
    }

    public int getMulticollinearity() {
        return multicollinearity;
    }

    public void setMulticollinearity(int multicollinearity) {
        this.multicollinearity = multicollinearity;
    }

    public int getDimensionality() {
        return dimensionality;
    }

    public void setDimensionality(int dimensionality) {
        this.dimensionality = dimensionality;
    }

    public List<HyperParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<HyperParameter> parameters) {
        this.parameters = parameters;
    }
}
