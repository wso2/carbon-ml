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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

/**
 * This class handles database connectivity in model component
 */
public class DatabaseHandler {
    private static final Log logger = LogFactory.getLog(DatabaseHandler.class);
    private DataSource dataSource;

    /**
     * DatabaseHandler constructor
     *
     * @throws DatabaseHandlerException
     */
    public DatabaseHandler() throws DatabaseHandlerException {
        try {
            Context initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup(MLModelConstants.ML_DB);
        } catch (Exception e) {
            throw new DatabaseHandlerException(
                    "An error occured while obtaining the data source: " + e.getMessage(), e);
        }
    }

    /**
     * This method inserts model settings to database
     *
     * @param modelSettingsID   Model settings ID
     * @param workflowID        Workflow ID
     * @param algorithmName     Machine learning algorithm name
     * @param algorithmClass    Type of machine learning algorithm: e.g. Classification
     * @param response          Name of the response variable
     * @param trainDataFraction Training data fraction
     * @param hyperparameters   Hyper-parameters
     * @throws DatabaseHandlerException
     */
    public void insertModelSettings(String modelSettingsID, String workflowID, String
            algorithmName, String algorithmClass, String response, double trainDataFraction,
            JSONObject hyperparameters)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // insert model settings to the database.
            connection = this.dataSource.getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ML_MODEL_SETTINGS);
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
                logger.debug(
                        "Succesfully updated model settings for model settings id " +
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

    public void insertModel(String modelID, String workflowID, Time executionStartTime)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // insert model settings to the database.
            connection = this.dataSource.getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ML_MODEL);
            insertStatement.setString(1, modelID);
            insertStatement.setString(2, workflowID);
            insertStatement.setTime(3, executionStartTime);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Succesfully inserted model details for model id " + modelID);
            }
        } catch (SQLException e) {
            // rollback the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occured while inserting model details for model id " + modelID + " " +
                    "to the database: " + e.getMessage(),
                    e);
        } finally {
            // enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    public <T, S> void updateModel(String modelID, T model,
            S modelSummary, Time executionEndTime)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = this.dataSource.getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_ML_MODEL);
            updateStatement.setObject(1, model);
            updateStatement.setObject(2, modelSummary);
            updateStatement.setTime(3, executionEndTime);
            updateStatement.setString(4, modelID);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the details of model: Model ID" + modelID);
            }

        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occured while updating the details of model id " + modelID + " : "
                    + e.getMessage(), e);

        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    public <T> T getModelSummary(String modelID) throws DatabaseHandlerException{
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            getStatement = connection.prepareStatement(SQLQueries.GET_MODEL_SUMMARY);
            getStatement.setString(1, modelID);
            result = getStatement.executeQuery();
            if (result.first()) {
                return (T)result.getObject(1);
            } else {
                logger.error("Invalid model ID: " + modelID);
                throw new DatabaseHandlerException("Invalid model ID: " + modelID);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occured while reading model summary for " +
                                               modelID + " from the database: " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }

    }
}
