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
package org.wso2.carbon.ml.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.commons.domain.MLProject;
import org.wso2.carbon.ml.core.exceptions.MLProjectHandlerException;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

/**
 * {@link MLProjectHandler} is responsible for handling/delegating all the project management requests.
 */
public class MLProjectHandler {
    private static final Log log = LogFactory.getLog(MLProjectHandler.class);
    private DatabaseService databaseService;

    public MLProjectHandler() {
        databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
    }
    
    public void createProject(MLProject project) throws MLProjectHandlerException {
        try {
            databaseService.createProject(project.getName(), project.getDescription(), project.getTenantId(), project.getUserName());
            log.info(String.format("[Created] %s", project));
        } catch (DatabaseHandlerException e) {
            throw new MLProjectHandlerException(e);
        }
    }
    
    public void deleteProject(int tenantId, String userName, String projectName) throws MLProjectHandlerException {
        try {
            databaseService.deleteProject(tenantId, userName, projectName);
            log.info(String.format("[Deleted] [project] %s of [user] %s of [tenant] %s", projectName, userName, tenantId));
        } catch (DatabaseHandlerException e) {
            throw new MLProjectHandlerException(e);
        }
    }
    
    public long getProjectId(int tenantId, String userName, String projectName) throws MLProjectHandlerException {
        try {
            return databaseService.getProjectId(tenantId, userName, projectName);
        } catch (DatabaseHandlerException e) {
            throw new MLProjectHandlerException(e);
        }
    }


}
