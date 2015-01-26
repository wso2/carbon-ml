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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;

/**
 * ML Project Management Service component.
 * @scr.component name="projectManagementService" immediate="true"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService" cardinality="1..1" policy="dynamic"
 *                bind="setHttpService" unbind="unsetHttpService"
 */
public class MLProjectManagementDS {

    private static final Log logger = LogFactory.getLog(MLProjectManagementDS.class);
    @SuppressWarnings("unused")
    private static HttpService httpServiceInstance;
            
    protected void activate(ComponentContext context) {
        try {
            ProjectManagementService projectManagementService = new ProjectManagementService();
            context.getBundleContext().registerService(ProjectManagementService.class.getName(),
                                                       projectManagementService, null);
            logger.info("ML Project Management Service Started.");
            // TODO: Read from a config file
            logger.info("http://localhost:9763/new");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        logger.info("ML Project Management Service Stopped.");
    }
    
    protected void setHttpService(HttpService httpService) {
        httpServiceInstance = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        httpServiceInstance = null;
    }
}
