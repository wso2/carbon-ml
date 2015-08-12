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
package org.wso2.carbon.ml.commons.constants;

/**
 * A class to keep ML related shared constants.
 */
public class MLConstants {

    private MLConstants() {
        
    }
    
    public static final String ML_MODEL_TABLE_NAME = "ml_model_store";

    public static final String MACHINE_LEARNER_XML = "repository/conf/machine-learner.xml";

    // Data-set upload configurations    
    public static final String UPLOAD_SETTINGS = "dataUploadSettings";
    public static final String UPLOAD_LOCATION = "uploadLocation";
    public static final String IN_MEMORY_THRESHOLD = "inMemoryThreshold";
    public static final String UPLOAD_LIMIT = "uploadLimit";
    
    // Summary statistic calculation configurations  
    public static final String SUMMARY_STATISTICS_SETTINGS = "summaryStatisticsSettings";
    public static final String HISTOGRAM_BINS = "histogramBins";
    public static final String CATEGORICAL_THRESHOLD = "categoricalThreshold";
    public static final String SAMPLE_SIZE = "sampleSize";
    
    public static final String PROPERTIES = "properties";
    
    // System property names
    public static final String HOME = "user.home";
    public static final String FILE_SEPARATOR = "file.separator";
    
    public static final String USER_HOME= "USER_HOME";
    public static final String DATABASE= "database";
    public static final String ML_PROJECTS = "MLProjects";
    
    // Character Encodings
    public static final String UTF_8= "UTF-8";
    public static final String ISO_8859_1= "ISO-8859-1";
    
    public static final String TARGET_HOME_PROP = "target.home";
    public static final String ML_THREAD_POOL_SIZE = "ml.thread.pool.size";

    // I/O Adapter configs
    public static final String BAM_SERVER_URL= "BAMServerURL";
    public static final String BAM_DATA_VALUES = "values";
    
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
    
    public static final String MODEL_NAME = "Model";
    
    // model statuses
    public static final String MODEL_STATUS_NOT_STARTED = "Not Started";
    public static final String MODEL_STATUS_IN_PROGRESS = "In Progress";
    public static final String MODEL_STATUS_COMPLETE = "Complete";
    public static final String MODEL_STATUS_FAILED = "Failed";

    // dataset version statuses
    public static final String DATASET_VERSION_STATUS_IN_PROGRESS = "Processing";
    public static final String DATASET_VERSION_STATUS_COMPLETE = "Processed";
    public static final String DATASET_VERSION_STATUS_FAILED = "Failed";

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

    // file formats
    public static final String CSV = ".csv";
    public static final String TSV = ".tsv";
    public static final String IN_SUFFIX = ".in";
    public static final String OUT_SUFFIX = ".out";

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
    public static final String SEED = "Seed";
    public static final String FEATURE_SUBSET_STRATEGY = "Feature_Subset_Strategy";
    public static final String NUM_TREES = "Num_Trees";
    public static final String L1 = "L1";
    public static final String L2 = "L2";
    public static final String NUM_CLUSTERS = "Num_Clusters";
    public static final String LAMBDA = "Lambda";

    // configuration file names
    public static final String ML_ALGORITHMS_CONFIG_XML = "repository/conf/etc/ml-algorithms.xml";
    public static final String SPARK_CONFIG_XML = "repository/conf/etc/spark-config.xml";
    public static final String ML_DB = "jdbc/WSO2ML_DB";

    // other
    public static final Long RANDOM_SEED = 11L;
    public static final String DECIMAL_FORMAT = "#.00";
    public static final String CLASS_CLASSIFICATION_AND_REGRESSION_MODEL_SUMMARY = "ClassClassificationAndRegressionModelSummary";
    public static final String PROBABILISTIC_CLASSIFICATION_MODEL_SUMMARY = "ProbabilisticClassificationModelSummary";
    public static final String CLUSTER_MODEL_SUMMARY = "ClusterModelSummary";
    
    public static final int K_MEANS_SAMPLE_SIZE = 10000;
    
    public static final int ML_ALGORITHM_WEIGHT_LEVEL_1 = 5;
    public static final int ML_ALGORITHM_WEIGHT_LEVEL_2 = 3;
    public static final int ML_ALGORITHM_WEIGHT_LEVEL_3 = 1;

    public static final String STORAGE_TYPE = "storageType";
    public static final String STORAGE_LOCATION = "storageLocation";
    
    //Email Sender constants
    public static final String ML_EMAIL_ADAPTER = "MLEmailAdapter";
    public static final String EMAIL_CONF_DIRECTORY = "email";
    public static final String ML_EMAIL_TEMPLATES_FILE = "ml-email-templates.xml";
    public static final String EMAIL_TEMPLATES = "emailTemplates";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEMPLATE = "template";
    public static final String MODEL_BUILDING_COMPLETE_NOTIFICATION = "modelBuildingCompleteNotification";
    public static final String MODEL_BUILDING_FAILED_NOTIFICATION = "modelBuildingFailedNotification";

    // to build predictions CSV
    public static final String NEW_LINE = "\n";

    // enums
    public enum SUPERVISED_ALGORITHM {
        LINEAR_REGRESSION, RIDGE_REGRESSION, LASSO_REGRESSION, LOGISTIC_REGRESSION, LOGISTIC_REGRESSION_LBFGS, 
        SVM, DECISION_TREE, RANDOM_FOREST, NAIVE_BAYES
    }

    public enum UNSUPERVISED_ALGORITHM {
        K_MEANS
    }

    public enum MISSING_VALUES {
        EMPTY(""), NA("NA"), QUESTION("?");

        private final String value;
        private MISSING_VALUES(final String str) {
            this.value = str;
        }

        @Override
        public String toString() {
            return value;
        }

        public static boolean contains(String s) {
            for (MISSING_VALUES val : values())
                if (val.toString().equals(s)) {
                    return true;
                }
            return false;
        }
    }
}
