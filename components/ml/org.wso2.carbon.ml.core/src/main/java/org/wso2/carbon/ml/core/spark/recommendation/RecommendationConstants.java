/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.ml.core.spark.recommendation;

public class RecommendationConstants {
    //Collaborative filtering parameters
    /**
     * Default regularization parameter
     */
    public static final Double DEFAULT_LAMBDA = 0.01;

    /**
     * Default confidence level parameter
     */
    public static final Double DEFAULT_ALPHA = 40.0;

    /**
     * Default number of recommended items
     */
    public static final Integer DEFAULT_NUMBER_OF_ITEMS = 50;

    /**
     * Default number of recommended users
     */
    public static final Integer DEFAULT_NUMBER_OF_USERS = 50;

    //Implicit inference constants
    /**
     * Default weight
     */
    public static final Double DEFAULT_WEIGHT = 0.7;
}
