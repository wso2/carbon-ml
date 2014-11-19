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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

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
     * @return DatabaseHandler instance
     * @throws DatabaseHandlerException
     */
    protected static DatabaseHandler getDatabaseHandler() throws DatabaseHandlerException {
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
}
