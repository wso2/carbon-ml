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

package org.wso2.carbon.ml.core.spark.summary;

import java.io.Serializable;

/**
 * DTO class to store feature vs. value value
 */
public class FeatureImportance implements Serializable {

    private static final long serialVersionUID = 4836787591464024343L;
    private String label; //feature name. This is refer to as "label" according to the requirement of the graph.
    private double value; //weight. This is refer to as "value" according to the requirement of the graph.

    /**
     * @return Returns feature name
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param feature Sets feature name
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value Sets value
     */
    public void setValue(double value) {
        this.value = value;
    }
}
