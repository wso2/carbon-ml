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
package org.wso2.carbon.ml.decomposition.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.decomposition.DecompositionService;
import org.wso2.carbon.ml.decomposition.spark.SparkDecompositionService;

/**
 * @scr.component name="decompositionService" immediate="true"
 * @scr.reference name="databaseService"
 * interface="org.wso2.carbon.ml.database.DatabaseService" cardinality="1..1"
 * policy="dynamic" bind="setDatabaseService" unbind="unsetDatabaseService"
 */

/**
 * The DS used by the decomposition service.
 */
public class DecompositionServiceDS {

    private static final Log log = LogFactory.getLog(DecompositionServiceDS.class);
    protected void activate(ComponentContext context) {
        try {
            DecompositionService decompositionService = new SparkDecompositionService();
            DecompositionServiceValueHolder.registerModelService(decompositionService);

            context.getBundleContext().registerService(
                    DecompositionService.class.getName(), decompositionService, null);

        } catch (Throwable e) {
            log.error("Could not create ModelService: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        DecompositionServiceValueHolder.registerDatabaseService(null);
    }

    protected void setDatabaseService(DatabaseService databaseService){
        DecompositionServiceValueHolder.registerDatabaseService(databaseService);
    }

    protected void unsetDatabaseService(DatabaseService databaseService){
        DecompositionServiceValueHolder.registerDatabaseService(null);
    }
}
