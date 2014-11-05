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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    public UUID createProject(String projectName, String description) throws DatabaseHandlerException {
    	UUID projectId = UUID.randomUUID();
        PreparedStatement createProjectStatement = null;
        try {
        	connection.setAutoCommit(false);
        	createProjectStatement = connection.prepareStatement(SQLQueries.CREATE_PROJECT);
        	createProjectStatement.setObject(1, projectId);
        	createProjectStatement.setString(2, projectName);
        	createProjectStatement.setString(3, description);
        	createProjectStatement.execute();
			connection.commit();
			logger.debug("Successfully inserted details of project: " + projectName+
			             ". Project ID" + projectId.toString());
			return projectId;
        } catch (SQLException e) {
        	MLDatabaseUtil.rollBack(connection);
            String msg = "Error occured while inserting details of project: " + projectName + " to the database.\n" + e.getMessage();
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
        UUID projectUUID = UUID.fromString(projectId);
        try {
        	connection.setAutoCommit(false);
        	deleteProjectStatement = connection.prepareStatement(SQLQueries.DELETE_PROJECT);
        	deleteProjectStatement.setObject(1, projectUUID);
        	deleteProjectStatement.execute();
			connection.commit();
			logger.debug("Successfully deleted the project: " + projectId);
        } catch (SQLException e) {
        	MLDatabaseUtil.rollBack(connection);
            String msg = "Error occured while deleting the project: " + projectId + ".\n" + e.getMessage();
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
     * Assign a user to a given project
     * 
     * @param username
     * @param projectId
     * @throws DatabaseHandlerException
     */
    public void addUserToProject(String username,UUID projectId) throws DatabaseHandlerException{
    	PreparedStatement addUserStatement = null;
    	try {
        	connection.setAutoCommit(false);
        	addUserStatement = connection.prepareStatement(SQLQueries.ADD_USER_TO_PROJECT);
        	addUserStatement.setString(1, username);
        	addUserStatement.setObject(2, projectId);
        	addUserStatement.execute();
			connection.commit();
			logger.debug("Successfully added the user: " + username+ " to the project: "+projectId);
        } catch (SQLException e) {
        	MLDatabaseUtil.rollBack(connection);
            String msg = "Error occured while adding the user: " + username+ " to the project: "+projectId + ".\n" + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
        	// enable auto commit
        	MLDatabaseUtil.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtil.closeStatement(addUserStatement);
        }
    }
    
    /**
     * Get the project names and created dates, that a user is assigned to
     * 
     * @param username
     * @return
     * @throws DatabaseHandlerException
     */
    public String[][] getUserProjects(String username) throws DatabaseHandlerException{
    	PreparedStatement addUserStatement = null;
    	ResultSet result = null;
    	String [][]projects = null;
    	try {
        	addUserStatement = connection.prepareStatement(SQLQueries.GET_USER_PROJECTS,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        	addUserStatement.setString(1, username);
        	result=addUserStatement.executeQuery();
        	// create a 2-d string array having the size of the result set
        	result.last();
        	int size=result.getRow();
        	if(size>0){
            	projects=new String[3][size];
            	result.beforeFirst();
            	//put the result set to the string array
        		for(int i=0 ; i<size ; i++){
        			result.next();
        			projects[0][i]=result.getObject(1).toString();
        			projects[1][i]=result.getString(2);
        			projects[2][i]=result.getDate(3).toString();
            	}
        	}
        	return projects;
        } catch (SQLException e) {
        	MLDatabaseUtil.rollBack(connection);
            String msg = "Error occured while retrieving the projects of user: " + username + ".\n" + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(addUserStatement);
        }
    }
    
    /**
     *  Returns the ID of the dataset associated with the project
     *  
     * @param projectId
     * @return
     * @throws DatabaseHandlerException
     */
    public UUID getDatasetId(String projectId) throws DatabaseHandlerException{
    	PreparedStatement addUserStatement = null;
    	ResultSet result = null;
    	try {
        	addUserStatement = connection.prepareStatement(SQLQueries.GET_DATASET_ID);
        	addUserStatement.setObject(1, projectId);
        	result=addUserStatement.executeQuery();
        	result.first();
        	return UUID.fromString(result.getObject(1).toString());
        } catch (SQLException e) {
        	MLDatabaseUtil.rollBack(connection);
            String msg = "Error occured while retrieving the Dataset Id of project: " + projectId + ".\n" + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(addUserStatement);
        }
    }
}
