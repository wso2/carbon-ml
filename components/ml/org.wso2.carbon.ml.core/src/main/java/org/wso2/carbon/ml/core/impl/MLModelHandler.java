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
import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLHyperParameter;
import org.wso2.carbon.ml.commons.domain.MLModelConfiguration;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

/**
 * {@link MLModelHandler} is responsible for handling/delegating all the model related requests.
 */
public class MLModelHandler {
    private static final Log log = LogFactory.getLog(MLModelHandler.class);
    private DatabaseService databaseService;

    public MLModelHandler() {
        databaseService = MLCoreServiceValueHolder.getInstance().getDatabaseService();
    }

    public void createModel(MLModelNew model) throws MLModelHandlerException {
        try {
            int tenantId = model.getTenantId();
            String userName = model.getUserName();
            String analysisName = model.getAnalysisName();
            long analysisId = databaseService.getAnalysisId(tenantId, userName, analysisName);

            String valueSetName = model.getValueSetName();
            long valueSetId = databaseService.getValueSetId(valueSetName, tenantId);
            databaseService.insertModel(analysisId, valueSetId, tenantId, userName);
            log.info(String.format("[Created] %s", model));
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public void deleteModel(int tenantId, String userName, String analysisName) throws MLModelHandlerException {
        // TODO try {
        // databaseService.deleteAnalysis(tenantId, userName, analysisName);
        // log.info(String.format("[Deleted] [analysis] %s of [user] %s of [tenant] %s", analysisName, userName,
        // tenantId));
        // } catch (DatabaseHandlerException e) {
        // throw new MLAnalysisHandlerException(e);
        // }
    }

    public long getModelId(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        try {
            return databaseService.getAnalysisId(tenantId, userName, modelName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }
    
    public void addCustomizedFeatures(long modelId, List<MLCustomizedFeature> customizedFeatures) throws MLModelHandlerException {
        try {
            databaseService.insertFeatureCustomized(modelId, customizedFeatures);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }
    
    public void addModelConfigurations(long modelId, List<MLModelConfiguration> modelConfigs) throws MLModelHandlerException {
        try {
            databaseService.insertModelConfigurations(modelId, modelConfigs);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }
    
    public void addHyperParameters(long modelId, List<MLHyperParameter> hyperParameters) throws MLModelHandlerException {
        try {
            databaseService.insertHyperParameters(modelId, hyperParameters);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

}
