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
package org.wso2.carbon.ml.model.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.model.ModelService;
import org.wso2.carbon.ml.model.internal.SparkModelService;

/**
 * @scr.component name="modelService" immediate="true"
 * @scr.reference name="databaseService"
 * interface="org.wso2.carbon.ml.database.DatabaseService" cardinality="1..1"
 * policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 */
public class MLModelServiceDS {

    private static final Log log = LogFactory.getLog(MLModelServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            ModelService modelService = new SparkModelService();
            MLModelServiceValueHolder.registerModelService(modelService);

            context.getBundleContext().registerService(ModelService.class.getName(), modelService, null);

        } catch (Throwable e) {
            log.error("Could not create ModelService: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        MLModelServiceValueHolder.registerDatabaseService(null);
    }

    protected void setDatabaseService(DatabaseService databaseService){
        MLModelServiceValueHolder.registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService){
        MLModelServiceValueHolder.registerDatabaseService(null);
    }
}