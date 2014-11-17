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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {

	private static volatile DatabaseHandler databaseHandler = null;
	private Connection connection;
	private static final Log logger = LogFactory.getLog(DatabaseHandler.class);

	/*
	 * private Constructor to prevent any other class from instantiating.
	 */
	private DatabaseHandler() {
	}

	/**
	 * Creates a singleton DatabaseHandler instance and returns it.
	 *
	 * @return
	 * @throws DatabaseHandlerException
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
						DataSource ds = (DataSource) initContext.lookup("jdbc/WSO2ML_DB");
						databaseHandler.connection = ds.getConnection();
						// enable auto commit
						databaseHandler.connection.setAutoCommit(true);
					}
				}
			}
			return databaseHandler;
		} catch (Exception e) {
			String msg = "Error occured while connecting to database. " + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		}
	}

	/**
	 * Creates a new project
	 *
	 * @param projectName
	 * @param description
	 * @return
	 * @throws DatabaseHandlerException
	 */
	public void createProject(String projectID, String projectName, String description)
			throws DatabaseHandlerException {
		PreparedStatement createProjectStatement = null;
		try {
			connection.setAutoCommit(false);
			createProjectStatement = connection.prepareStatement(SQLQueries.CREATE_PROJECT);
			createProjectStatement.setString(1, projectID);
			createProjectStatement.setString(2, projectName);
			createProjectStatement.setString(3, description);
			createProjectStatement.execute();
			connection.commit();
			logger.debug("Successfully inserted details of project: " + projectName +
			             ". Project ID" + projectID.toString());
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while inserting details of project: " + projectName +
					" to the database.\n" + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeStatement(createProjectStatement);
		}
	}

	/**
	 * Delete details of a given project from the database
	 *
	 * @param projectId
	 * @throws DatabaseHandlerException
	 */
	public void deleteProject(String projectId) throws DatabaseHandlerException {
		PreparedStatement deleteProjectStatement = null;
		try {
			connection.setAutoCommit(false);
			deleteProjectStatement = connection.prepareStatement(SQLQueries.DELETE_PROJECT);
			deleteProjectStatement.setString(1, projectId);
			deleteProjectStatement.execute();
			connection.commit();
			logger.debug("Successfully deleted the project: " + projectId);
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while deleting the project: " + projectId + ".\n" +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeStatement(deleteProjectStatement);
		}
	}

	/**
	 * Assign a tenant to a given project
	 *
	 * @param tenantID
	 * @param projectID
	 * @throws DatabaseHandlerException
	 */
	public void addTenantToProject(String tenantID, String projectID)
			throws DatabaseHandlerException {
		PreparedStatement addTenantStatement = null;
		try {
			connection.setAutoCommit(false);
			addTenantStatement = connection.prepareStatement(SQLQueries.ADD_TENANT_TO_PROJECT);
			addTenantStatement.setString(1, tenantID);
			addTenantStatement.setString(2, projectID);
			addTenantStatement.execute();
			connection.commit();
			logger.debug("Successfully added the tenant: " + tenantID + " to the project: " + "" +
					projectID);
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while adding the tenant: " + tenantID +
					" to the project: " + "" + projectID + ".\n" + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeStatement(addTenantStatement);
		}
	}

	/**
	 * Get the project names and created dates, that a tenant is assigned to
	 *
	 * @param tenantID
	 * @return
	 * @throws DatabaseHandlerException
	 */
	public String[][] getTenantProjects(String tenantID) throws DatabaseHandlerException {
		PreparedStatement getTenantProjectsStatement = null;
		ResultSet result = null;
		String[][] projects = null;
		try {
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
				projects = new String[3][size];
				result.beforeFirst();
				// put the result set to the string array
				for (int i = 0; i < size; i++) {
					result.next();
					projects[0][i] = result.getObject(1).toString();
					projects[1][i] = result.getString(2);
					projects[2][i] = result.getDate(3).toString();
				}
			}
			return projects;
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while retrieving the projects of user: " + tenantID +
					".\n" + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// close the database resources
			MLDatabaseUtil.closeStatement(getTenantProjectsStatement);
		}
	}

	/**
	 * Returns the ID of the dataset associated with the project
	 *
	 * @param projectId
	 * @return
	 * @throws DatabaseHandlerException
	 */
	public String getdatasetID(String projectId) throws DatabaseHandlerException {
		PreparedStatement getDatasetID = null;
		ResultSet result = null;
		try {
			getDatasetID = connection.prepareStatement(SQLQueries.GET_DATASET_ID);
			getDatasetID.setString(1, projectId);
			result = getDatasetID.executeQuery();
			result.first();
			return result.getObject(1).toString();
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while retrieving the Dataset Id of project: " + projectId +
					".\n" + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// close the database resources
			MLDatabaseUtil.closeStatement(getDatasetID);
		}
	}

	/**
	 * Creates a new work-flow
	 *
	 * @param workflowID
	 * @param parentWorkflowID
	 * @param projectID
	 * @param datasetID
	 * @param projectName
	 * @throws DatabaseHandlerException
	 */
	public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID,
	                              String datasetID, String projectName)
	                            		  throws DatabaseHandlerException {
		PreparedStatement createNewWorkflow = null;
		try {
			connection.setAutoCommit(false);
			createNewWorkflow = connection.prepareStatement(SQLQueries.CREATE_NEW_WORKFLOW);
			createNewWorkflow.setString(1, workflowID);
			createNewWorkflow.setString(2, parentWorkflowID);
			createNewWorkflow.setString(3, projectID);
			createNewWorkflow.setString(4, datasetID);
			createNewWorkflow.setString(5, projectName);
			createNewWorkflow.execute();
			connection.commit();
			logger.debug("Successfully created workflow: " + workflowID);
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"An error occured while creating a new workflow: " + workflowID + "\n" +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeStatement(createNewWorkflow);
		}
	}

	public void deleteWorkflow(String workflowID)
	                            		  throws DatabaseHandlerException {
		PreparedStatement deleteWorkflow = null;
		try {
			connection.setAutoCommit(false);
			deleteWorkflow = connection.prepareStatement(SQLQueries.DELETE_WORKFLOW);
			deleteWorkflow.setString(1, workflowID);
			deleteWorkflow.execute();
			connection.commit();
			logger.debug("Successfully deleted workflow: " + workflowID);
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"An error occured while deleting workflow: " + workflowID + "\n" +
							e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// enable auto commit
			MLDatabaseUtil.enableAutoCommit(connection);
			// close the database resources
			MLDatabaseUtil.closeStatement(deleteWorkflow);
		}
	}
	
	
	/**
	 * 
	 * @param projectId
	 * @return
	 * @throws DatabaseHandlerException
	 */
	public String[][] getProjectWorkflows(String projectId) throws DatabaseHandlerException {
		PreparedStatement getProjectWorkflows = null;
		ResultSet result = null;
		String[][] workFlows = null;
		try {
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
				workFlows = new String[2][noOfWorkflows];
				result.beforeFirst();
				// put the result set to the string array
				for (int i = 0; i < noOfWorkflows; i++) {
					result.next();
					workFlows[0][i] = result.getString(1);
					workFlows[1][i] = result.getString(2);
				}
			}
			return workFlows;
		} catch (SQLException e) {
			MLDatabaseUtil.rollBack(connection);
			String msg =
					"Error occured while retrieving the Dataset Id of project: " + projectId +
					".\n" + e.getMessage();
			logger.error(msg, e);
			throw new DatabaseHandlerException(msg);
		} finally {
			// close the database resources
			MLDatabaseUtil.closeStatement(getProjectWorkflows);
		}
	}
}
