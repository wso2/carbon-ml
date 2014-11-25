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
    static final String SMALL = "small";
    static final String MEDIUM = "medium";
    static final String LARGE = "large";
    static final String HIGH = "high";
    static final String DATASET_SIZE = "datasetSize";
    static final String TEXTUAL = "textual";
    static final String YES = "yes";
    static final String ALGORITHMS = "algorithms";
    static final String ALGORITHM = "algorithm";
    static final String NAME = "name";
    static final String TYPE = "type";
    static final String SCALABILITY = "scalability";
    static final String INTERPRETABILITY = "interpretability";
    static final String MULTICOLLINEARITY = "multicollinearity";
    static final String DIMENSIONALITY = "dimensionality";
    static final String PARAMETERS = "parameters";
    static final String PARAMETER = "parameter";
    static final String ML_ALGORITHMS_CONFIG_XML = "repository/conf/etc/ml-algorithms.xml";
    static final String SPARK_CONFIG_XML = "repository/conf/etc/spark-config.xml";
    static final String DECIMAL_FORMAT = "#.00";
    static final String EMPTY = "";
    static final String NA = "NA";
    static final String PROPERTY = "property";
    static final String MODEL_ID = "modelID";
    static final String DATASET_URL = "datasetURL";
    static final String TRAIN_DATA_FRACTION = "trainDataFraction";
    static final String CSV = ".csv";
    static final String TSV = ".tsv";
    static final String RESPONSE = "responseVariable";
    static final Long RANDOM_SEED = 11L;
    static final String LEARNING_RATE = "Learning_Rate";
    static final String ITERATIONS = "Iterations";
    static final String REGULARIZATION_TYPE = "Reg_Type";
    static final String REGULARIZATION_PARAMETER = "Reg_Parameter";
    static final String SGD_DATA_FRACTION = "SGD_Data_Fraction";
    static final String NUM_CLASSES = "Num_Classes";
    static final String MAX_DEPTH = "Max_Depth";
    static final String MAX_BINS = "Max_Bins";
    static final String IMPURITY = "Impurity";
    static final String L1 = "L1";
    static final String L2 = "L2";

    enum SUPERVISED_ALGORITHM {
        LOGISTIC_REGRESSION, SVM, DECISION_TREE, NAIVE_BAYES
    }

    enum UNSUPERVISED_ALGORITHM {
        K_MEANS
    }

    /**
     * Private constructor to prevent any other class from instantiating.
     */
    private MLModelConstants() {
        //
    }
}