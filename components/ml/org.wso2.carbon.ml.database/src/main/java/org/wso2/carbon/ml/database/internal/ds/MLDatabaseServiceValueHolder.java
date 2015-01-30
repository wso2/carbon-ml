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
package org.wso2.carbon.ml.database.internal.ds;


import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class MLDatabaseServiceValueHolder {
    private static DatabaseService databaseService;
    private static AnalyticsDataService analyticsService;
    private static ConfigurationContextService contextService;

    public static  void registerDatabaseService(DatabaseService databaseService){
        MLDatabaseServiceValueHolder.databaseService = databaseService;
    }

    public static DatabaseService getDatabaseService(){
        return databaseService;
    }

    public static void setContextService(ConfigurationContextService contextService) {
        MLDatabaseServiceValueHolder.contextService = contextService;
    }

    public static ConfigurationContextService getContextService() {
        return contextService;
    }

    public static AnalyticsDataService getAnalyticsService() {
        return analyticsService;
    }

    public static void setAnalyticsService(AnalyticsDataService analyticsService) {
        MLDatabaseServiceValueHolder.analyticsService = analyticsService;
    }
}
