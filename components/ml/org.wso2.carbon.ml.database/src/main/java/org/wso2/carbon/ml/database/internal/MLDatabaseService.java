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
package org.wso2.carbon.ml.database.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.dto.Feature;
import org.wso2.carbon.ml.database.dto.HyperParameter;
import org.wso2.carbon.ml.database.dto.ModelSummary;
import org.wso2.carbon.ml.database.dto.Workflow;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.database.internal.constants.SQLQueries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MLDatabaseService implements DatabaseService{

    private static final Log logger = LogFactory.getLog(MLDatabaseService.class);

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
                                    List<HyperParameter> hyperparameters)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // insert model settings to the database.
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
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
                        "Successfully updated model settings for model settings id " +
                                modelSettingsID);
            }
        } catch (SQLException e) {
            // rollback the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting model settings for model settings id " +
                            modelSettingsID + " to the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    /**
     * This method initialize insering model into the database
     *
     * @param modelID            Model ID
     * @param workflowID         Workflow ID
     * @param executionStartTime Model execution start time
     * @throws DatabaseHandlerException
     */
    public void insertModel(String modelID, String workflowID, Time executionStartTime)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // insert model settings to the database.
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();

            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ML_MODEL);
            insertStatement.setString(1, modelID);
            insertStatement.setString(2, workflowID);
            insertStatement.setTime(3, executionStartTime);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted model details for model id " + modelID);
            }
        } catch (SQLException e) {
            // rollback the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while inserting model details for model id " + modelID + " " +
                            "to the database: " + e.getMessage(),
                    e);
        } finally {
            // enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }
    /**
     * This method inserts model and model summary into the database
     *
     * @param modelID          Model ID
     * @param model            Machine learning model
     * @param modelSummary     Machine learning model summary
     * @param executionEndTime Model execution end time
     * @param <T>              Type of machine learning  model
     * @throws DatabaseHandlerException
     */
    public <T> void updateModel(String modelID, T model,
                                ModelSummary modelSummary, Time executionEndTime)
            throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_ML_MODEL);
            updateStatement.setObject(1, model);
            updateStatement.setObject(2, modelSummary);
            updateStatement.setTime(3, executionEndTime);
            updateStatement.setString(4, modelID);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the details of model: model ID" + modelID);
            }

        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while updating the details of model id " + modelID + " : "
                            + e.getMessage(), e);

        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * This method returns machine learning model summary
     *
     * @param modelID Model ID
     * @return Model summary
     * @throws DatabaseHandlerException
     */
    public ModelSummary getModelSummary(String modelID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            getStatement = connection.prepareStatement(SQLQueries.GET_MODEL_SUMMARY);
            getStatement.setString(1, modelID);
            result = getStatement.executeQuery();
            if (result.first()) {
                return (ModelSummary) result.getObject(1);
            } else {
                throw new DatabaseHandlerException("Invalid model ID: " + modelID);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading model summary for " +
                    modelID + " from the database: " + e.getMessage(),
                    e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }

    }

    /**
     *
     * @param workflowID Machine learning workflow ID
     * @return Returns a machine learning workflow object
     * @throws DatabaseHandlerException
     */
    public Workflow getWorkflow(String workflowID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            Workflow mlWorkflow = new Workflow();
            mlWorkflow.setWorkflowID(workflowID);
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            getStatement = connection.prepareStatement(SQLQueries.GET_DATASET_LOCATION);
            getStatement.setString(1, workflowID);
            result = getStatement.executeQuery();
            if (result.first()) {
                mlWorkflow.setDatasetURL(result.getString(1));
            }
            List<Feature> mlFeatures = new ArrayList();
            getStatement = connection.prepareStatement(SQLQueries.GET_ML_FEATURE_SETTINGS);
            getStatement.setString(1, workflowID);
            result = getStatement.executeQuery();
            while (result.next()) {
                // check whether to include the feature or not
                if (result.getBoolean(5) == true) {
                    Feature mlFeature = new Feature();
                    mlFeature.setName(result.getString(1));
                    mlFeature.setIndex(result.getInt(2));
                    mlFeature.setType(result.getString(3));
                    mlFeature.setImputeOption(result.getString(4));
                    mlFeature.setInclude(result.getBoolean(5));
                    mlFeatures.add(mlFeature);
                }
            }
            mlWorkflow.setFeatures(mlFeatures);
            getStatement = connection.prepareStatement(SQLQueries.GET_ML_MODEL_SETTINGS);
            getStatement.setString(1, workflowID);
            result = getStatement.executeQuery();
            if (result.first()) {
                mlWorkflow.setAlgorithmClass(result.getString(1));
                mlWorkflow.setAlgorithmName(result.getString(2));
                mlWorkflow.setResponseVariable(result.getString(3));
                mlWorkflow.setTrainDataFraction(result.getDouble(4));
                mlWorkflow.setHyperParameters((Map<String, String>) result.getObject(5));
            }
            return mlWorkflow;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    /**
     * Reads model execution completion time for a given model id.
     *
     * @param modelId
     * @return Returns the number of millis since Jan 1, 1970, 00:00:00 GMT represented by
     * model execution
     * end time.
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionEndTime(String modelId) throws DatabaseHandlerException {
        return getModelExecutionTime(modelId, SQLQueries.GET_MODEL_EXE_END_TIME);
    }

    /**
     * Read model execution start time for a given model id.
     *
     * @param modelId
     * @return Returns the number of millis since Jan 1, 1970, 00:00:00 GMT represented by model
     * execution
     * start time
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionStartTime(String modelId) throws DatabaseHandlerException {
        return getModelExecutionTime(modelId, SQLQueries.GET_MODEL_EXE_START_TIME);
    }

    /**
     * This helper class is used to extract model execution start/end time
     *
     * @param modelId
     * @param query
     * @return
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionTime(String modelId, String query)
            throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, modelId);
            result = statement.executeQuery();
            if (result.first()) {
                Timestamp time = result.getTimestamp(1);
                if (time != null) {
                    return time.getTime();
                }
                return 0;
            } else {
                throw new DatabaseHandlerException(
                        "No timestamp data associated with model id: " + modelId);
            }

        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while reading execution time from the database: " + e
                            .getMessage(), e);
        } finally {
            // closing database resources
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }
}
