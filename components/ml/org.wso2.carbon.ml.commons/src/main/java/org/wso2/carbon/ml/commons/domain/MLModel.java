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

package org.wso2.carbon.ml.commons.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DTO class to hold a deployable machine learning model
 */
public class MLModel implements Serializable {
    private static final long serialVersionUID = -1310680827450949233L;
    private String algorithmName;
    private String algorithmClass;
    private List<Feature> features;
    private String responseVariable;
    private int responseIndex;
    private List<Map<String, Integer>> encodings;
    private List<Integer> newToOldIndicesList;
    private Serializable model;

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }


    public String getAlgorithmClass() {
        return algorithmClass;
    }

    public void setAlgorithmClass(String algorithmClass) {
        this.algorithmClass = algorithmClass;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public String getResponseVariable() {
        return responseVariable;
    }

    public void setResponseVariable(String responseVariable) {
        this.responseVariable = responseVariable;
    }

    public Serializable getModel() {
        return model;
    }

    public void setModel(Serializable model) {
        this.model = model;
    }

    public List<Map<String, Integer>> getEncodings() {
        return encodings;
    }

    public void setEncodings(List<Map<String, Integer>> encodings) {
        this.encodings = encodings;
    }

    public int getResponseIndex() {
        return responseIndex;
    }

    public void setResponseIndex(int responseIndex) {
        this.responseIndex = responseIndex;
    }

    public List<Integer> getNewToOldIndicesList() {
        return newToOldIndicesList;
    }

    public void setNewToOldIndicesList(List<Integer> newToOldIndicesList) {
        this.newToOldIndicesList = newToOldIndicesList;
    }
}
