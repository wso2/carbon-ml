/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.ml.model.constants.MLModelConstants;
import org.wso2.carbon.ml.model.constants.SQLQueries;
import org.wso2.carbon.ml.model.exceptions.DatabaseHandlerException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class handles database transformations in model component
 */
public class DatabaseHandler {
    private static final Log logger = LogFactory.getLog(DatabaseHandler.class);
    private DataSource dataSource;

    /**
     * DatabaseHandler constructor
     *
     * @throws DatabaseHandlerException
     */
    DatabaseHandler() throws DatabaseHandlerException {
        try {
            Context initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup(MLModelConstants.ML_DB);
        } catch (Exception e) {
            throw new DatabaseHandlerException(e.getMessage(), e);
        }
    }

    protected void insertModelSettings(String modelSettingsID, String workflowID, String
            algorithmName, String algorithmClass, String response, double trainDataFraction,
                                       JSONObject hyperparameters)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // insert model settings to the database.
            connection = this.dataSource.getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_MODEL_SETTINGS);
            insertStatement.setString(1, modelSettingsID);
            insertStatement.setString(2, workflowID);
            insertStatement.setString(3, algorithmName);
            insertStatement.setString(4, algorithmClass);
            insertStatement.setString(5, response);
            insertStatement.setDouble(6, trainDataFraction);
            insertStatement.setObject(7, hyperparameters);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Succesfully updated model settings for model settings id " +
                             modelSettingsID);
            }
        } catch (SQLException e) {
            // rollback the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occured while inserting model settings for model settings id " +
                    modelSettingsID + " to the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    public <T,S>void insertModel(String modelID, String workflowExecutionID, T model,
                                        S modelSummary)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            connection = this.dataSource.getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ML_MODEL);
            insertStatement.setString(1, modelID);
            insertStatement.setString(2, workflowExecutionID);
            insertStatement.setObject(3, model);
            insertStatement.setObject(4, modelSummary);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the details of model: Model ID" + modelID);
            }

        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occured while inserting model id " +
                    modelID + " to the database: " + e.getMessage(), e);

        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }
}
