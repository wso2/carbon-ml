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

package org.wso2.carbon.ml.model.internal.dto;

/**
 * DTO class to store a machine learning feature
 */
public class MLFeature {
    private String name;
    private int index;
    private String type;
    private String imputeOption;
    private boolean include;

    /**
     * @return Returns feature name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Sets feature name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Whether to include the feature in the machine learning model building or not
     */
    public boolean isInclude() {
        return include;
    }

    /**
     * @param include Sets whether to include the feature or not
     */
    public void setInclude(boolean include) {
        this.include = include;
    }

    /**
     * @return Returns feature type - CATEGORICAL or NUMERICAL
     */
    public String getType() {
        return type;
    }

    /**
     * @param type Sets feature type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns impute option
     */
    public String getImputeOption() {
        return imputeOption;
    }

    /**
     * @param imputeOption Sets impute option - DISCARD or REPLACE_WITH_MEAN
     */
    public void setImputeOption(String imputeOption) {
        this.imputeOption = imputeOption;
    }

    /**
     * @return Returns feature index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index Sets feature index
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
