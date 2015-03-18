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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.commons.domain.MLAnalysis;
import org.wso2.carbon.ml.core.exceptions.MLAnalysisHandlerException;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

/**
 * {@link MLAnalysisHandler} is responsible for handling/delegating all the analysis related requests.
 */
public class MLAnalysisHandler {
    private static final Log log = LogFactory.getLog(MLAnalysisHandler.class);
    private DatabaseService databaseService;

    public MLAnalysisHandler() {
        databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
    }
    
    public void createAnalysis(MLAnalysis analysis) throws MLAnalysisHandlerException {
        try {
            databaseService.insertAnalysis(analysis);
            log.info(String.format("[Created] %s", analysis));
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public void deleteAnalysis(int tenantId, String userName, String analysisName) throws MLAnalysisHandlerException {
        try {
            databaseService.deleteAnalysis(tenantId, userName, analysisName);
            log.info(String.format("[Deleted] [analysis] %s of [user] %s of [tenant] %s", analysisName, userName, tenantId));
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public long getAnalysisId(int tenantId, String userName, String analysisName) throws MLAnalysisHandlerException {
        try {
            return databaseService.getAnalysisId(tenantId, userName, analysisName);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public MLAnalysis getAnalysis(int tenantId, String userName, String analysisName) throws MLAnalysisHandlerException {
        try {
            return databaseService.getAnalysis(tenantId, userName, analysisName);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }

    public List<MLAnalysis> getAnalyses(int tenantId, String userName) throws MLAnalysisHandlerException {
        try {
            return databaseService.getAllAnalyses(tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }

}
