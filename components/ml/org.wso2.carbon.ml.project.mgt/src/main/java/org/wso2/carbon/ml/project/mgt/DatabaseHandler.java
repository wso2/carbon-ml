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

package org.wso2.carbon.ml.project.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.project.mgt.exceptions.DatabaseHandlerException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {

	private static volatile DatabaseHandler databaseHandler = null;
	private static DataSource dataSource;
	private static final Log logger = LogFactory.getLog(DatabaseHandler.class);

	/*
	 * private Constructor to prevent any other class from instantiating.
	 */
	private DatabaseHandler() {
	}

	/**
	 * Creates a singleton DatabaseHandler instance and returns it.
	 *
	 * @return     A singleton DatabaseHandler instance
	 * @throws     DatabaseHandlerException
	 */
	public static DatabaseHandler getDatabaseHandler() throws DatabaseHandlerException {
		try {
			if (databaseHandler == null) {
				synchronized (DatabaseHandler.class) {
					if (databaseHandler == null) {
						databaseHandler = new DatabaseHandler();
						// load the carbon data source configurations of the H2
						// database
						Context initContext = new InitialContext();
						dataSource = (DataSource) initContext.lookup("jdbc/WSO2ML_DB");
					}
				}
			}
			return databaseHandler;
		} catch (Exception e) {
			throw new DatabaseHandlerException("Error occurred while connecting to database: " +
					e.getMessage(), e);
		}
	}

	/**
	 * Creates a new project.
	 *
	 * @param projectID        Unique identifier for the project
	 * @param projectName      Name of the project
	 * @param description      Description of the project
	 * @throws                 DatabaseHandlerException
	 */
	public void createProject(String projectID, String projectName, String description)
			throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement createProjectStatement = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			createProjectStatement = connection.prepareStatement(SQLQueries.CREATE_PROJECT);
			createProjectStatement.setString(1, projectID);
			createProjectStatement.setString(2, projectName);
			createProjectStatement.setString(3, description);
			createProjectStatement.execute();
			connection.commit();
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully inserted details of project: " + projectName +
				             ". Project ID: " + projectID.toString());
			}
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException(
			                                   "Error occurred while inserting details of project: " +
			                                		   projectName +
			                                		   " to the database: " +
			                                		   e.getMessage(),e);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, createProjectStatement);
		}
	}

	/**
	 * Delete details of a given project from the database.
	 *
	 * @param projectId    Unique identifier for the project
	 * @throws             DatabaseHandlerException
	 */
	public void deleteProject(String projectId) throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement deleteProjectStatement = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			deleteProjectStatement = connection.prepareStatement(SQLQueries.DELETE_PROJECT);
			deleteProjectStatement.setString(1, projectId);
			deleteProjectStatement.execute();
			connection.commit();
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully deleted the project: " + projectId);
			}
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException("Error occurred while deleting the project: " +
					projectId + ": " + e.getMessage(),e);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, deleteProjectStatement);
		}
	}

	/**
	 * Assign a tenant to a given project.
	 *
	 * @param tenantID     Unique identifier for the current tenant.
	 * @param projectID    Unique identifier for the project.
	 * @throws             DatabaseHandlerException
	 */
	public void addTenantToProject(String tenantID, String projectID)
			throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement addTenantStatement = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			addTenantStatement = connection.prepareStatement(SQLQueries.ADD_TENANT_TO_PROJECT);
			addTenantStatement.setString(1, tenantID);
			addTenantStatement.setString(2, projectID);
			addTenantStatement.execute();
			connection.commit();
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully added the tenant: " + tenantID + " to the project: " +
						projectID);
			}
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException("Error occurred while adding the tenant " + tenantID +
			                                   " to the project " + projectID + ": " +
			                                   e.getMessage(),e);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, addTenantStatement);
		}
	}

	/**
	 * Get the project names and created dates, that a tenant is assigned to.
	 *
	 * @param tenantID     Unique identifier for the tenant.
	 * @return             An array of project ID, Name and the created date of the projects 
	 *                     associated with a given tenant.
	 * @throws             DatabaseHandlerException.
	 */
	public String[][] getTenantProjects(String tenantID) throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement getTenantProjectsStatement = null;
		ResultSet result = null;
		String[][] projects = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
			getTenantProjectsStatement =
					connection.prepareStatement(SQLQueries.GET_TENANT_PROJECTS,
					                            ResultSet.TYPE_SCROLL_INSENSITIVE,
					                            ResultSet.CONCUR_READ_ONLY);
			getTenantProjectsStatement.setString(1, tenantID);
			result = getTenantProjectsStatement.executeQuery();
			// create a 2-d string array having the size of the result set
			result.last();
			int size = result.getRow();
			if (size > 0) {
				projects = new String[size][3];
				result.beforeFirst();
				// put the result set to the string array
				for (int i = 0; i < size; i++) {
					result.next();
					projects[i][0] = result.getObject(1).toString();
					projects[i][1] = result.getString(2);
					projects[i][2] = result.getDate(3).toString();
				}
			}
			return projects;
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException(
			                                   "Error occurred while retrieving the projects of user " +
			                                		   tenantID + ": " + e.getMessage(),e);
		} finally {
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, getTenantProjectsStatement, result);
		}
	}

	/**
	 * Returns the ID of the data-set associated with the project.
	 *
	 * @param projectId    Unique identifier for the project.
	 * @return             ID of the data-set associated with the project.
	 * @throws             DatabaseHandlerException.
	 */
	public String getdatasetID(String projectId) throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement getDatasetID = null;
		ResultSet result = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
			getDatasetID = connection.prepareStatement(SQLQueries.GET_DATASET_ID);
			getDatasetID.setString(1, projectId);
			result = getDatasetID.executeQuery();
			result.first();
			return result.getObject(1).toString();
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException(
			                                   "Error occurred while retrieving the Dataset Id of project " +
			                                		   projectId + ": " + e.getMessage(),e);
		} finally {
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, getDatasetID, result);
		}
	}

	/**
	 * Creates a new work-flow.
	 *
	 * @param workflowID           Unique identifier for the new workflow.
	 * @param parentWorkflowID     Unique identifier for the workflow from which the current workflow
	 *                             is inherited from.
	 * @param projectID            Unique identifier for the project for which the workflow is created.
	 * @param datasetID            Unique identifier for the data-set associated with the workflow.
	 * @param workflowName         Name of the project.
	 * @throws                     DatabaseHandlerException
	 */
	public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID,
	                              String datasetID, String workflowName)
	                            		  throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement createNewWorkflow = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			createNewWorkflow = connection.prepareStatement(SQLQueries.CREATE_NEW_WORKFLOW);
			createNewWorkflow.setString(1, workflowID);
			createNewWorkflow.setString(2, parentWorkflowID);
			createNewWorkflow.setString(3, projectID);
			createNewWorkflow.setString(4, datasetID);
			createNewWorkflow.setString(5, workflowName);
			createNewWorkflow.execute();
			connection.commit();
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully created workflow: " + workflowID);
			}
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException("An error occurred while creating a new workflow " +
					workflowID + ": " + e.getMessage(),e);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, createNewWorkflow);
		}
	}

	public void updateWorkdflowName(String workflowId, String name) throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement updateWorkflow = null;

		try{
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			updateWorkflow = connection.prepareStatement(SQLQueries.UPDATE_WORKFLOW_NAME);

			updateWorkflow.setString(1, name);
			updateWorkflow.setString(2, workflowId);
			updateWorkflow.executeUpdate();
			connection.commit();

			if(logger.isDebugEnabled()){
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully updated workflow: " + workflowId);
				}
			}
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException("An error occurred while updating workflow " +
					workflowId + ": " + e.getMessage(),e);
		}finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);

			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, updateWorkflow);
		}
	}

	/**
	 * Deletes a workflow.
	 *
	 * @param workflowID   Unique identifier of the workflow to be deleted
	 * @throws             DatabaseHandlerException
	 */
	public void deleteWorkflow(String workflowID)
			throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement deleteWorkflow = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			deleteWorkflow = connection.prepareStatement(SQLQueries.DELETE_WORKFLOW);
			deleteWorkflow.setString(1, workflowID);
			deleteWorkflow.execute();
			connection.commit();
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully deleted workflow: " + workflowID);
			}
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException("An error occurred while deleting workflow " +
					workflowID + ": " + e.getMessage(),e);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(deleteWorkflow);
		}
	}

	/**
	 * Get a list of workflows associated with a given project.
	 *
	 * @param projectId    Unique identifier for the project for which the wokflows are needed
	 * @return             An array of workflow ID's and Names
	 * @throws             DatabaseHandlerException
	 */
	public String[][] getProjectWorkflows(String projectId) throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement getProjectWorkflows = null;
		ResultSet result = null;
		String[][] workFlows = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
			getProjectWorkflows =
					connection.prepareStatement(SQLQueries.GET_PROJECT_WORKFLOWS,
					                            ResultSet.TYPE_SCROLL_INSENSITIVE,
					                            ResultSet.CONCUR_READ_ONLY);
			getProjectWorkflows.setString(1, projectId);
			result = getProjectWorkflows.executeQuery();

			// create a 2-d string array having the size of the result set
			result.last();
			int noOfWorkflows = result.getRow();
			if (noOfWorkflows > 0) {
				workFlows = new String[noOfWorkflows][2];
				result.beforeFirst();
				// put the result set to the string array
				for (int i = 0; i < noOfWorkflows; i++) {
					result.next();
					workFlows[i][0] = result.getString(1);
					workFlows[i][1] = result.getString(2);
				}
			}
			return workFlows;
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException(
			                                   "Error occurred while retrieving the Dataset Id of project " +
			                                		   projectId + ": " + e.getMessage(),e);
		} finally {
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, getProjectWorkflows, result);
		}
	}

	/**
	 * Set the default values for feature properties of a given workflow.
	 *
	 * @param datasetID    Unique identifier of the data-set
	 * @param workflowID   Unique identifier of the current workflow
	 * @throws             DatabaseHandlerException
	 */
	protected void setDefaultFeatureSettings(String datasetID, String workflowID)
			throws DatabaseHandlerException {
		Connection connection = null;
		PreparedStatement insertStatement = null;
		PreparedStatement getDefaultFeatureSettings = null;
		ResultSet result = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
			// read default feature settings from data-set summary table
			getDefaultFeatureSettings =
					connection.prepareStatement(SQLQueries.GET_DEFAULT_FEATURE_SETTINGS);
			getDefaultFeatureSettings.setString(1, datasetID);
			result = getDefaultFeatureSettings.executeQuery();
			// insert default feature settings into feature settings table
			connection.setAutoCommit(false);
			while (result.next()) {
				insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_SETTINGS);
				insertStatement.setString(1, workflowID);
				insertStatement.setString(2, result.getString(1));
				insertStatement.setString(3, result.getString(2));
				insertStatement.setString(4, result.getString(3));
				insertStatement.setString(5, result.getString(4));
				insertStatement.setBoolean(6, result.getBoolean(5));
				insertStatement.execute();
				connection.commit();
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully inserted feature deafults of dataset: " + datasetID +
				             " of the workflow: " + datasetID);
			}
		} catch (SQLException e) {
			// rollback the changes
			MLDatabaseUtil.rollBack(connection);
			throw new DatabaseHandlerException(
			                                   "An error occurred while setting details of dataset " +
			                                		   datasetID + " of the workflow " +
			                                		   datasetID + " to the database:" +
			                                		   e.getMessage(), e);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeDatabaseResources(connection, insertStatement, result);
			MLDatabaseUtil.closeDatabaseResources(getDefaultFeatureSettings);
		}
	}
}
