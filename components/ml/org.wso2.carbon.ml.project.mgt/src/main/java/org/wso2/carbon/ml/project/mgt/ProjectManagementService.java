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
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ml.project.mgt.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.project.mgt.exceptions.ProjectManagementServiceException;

/**
 * Class contains services related to project and workflow management
 *
 * @scr.component name="projectManagementService" immediate="true"
 */
public class ProjectManagementService {
	private static final Log logger = LogFactory.getLog(ProjectManagementService.class);

	protected void activate(ComponentContext context) {
		try {
			ProjectManagementService projectManagementService = new ProjectManagementService();
			context.getBundleContext().registerService(ProjectManagementService.class.getName(),
			                                           projectManagementService, null);
			logger.info("ML Project Management Service Started.");
			// TODO: Read from a config file
			logger.info("http://localhost:9763/mlUI/");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected void deactivate(ComponentContext context) {
		logger.info("ML Project Management Service Stopped.");
	}

	/**
	 * Creates a new project.
	 *
	 * @param projectID
	 *            Unique identifier of the project
	 * @param projectName
	 *            Name of the project
	 * @param description
	 *            Description of the project
	 * @throws ProjectManagementServiceException
	 */
	public void createProject(String projectID, String projectName, String description)
			throws
			ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.createProject(projectID, projectName, description);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to create the project: " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to create the project: " +
					e.getMessage(),e);
		}
	}

	/**
	 * Delete details of a given project.
	 *
	 * @param projectId
	 *            Unique identifier of the project
	 * @throws ProjectManagementServiceException
	 */
	public void deleteProject(String projectId) throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.deleteProject(projectId);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to delete the project: " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to delete the project: " +
					e.getMessage(),e);
		}
	}

	/**
	 * Assign a tenant to a given project.
	 *
	 * @param tenantID
	 *            Unique identifier of the tenant
	 * @param projectId
	 *            Unique identifier of the project
	 * @throws ProjectManagementServiceException
	 */
	public void addTenantToProject(String tenantID, String projectId)
			throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.addTenantToProject(tenantID, projectId);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to add the tenant to project: " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to add the tenant to project: " +
					e.getMessage(),e);
		}
	}

	/**
	 * Get the project names and created dates, that a tenant is assigned to.
	 *
	 * @param tenantID
	 *            Unique identifier of the tenant
	 * @return An array of project ID, Name and the created date of the projects
	 *         associated with a given tenant
	 * @throws ProjectManagementServiceException
	 */
	public String[][] getTenantProjects(String tenantID) throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.getTenantProjects(tenantID);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to get tenant's projects. " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to get tenant's projects: " +
					e.getMessage(),e);
		}
	}

	/**
	 * Returns the ID of the data-set associated with the project.
	 *
	 * @param projectId
	 *            Unique identifier of the project
	 * @return Unique identifier of the data-set associated with the project
	 * @throws ProjectManagementServiceException
	 */
	public String getdatasetID(String projectId) throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.getdatasetID(projectId);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to return dataset ID. " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to return dataset ID: " +
					e.getMessage(),e);
		}
	}

	/**
	 * Create a new machine learning workflow.
	 *
	 * @param workflowID
	 *            Unique identifier for the new workflow
	 * @param parentWorkflowID
	 *            Unique identifier for the workflow from which the current
	 *            workflow is inherited from.
	 * @param projectID
	 *            Unique identifier for the project for which the workflow is
	 *            created
	 * @param datasetID
	 *            Unique identifier for the data-set associated with the
	 *            workflow
	 * @param workflowName
	 *            Name of the project
	 * @throws ProjectManagementServiceException
	 */
	public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID,
	                              String datasetID, String workflowName)
	                            		  throws
	                            		  ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.createNewWorkflow(workflowID, parentWorkflowID, projectID, datasetID,
			                            workflowName);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to create the workflow: " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to create the workflow: " +
					e.getMessage(),e);
		}
	}

	/**
	 * Delete an existing workflow.
	 *
	 * @param workflowID
	 *            Unique identifier of the workflow to be deleted
	 * @throws ProjectManagementServiceException
	 */
	public void deleteWorkflow(String workflowID) throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.deleteWorkflow(workflowID);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to delete the workflow: " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to delete the workflow: " +
					e.getMessage(),e);
		}
	}

	/**
	 * Get the array of workflows in a project.
	 *
	 * @param projectId
	 *            Unique identifier for the project for which the wokflows are
	 *            needed
	 * @return An array of workflow ID's and Names
	 * @throws ProjectManagementServiceException
	 */
	public String[][] getProjectWorkflows(String projectId)
			throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.getProjectWorkflows(projectId);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to get workflows of the project " + projectId + ": " +
					e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to get workflows of the project " +
					projectId + ": " + e.getMessage(),e);
		}
	}

	/**
	 * Set the default values for feature properties, of a given workflow.
	 *
	 * @param workflowID
	 *            Unique Identifier of the new workflow
	 * @param datasetID
	 *            Unique Identifier of the data-set associated with the workflow
	 * @throws DatasetServiceException
	 */
	public void setDefaultFeatureSettings(String datasetID, String workflowID)
			throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.setDefaultFeatureSettings(datasetID, workflowID);
		} catch (DatabaseHandlerException e) {
			logger.error("Failed to set default feature settings: " + e.getMessage(), e);
			throw new ProjectManagementServiceException("Failed to set default feature settings: " +
					e.getMessage(), e);
		}
	}
}
