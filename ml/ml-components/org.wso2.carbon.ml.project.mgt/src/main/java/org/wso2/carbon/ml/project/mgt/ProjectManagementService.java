package org.wso2.carbon.ml.project.mgt;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProjectManagementService {
	private static final Log logger = LogFactory.getLog(ProjectManagementService.class);
	
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
	
	public void deleteProject(UUID projectId) throws ProjectManagementServiceException{
		try {
			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.deleteProject(projectId);
		} catch (DatabaseHandlerException e) {
			String msg = "Failed to update the data-source details in the database. " + e.getMessage();
			logger.error(msg, e);
			throw new ProjectManagementServiceException(msg);
		}
	}
}
