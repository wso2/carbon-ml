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

package org.wso2.carbon.ml.model.internal.constants;

/**
 * A utility class to store ML model service constants
 */
public final class MLModelConstants {

    // user response
    public static final String SMALL = "small";
    public static final String MEDIUM = "medium";
    public static final String LARGE = "large";
    public static final String HIGH = "high";
    public static final String DATASET_SIZE = "datasetSize";
    public static final String TEXTUAL = "textual";
    public static final String BINARY = "binary";
    public static final String YES = "yes";
    public static final String NO = "no";

    // model settings json
    public static final String MODEL_ID = "modelID";
    public static final String MODEL_SETTINGS_ID = "modelSettingsID";
    public static final String WORKFLOW_ID = "workflowID";
    public static final String DATASET_URL = "datasetURL";
    public static final String TRAIN_DATA_FRACTION = "trainDataFraction";
    public static final String RESPONSE = "responseVariable";
    public static final String ALGORITHM_NAME = "algorithmName";
    public static final String ALGORITHM_TYPE = "algorithmType";
    public static final String HYPER_PARAMETERS = "hyperParameters";

    // feature settings
    public static final String DISCARD = "DISCARD";
    public static final String MEAN_IMPUTATION = "REPLACE_WTH_MEAN";

    // xml tags
    public static final String ALGORITHMS = "algorithms";
    public static final String ALGORITHM = "algorithm";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String VALUE = "value";
    public static final String SCALABILITY = "scalability";
    public static final String INTERPRETABILITY = "interpretability";
    public static final String MULTICOLLINEARITY = "multicollinearity";
    public static final String DIMENSIONALITY = "dimensionality";
    public static final String PARAMETERS = "parameters";
    public static final String PARAMETER = "parameter";
    public static final String PROPERTY = "property";
    public static final String CLASSIFICATION = "Classification";
    public static final String NUMERICAL_PREDICTION = "Numerical_Prediction";
    public static final String CLUSTERING = "Clustering";

    // file formats
    public static final String CSV = ".csv";
    public static final String TSV = ".tsv";

    // hyper-parameter names
    public static final String LEARNING_RATE = "Learning_Rate";
    public static final String ITERATIONS = "Iterations";
    public static final String REGULARIZATION_TYPE = "Reg_Type";
    public static final String REGULARIZATION_PARAMETER = "Reg_Parameter";
    public static final String SGD_DATA_FRACTION = "SGD_Data_Fraction";
    public static final String NUM_CLASSES = "Num_Classes";
    public static final String MAX_DEPTH = "Max_Depth";
    public static final String MAX_BINS = "Max_Bins";
    public static final String IMPURITY = "Impurity";
    public static final String L1 = "L1";
    public static final String L2 = "L2";

    // configuration file names
    public static final String ML_ALGORITHMS_CONFIG_XML = "repository/conf/etc/ml-algorithms.xml";
    public static final String SPARK_CONFIG_XML = "repository/conf/etc/spark-config.xml";
    public static final String ML_DB = "jdbc/WSO2ML_DB";

    // other
    public static final Long RANDOM_SEED = 11L;
    public static final String EMPTY = "";
    public static final String NA = "NA";
    public static final String DECIMAL_FORMAT = "#.00";
    public static final String CLASS_CLASSIFICATION_MODEL_SUMMARY = "ClassClassificationModel" +
                                                                    "Summary";
    public static final String PROBABILISTIC_CLASSIFICATION_MODEL_SUMMARY =
            "ProbabilisticClassificationModelSummary";

    // enums
    public enum SUPERVISED_ALGORITHM {
        LOGISTIC_REGRESSION, SVM, DECISION_TREE, NAIVE_BAYES
    }

    public enum UNSUPERVISED_ALGORITHM {
        K_MEANS
    }

    /**
     * Private constructor to prevent any other class from instantiating.
     */
    private MLModelConstants() {
        //
    }
}