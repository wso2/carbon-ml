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
 * DTO class to store feature vs. weight value
 */
public class FeatureWeight implements Serializable {

    private static final long serialVersionUID = 4836787591464024343L;
    private String featureName;
    private double weight;

    /**
     * @return Returns feature name
     */
    public String getFeatureName() {
        return featureName;
    }

    /**
     * @param feature Sets feature name
     */
    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    /**
     * @return Returns weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight Sets weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
}
