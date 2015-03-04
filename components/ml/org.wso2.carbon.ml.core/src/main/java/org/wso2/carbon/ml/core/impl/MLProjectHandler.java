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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.core.domain.MLProject;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;

/**
 * {@link MLProjectHandler} is responsible for handling/delegating all the project management requests.
 */
public class MLProjectHandler {
    private static final Log log = LogFactory.getLog(MLProjectHandler.class);
    private DatabaseService databaseService;

    public MLProjectHandler() {
        databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
    }
    
    public void createProject(MLProject project) {
        //TODO persist a project
    }
    
    public void deleteProject(int tenantId, String userName, String projectName) {
        //TODO
    }
    
    public String getProjectId(int tenantId, String userName, String projectName) {
        //TODO
        return null;
    }


}
