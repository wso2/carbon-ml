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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.project.mgt.constant;

public class ProjectMgtConstants {

    private ProjectMgtConstants() {
    }

    public static final String ADAPTOR_TYPE_EMAIL = "email";

    public static final String ADAPTOR_MESSAGE_EMAIL_ADDRESS = "email.address";
    public static final String ADAPTOR_MESSAGE_EMAIL_SUBJECT = "email.subject";

    public static final String ADAPTOR_CONF_EMAIL_DEFAULT_SUBJECT = "email.subject.default";
    public static final String ADAPTOR_CONF_EMAIL_HINT_DEFAULT_SUBJECT = "email.subject.default.hint";

    public static final String EMAIL_CONF_DIRECTORY = "email";
    public static final String ML_EMAIL_CONF_FILE = "ml-email-config.xml";
    
    public static final String CONFIGURATIONS = "configurations";
    public static final String CONFIGURATION = "configuration";
    public static final String MODEL_BUILDING_COMPLETE_NOTIFICATION = "modelBuildingCompleteNotification";
    public static final String MODEL_BUILDING_FAILED_NOTIFICATION = "modelBuildingFailedNotification";
    
    public static final String USER_FIRST_NAME = "\\{first-name\\}";
}