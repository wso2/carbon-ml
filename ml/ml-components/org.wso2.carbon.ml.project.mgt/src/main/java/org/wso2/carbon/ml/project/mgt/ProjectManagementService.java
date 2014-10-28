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

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProjectManagementService {
	private static final Log logger = LogFactory.getLog(ProjectManagementService.class);
	
	/**
	 * Creates a new project
	 * 
	 * @param projectName
	 * @param description
	 * @return
	 * @throws ProjectManagementServiceException
	 */
	public UUID createProject(String projectName, String description) throws ProjectManagementServiceException {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.createProject(projectName, description);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to update the data-source details in the database. " + e.getMessage();
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
	public void deleteProject(String projectId) throws ProjectManagementServiceException{
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.deleteProject(projectId);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to update the data-source details in the database. " + e.getMessage();
			logger.error(msg, e);
			throw new ProjectManagementServiceException(msg);
		}
	}
	
	/**
	 * Assign a user to a given project
	 * 
	 * @param username
	 * @param projectId
	 * @throws ProjectManagementServiceException
	 */
	public void addUserToProject(String username,UUID projectId) throws ProjectManagementServiceException{
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.addUserToProject(username,projectId);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to update the data-source details in the database. " + e.getMessage();
			logger.error(msg, e);
			throw new ProjectManagementServiceException(msg);
		}
	}
	
	/**
	 * Get the project names and created dates, that a user is assigned to
	 * 
	 * @param username
	 * @return
	 * @throws ProjectManagementServiceException
	 */
	public String[][] getUserProjects(String username) throws ProjectManagementServiceException{
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			return dbHandler.getUserProjects(username);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to update the data-source details in the database. " + e.getMessage();
			logger.error(msg, e);
			throw new ProjectManagementServiceException(msg);
		}
	}
}
