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
                        DataSource ds = (DataSource) initContext.lookup("jdbc/WSO2CarbonDB");
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
			             ". Project ID" + projectId);
			return projectId;
        } catch (SQLException e) {
        	MLDatabaseUtil.rollBack(connection);
            String msg = "Error occured while inserting details of project: " + projectName + " to the database.\n" + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(createProjectStatement);
        }
    }
    
    
    public void deleteProject(UUID projectId) throws DatabaseHandlerException {
        PreparedStatement deleteProjectStatement = null;
        try {
        	connection.setAutoCommit(false);
        	deleteProjectStatement = connection.prepareStatement(SQLQueries.DELETE_PROJECT);
        	deleteProjectStatement.setObject(1, projectId);
        	deleteProjectStatement.execute();
			connection.commit();
			logger.debug("Successfully deleted the project: " + projectId);
        } catch (SQLException e) {
        	MLDatabaseUtil.rollBack(connection);
            String msg = "Error occured while deleting the project: " + projectId + ".\n" + e.getMessage();
            logger.error(msg, e);
            throw new DatabaseHandlerException(msg);
        } finally {
            // close the database resources
            MLDatabaseUtil.closeStatement(deleteProjectStatement);
        }
    }
}
