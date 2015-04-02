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
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.FeatureSummary;
import org.wso2.carbon.ml.commons.domain.MLAnalysis;
import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLHyperParameter;
import org.wso2.carbon.ml.commons.domain.MLModelConfiguration;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.commons.domain.config.MLAlgorithm;
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
    private List<MLAlgorithm> algorithms;

    public MLAnalysisHandler() {
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        databaseService = valueHolder.getDatabaseService();
        algorithms = valueHolder.getAlgorithms();
    }
    
    public void createAnalysis(MLAnalysis analysis) throws MLAnalysisHandlerException {
        try {
            databaseService.insertAnalysis(analysis);
            log.info(String.format("[Created] %s", analysis));
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public void addCustomizedFeatures(long analysisId, List<MLCustomizedFeature> customizedFeatures, int tenantId, String userName)
            throws MLAnalysisHandlerException {
        try {
            databaseService.insertFeatureCustomized(analysisId, customizedFeatures, tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public void addDefaultsIntoCustomizedFeatures(long analysisId, MLCustomizedFeature customizedValues)
            throws MLAnalysisHandlerException {
        try {
            databaseService.insertDefaultsIntoFeatureCustomized(analysisId, customizedValues);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }

    public void addCustomizedFeature(long analysisId, MLCustomizedFeature customizedFeature)
            throws MLAnalysisHandlerException {
        try {
            databaseService.insertFeatureCustomized(analysisId, customizedFeature);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public List<FeatureSummary> getSummarizedFeatures(int tenantId, String userName, long analysisId, int limit, int offset) throws MLAnalysisHandlerException {
        try {
            return databaseService.getFeatures(tenantId, userName, analysisId, limit, offset);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }

    public List<String> getFeatureNames(String analysisId, String featureType) throws MLAnalysisHandlerException {
        try {
            return databaseService.getFeatureNames(analysisId, featureType);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public String getResponseVariable(long analysisId) throws MLAnalysisHandlerException {
        try {
            return databaseService.getAStringModelConfiguration(analysisId, MLConstants.RESPONSE);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public String getSummaryStats(int tenantId, String userName, long analysisId, String featureName) throws MLAnalysisHandlerException {
        try {
            return databaseService.getSummaryStats(tenantId, userName, analysisId, featureName);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }

    public void addModelConfigurations(long analysisId, List<MLModelConfiguration> modelConfigs)
            throws MLAnalysisHandlerException {
        try {
            databaseService.insertModelConfigurations(analysisId, modelConfigs);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }

    public void addHyperParameters(long analysisId, List<MLHyperParameter> hyperParameters) throws MLAnalysisHandlerException {
        try {
            databaseService.insertHyperParameters(analysisId, hyperParameters);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }
    
    public void addDefaultsIntoHyperParameters(long analysisId) throws MLAnalysisHandlerException {
        try {
            // read the algorithm name of this model
            String algorithmName = databaseService.getAStringModelConfiguration(analysisId, MLConstants.ALGORITHM_NAME);
            if (algorithmName == null) {
                throw new MLAnalysisHandlerException("You have to set the model configurations (algorithm name) before loading default hyper parameters for model [id] "+analysisId);
            }
            // get the MLAlgorithm and then the hyper params of the model's algorithm
            List<MLHyperParameter> hyperParameters = null;
            for (MLAlgorithm mlAlgorithm : algorithms) {
                if (algorithmName.equalsIgnoreCase(mlAlgorithm.getName())) {
                    hyperParameters = mlAlgorithm.getParameters();
                    break;
                }
            }
            if (hyperParameters == null) {
                throw new MLAnalysisHandlerException("Cannot find the default hyper parameters for algorithm [name] "+algorithmName);
            }
            // add default hyper params
            databaseService.insertHyperParameters(analysisId, hyperParameters);
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
    
    public List<MLModelNew> getAllModelsOfAnalysis(int tenantId, String userName, long analysisId) throws MLAnalysisHandlerException {
        try {
            return databaseService.getAllModels(tenantId, userName, analysisId);
        } catch (DatabaseHandlerException e) {
            throw new MLAnalysisHandlerException(e);
        }
    }

}
