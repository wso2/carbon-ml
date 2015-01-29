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
package org.wso2.carbon.ml.project.mgt.internal.ds;

import org.wso2.carbon.identity.notification.mgt.NotificationSender;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;

public class MLProjectManagementServiceValueHolder {
    private static DatabaseService databaseService;
    private static ProjectManagementService projectManagementService;
    //private static NotificationSender notificationSender;

    public static  void registerDatabaseService(DatabaseService databaseService){
        MLProjectManagementServiceValueHolder.databaseService = databaseService;
    }

    public static DatabaseService getDatabaseService(){
        return databaseService;
    }
    
    public static  void registerProjectManagementService(ProjectManagementService projectManagementService){
        MLProjectManagementServiceValueHolder.projectManagementService = projectManagementService;
    }

    public static ProjectManagementService getProjectManagementService(){
        return projectManagementService;
    }

    /*public static NotificationSender getNotificationSender() {
        return notificationSender;
    }

    public static void registerNotificationSender(NotificationSender notificationSender) {
        MLProjectManagementServiceValueHolder.notificationSender = notificationSender;
    }*/
}
