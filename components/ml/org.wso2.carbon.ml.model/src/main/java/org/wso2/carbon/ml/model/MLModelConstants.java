/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.model;

public final class MLModelConstants {
    public static final String SMALL = "small";
    public static final String MEDIUM = "medium";
    public static final String LARGE = "large";
    public static final String HIGH = "high";
    public static final String DATASET_SIZE = "datasetSize";
    public static final String TEXTUAL = "textual";
    public static final String YES = "yes";
    public static final String ALGORITHMS = "algorithms";
    public static final String ALGORITHM = "algorithm";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String SCALABILITY = "scalability";
    public static final String INTERPRETABILITY = "interpretability";
    public static final String MULTICOLLINEARITY = "multicollinearity";
    public static final String DIMENSIONALITY = "dimensionality";
    public static final String PARAMETERS = "parameters";
    public static final String PARAMETER = "parameter";
    public static final String ML_ALGORITHMS_CONFIG_XML = "repository/conf/etc/ml-algorithms.xml";
    public static final String DECIMAL_FORMAT = "#.00";
    public static final String EMPTY = "";
    public static final String NA = "NA";

    /**
     * Private constructor to prevent any other class from instantiating.
     */
    private MLModelConstants() {
    }
}