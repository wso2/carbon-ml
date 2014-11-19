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
/**
 * @scr.component name="projectManagementService" immediate="true"
 */
public class ProjectManagementService {
    private static final Log logger = LogFactory.getLog(ProjectManagementService.class);

    protected void activate(ComponentContext context) {
        try {
            ProjectManagementService projectManagementService = new ProjectManagementService();
            context.getBundleContext().registerService(ProjectManagementService.class.getName(),
                                                       projectManagementService, null);
            logger.info("ML Project Management Service Started");
            logger.info("http://localhost:9763/mlUI/");
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        logger.info("ML Project Management Service Stopped");
    }
    /**
     * Creates a new project
     *
     * @param projectName
     * @param description
     * @return
     * @throws ProjectManagementServiceException
     */
    public void createProject(String projectID, String projectName, String description) throws
                                                                                      ProjectManagementServiceException {
        try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            dbHandler.createProject(projectID, projectName, description);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to create the project. " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }

    /**
     * Delete details of a given project
     *
     * @param projectId
     * @throws ProjectManagementServiceException
     */
    public void deleteProject(String projectId) throws ProjectManagementServiceException {
        try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            dbHandler.deleteProject(projectId);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to delete the project. " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }

    /**
     * Assign a tenant to a given project
     *
     * @param tenantID
     * @param projectId
     * @throws ProjectManagementServiceException
     */
    public void addTenantToProject(String tenantID, String projectId)
            throws ProjectManagementServiceException {
        try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            dbHandler.addTenantToProject(tenantID, projectId);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to add the tenant to project. " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }

    /**
     * Get the project names and created dates, that a tenant is assigned to
     *
     * @param tenantID
     * @return
     * @throws ProjectManagementServiceException
     */
    public String[][] getTenantProjects(String tenantID) throws ProjectManagementServiceException {
        try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            return dbHandler.getTenantProjects(tenantID);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to get tenant projects. " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }

    /**
     * Returns the ID of the dataset associated with the project
     *
     * @param projectId
     * @return
     * @throws ProjectManagementServiceException
     */
    public String getdatasetID(String projectId) throws ProjectManagementServiceException {
        try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            return dbHandler.getdatasetID(projectId);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to return dataset ID. " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }

    /**
     * Create a new machine learning workflow
     *
     * @param workflowID
     * @param projectID
     * @param workflowName
     * @throws ProjectManagementServiceException
     */
    public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID,
                                  String datasetID,String workflowName) throws
                                                                        ProjectManagementServiceException {
        try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            dbHandler.createNewWorkflow(workflowID, parentWorkflowID, projectID, datasetID,
                                        workflowName);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to create the workflow. " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }
    
    /**
     * Delete an existing workflow
     * 
     * @param workflowID
     * @throws ProjectManagementServiceException
     */
    public void deleteWorkflow(String workflowID) throws ProjectManagementServiceException {
        try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            dbHandler.deleteWorkflow(workflowID);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to delete the workflow. " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }
    
    /**
     * Get the array of workflows in a project
     * 
     * @param projectId
     * @return
     * @throws ProjectManagementServiceException
     */
    public String[][] getProjectWorkflows(String projectId) throws ProjectManagementServiceException{
    	try {
            DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
            return dbHandler.getProjectWorkflows(projectId);
        } catch (DatabaseHandlerException e) {
            String msg = "Failed to get workflows of the project "+ projectId +". " + e.getMessage();
            logger.error(msg, e);
            throw new ProjectManagementServiceException(msg);
        }
    }
}
