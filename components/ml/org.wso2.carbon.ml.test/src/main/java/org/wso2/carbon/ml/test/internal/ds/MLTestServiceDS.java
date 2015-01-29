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
package org.wso2.carbon.ml.test.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.dataset.DatasetService;
import org.wso2.carbon.ml.model.ModelService;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;
import org.wso2.carbon.ml.test.TestMLFlow;

/**
 * @scr.component name="MLTestService" immediate="true"
 * @scr.reference name="databaseService" interface="org.wso2.carbon.ml.database.DatabaseService" cardinality="1..1"
 *                policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 * @scr.reference name="datasetService" interface="org.wso2.carbon.ml.dataset.DatasetService" cardinality="1..1"
 *                policy="dynamic" bind="setDatasetService" unbind="unsetDatasetService"
 * @scr.reference name="modelService" interface="org.wso2.carbon.ml.model.ModelService" cardinality="1..1"
 *                policy="dynamic" bind="setModelService" unbind="unsetModelService"
 * @scr.reference name="projectMgmtService" interface="org.wso2.carbon.ml.project.mgt.ProjectManagementService"
 *                cardinality="1..1" policy="dynamic" bind="setProjectManagementService"
 *                unbind="unsetProjectManagementService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService" cardinality="1..1" policy="dynamic"
 *                bind="setHttpService" unbind="unsetHttpService"
 */
public class MLTestServiceDS {

    private static final Log log = LogFactory.getLog(MLTestServiceDS.class);
    @SuppressWarnings("unused")
    private static HttpService httpServiceInstance;

    protected void activate(ComponentContext context) {
        try {
            TestMLFlow flow = new TestMLFlow();
            flow.testMLFlow();
        } catch (Throwable e) {
            log.error("Could not create ModelService: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        MLTestServiceValueHolder.registerDatabaseService(null);
    }

    protected void setDatabaseService(DatabaseService databaseService) {
        MLTestServiceValueHolder.registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService) {
        MLTestServiceValueHolder.registerDatabaseService(null);
    }

    protected void setDatasetService(DatasetService datasetService) {
        MLTestServiceValueHolder.registerDatasetService(datasetService);
    }

    protected void unsetDatasetService(DatasetService datasetService) {
        MLTestServiceValueHolder.registerDatasetService(null);
    }

    protected void setModelService(ModelService modelService) {
        MLTestServiceValueHolder.registerModelService(modelService);
    }

    protected void unsetModelService(ModelService modelService) {
        MLTestServiceValueHolder.registerModelService(null);
    }

    protected void setProjectManagementService(ProjectManagementService projectMgtService) {
        MLTestServiceValueHolder.registerProjectMgtService(projectMgtService);
    }

    protected void unsetProjectManagementService(ProjectManagementService projectMgtService) {
        MLTestServiceValueHolder.registerDatabaseService(null);
    }

    protected void setHttpService(HttpService httpService) {
        httpServiceInstance = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        httpServiceInstance = null;
    }
}
