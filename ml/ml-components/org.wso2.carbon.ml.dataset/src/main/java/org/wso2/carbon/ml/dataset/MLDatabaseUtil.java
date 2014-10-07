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
package org.wso2.carbon.ml.dataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MLDatabaseUtil {
	private static final Log log = LogFactory.getLog(MLDatabaseUtil.class);

	/**
	 * 
	 * @param dbConnection
	 * @param rs
	 * @param prepStmt
	 */
	public static void closeAllConnections(Connection dbConnection,
			ResultSet rs, PreparedStatement prepStmt) {
		
		closeResultSet(rs);
		closeStatement(prepStmt);
		closeConnection(dbConnection);
	}

	/**
	 * 
	 * @param dbConnection
	 */
	public static void closeConnection(Connection dbConnection) {
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				log.error(
						"Database error. Could not close statement."
								+ e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error(
						"Database error. Could not close result set  - "
								+ e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @param statement
	 */
	public static void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.error(
						"Database error. Could not close statement. Continuing with others. - "
								+ e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @param dbConnection
	 */
	public static void rollBack(Connection dbConnection) {
		try {
			if (dbConnection != null) {
				dbConnection.rollback();
			}
		} catch (SQLException e1) {
			log.error("An error occurred while rolling back transactions. ", e1);
		}
	}
	
	/**
	 * 
	 * @param dbConnection
	 */
	public static void enableAutoCommit(Connection dbConnection) {
		try {
			if (dbConnection != null) {
				dbConnection.setAutoCommit(true);
			}
		} catch (SQLException e1) {
			log.error("An error occurred while setting autocommit to true. ", e1);
		}
	}
}
