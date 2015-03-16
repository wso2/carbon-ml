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
package org.wso2.carbon.ml.core.utils;

public class MLConstants {

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
    public static final String PROPERTY = "property";
    
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
    
    //Email Sender constants
    public static final String EMAIL_CONF_DIRECTORY = "email";
    public static final String ML_EMAIL_TEMPLATES_FILE = "ml-email-templates.xml";
    public static final String EMAIL_TEMPLATES = "emailTemplates";
    public static final String TEMPLATE = "template";
    public static final String MODEL_BUILDING_COMPLETE_NOTIFICATION = "modelBuildingCompleteNotification";
    public static final String MODEL_BUILDING_FAILED_NOTIFICATION = "modelBuildingFailedNotification";
}
