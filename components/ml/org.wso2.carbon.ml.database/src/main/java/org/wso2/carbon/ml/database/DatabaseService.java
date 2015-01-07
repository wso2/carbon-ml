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
package org.wso2.carbon.ml.database;

import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.database.dto.HyperParameter;
import org.wso2.carbon.ml.database.dto.Workflow;
import org.wso2.carbon.ml.database.dto.ModelSummary;
import java.sql.Time;
import java.util.List;

public interface DatabaseService {

    /**
     *
     * @param modelSettingsID
     * @param workflowID
     * @param algorithmName
     * @param algorithmClass
     * @param response
     * @param trainDataFraction
     * @param hyperparameters
     * @throws DatabaseHandlerException
     */
    public void insertModelSettings(String modelSettingsID, String workflowID, String
            algorithmName, String algorithmClass, String response, double trainDataFraction,
            List<HyperParameter> hyperparameters) throws DatabaseHandlerException;

    /**
     *
     * @param modelID
     * @param workflowID
     * @param executionStartTime
     * @throws DatabaseHandlerException
     */
    public void insertModel(String modelID, String workflowID, Time executionStartTime)
            throws DatabaseHandlerException;

    /**
     *
     * @param modelID
     * @param model
     * @param modelSummary
     * @param executionEndTime
     * @param <T>
     * @throws DatabaseHandlerException
     */
    public <T> void updateModel(String modelID, T model,
                                ModelSummary modelSummary, Time executionEndTime)
            throws DatabaseHandlerException;

    /**
     *
     * @param modelID
     * @return
     * @throws DatabaseHandlerException
     */
    public ModelSummary getModelSummary(String modelID) throws DatabaseHandlerException;

    /**
     *
     * @param workflowID
     * @return
     * @throws DatabaseHandlerException
     */
    public Workflow getWorkflow(String workflowID) throws DatabaseHandlerException;

    /**
     *
     * @param modelId
     * @return
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionEndTime(String modelId) throws DatabaseHandlerException;

    /**
     *
     * @param modelId
     * @return
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionStartTime(String modelId) throws DatabaseHandlerException;
}
