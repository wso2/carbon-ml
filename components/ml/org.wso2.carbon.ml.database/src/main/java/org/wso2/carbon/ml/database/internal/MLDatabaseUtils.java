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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.database.internal;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectWriter;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.commons.domain.config.HyperParameter;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;

import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains utility methods for database resources.
 */
public class MLDatabaseUtils {

    /*
     * private Constructor to prevent any other class from instantiating.
     */
    private MLDatabaseUtils() {
    }

    /**
     * Close a given set of database resources.
     *
     * @param connection Connection to be closed
     * @param preparedStatement PeparedStatement to be closed
     * @param resultSet ResultSet to be closed
     */
    public static void closeDatabaseResources(Connection connection, PreparedStatement preparedStatement,
            ResultSet resultSet) throws DatabaseHandlerException {
        // Close the resultSet
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new DatabaseHandlerException("Could not close result set: " + e.getMessage(), e);
            }
        }
        // Close the connection
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DatabaseHandlerException("Database error. Could not close statement: " + e.getMessage(), e);
            }
        }
        // Close the statement
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                throw new DatabaseHandlerException("Database error. Could not close statement: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close a given set of database resources.
     *
     * @param connection Connection to be closed
     * @param preparedStatement PeparedStatement to be closed
     */
    public static void closeDatabaseResources(Connection connection, PreparedStatement preparedStatement)
            throws DatabaseHandlerException {
        closeDatabaseResources(connection, preparedStatement, null);
    }

    /**
     * Close a given set of database resources.
     *
     * @param connection Connection to be closed
     */
    public static void closeDatabaseResources(Connection connection) throws DatabaseHandlerException {
        closeDatabaseResources(connection, null, null);
    }

    /**
     * Close a given set of database resources.
     *
     * @param preparedStatement PeparedStatement to be closed
     */
    public static void closeDatabaseResources(PreparedStatement preparedStatement) throws DatabaseHandlerException {
        closeDatabaseResources(null, preparedStatement, null);
    }

    /**
     * Roll-backs a connection.
     *
     * @param dbConnection Connection to be rolled-back
     */
    public static void rollBack(Connection dbConnection) throws DatabaseHandlerException {
        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while rolling back transactions: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Enables the auto-commit of a connection.
     *
     * @param dbConnection Connection of which the auto-commit should be enabled
     */
    public static void enableAutoCommit(Connection dbConnection) throws DatabaseHandlerException {
        try {
            if (dbConnection != null) {
                dbConnection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while enabling autocommit: " + e.getMessage(), e);
        }
    }

    public static Map<String, String> getHyperParamsAsAMap(List<HyperParameter> hyperParams) {
        Map<String, String> map = new HashMap<String, String>();
        for (HyperParameter hyperParameter : hyperParams) {
            map.put(hyperParameter.getParameter(), hyperParameter.getValue());
        }
        return map;
    }

    public static String toString(Clob clob) throws DatabaseHandlerException {
        Reader in;
        try {
            in = clob.getCharacterStream();
            StringWriter w = new StringWriter();
            IOUtils.copy(in, w);
            String clobAsString = w.toString();
            return clobAsString;
        } catch (Exception e) {
            throw new DatabaseHandlerException("Failed to convert clob to string");
        }
    }
    
}
