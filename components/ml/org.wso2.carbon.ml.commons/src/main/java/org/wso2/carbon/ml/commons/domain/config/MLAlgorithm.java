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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.wso2.carbon.ml.commons.domain.MLHyperParameter;

/**
 * DTO class for JAXB binding of MLAlgorithmConfigurationParser
 */
@XmlRootElement(name = "Algorithm")
public class MLAlgorithm {
    private String name;
    private String type;
    private int interpretability;
    private int scalability;
    private int multicollinearity;
    private int dimensionality;
    private boolean isPMMLExportable;
    private List<MLHyperParameter> parameters;

    /**
     * @return Returns machine learning algorithm name
     */
    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    /**
     * @param name Sets machine learning algorithm name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns machine learning algorithm type e.g. Classification
     */
    @XmlElement(name = "Type")
    public String getType() {
        return type;
    }

    /**
     * @param type Sets machine learning algorithm type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns interpretability score (out of 5)
     */
    @XmlElement(name = "Interpretability")
    public int getInterpretability() {
        return interpretability;
    }

    /**
     * @param interpretability Sets interpretability score
     */
    public void setInterpretability(int interpretability) {
        this.interpretability = interpretability;
    }

    /**
     * @return Returns scalability score (out of 5)
     */
    @XmlElement(name = "Scalability")
    public int getScalability() {
        return scalability;
    }

    /**
     * @param scalability Sets scalability score
     */
    public void setScalability(int scalability) {
        this.scalability = scalability;
    }

    /**
     * @return Returns multicollinearity score (out of 5)
     */
    @XmlElement(name = "Multicollinearity")
    public int getMulticollinearity() {
        return multicollinearity;
    }

    /**
     * @param multicollinearity Sets multicollinearity score
     */
    public void setMulticollinearity(int multicollinearity) {
        this.multicollinearity = multicollinearity;
    }

    /**
     * @return Returns dimensionality score (out of 5)
     */
    @XmlElement(name = "Dimensionality")
    public int getDimensionality() {
        return dimensionality;
    }

    /**
     * @param dimensionality Sets dimensionality score
     */
    public void setDimensionality(int dimensionality) {
        this.dimensionality = dimensionality;
    }

    @XmlElement(name= "isPMMLExportable")
    public boolean isPMMLExportable(){ return isPMMLExportable;}

    public void setMMLExportable(boolean status){this.isPMMLExportable = status;}

    /**
     * @return Returns a list of hyper-parameters
     */
    @XmlElement(name = "Parameters")
    public List<MLHyperParameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters Sets hyper-parameters
     */
    public void setParameters(List<MLHyperParameter> parameters) {
        this.parameters = parameters;
    }
}
