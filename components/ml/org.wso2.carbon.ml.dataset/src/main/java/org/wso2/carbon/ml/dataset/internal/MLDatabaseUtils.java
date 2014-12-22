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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.dataset.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class contains utility methods for database resources.
 */
public class MLDatabaseUtils {
    private static final Log logger = LogFactory.getLog(MLDatabaseUtils.class);

    /*
     * private Constructor to prevent any other class from instantiating.
     */
    private MLDatabaseUtils() {
    }

    /**
     * Close a given set of database resources.
     *
     * @param connection            Connection to be closed
     * @param preparedStatement     PeparedStatement to be closed
     * @param resultSet             ResultSet to be closed
     */
    protected static void closeDatabaseResources(Connection connection,
            PreparedStatement preparedStatement, ResultSet resultSet) {
        // Close the resultSet
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("Could not close result set: " + e.getMessage(), e);
            }
        }
        // Close the connection
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Could not close connection: " + e.getMessage(), e);
            }
        }
        // Close the statement
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                logger.error("Could not close statement: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close a given set of database resources.
     *
     * @param connection            Connection to be closed
     * @param preparedStatement     PeparedStatement to be closed
     */
    protected static void closeDatabaseResources(Connection connection,
            PreparedStatement preparedStatement) {
        closeDatabaseResources(connection, preparedStatement, null);
    }

    /**
     * Close a given set of database resources.
     *
     * @param connection Connection to be closed
     */
    protected static void closeDatabaseResources(Connection connection) {
        closeDatabaseResources(connection, null, null);
    }

    /**
     * Close a given set of database resources.
     *
     * @param preparedStatement     PeparedStatement to be closed
     */
    protected static void closeDatabaseResources(PreparedStatement preparedStatement) {
        closeDatabaseResources(null, preparedStatement, null);
    }

    /**
     * Roll-backs a connection.
     *
     * @param dbConnection Connection to be rolled-back
     */
    protected static void rollBack(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e) {
            logger.error("An error occurred while rolling back transactions: ", e);
        }
    }

    /**
     * Enables the auto-commit of a connection.
     *
     * @param dbConnection  Connection of which the auto-commit should be enabled
     */
    protected static void enableAutoCommit(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                dbConnection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("An error occurred while enabling autocommit: ", e);
        }
    }
}
