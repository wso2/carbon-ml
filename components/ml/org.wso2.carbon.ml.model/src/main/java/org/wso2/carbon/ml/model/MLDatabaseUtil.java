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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MLDatabaseUtil {
	private static final Log logger = LogFactory.getLog(MLDatabaseUtil.class);
	
	/*
	 * private Constructor to prevent any other class from instantiating.
	 */
	private MLDatabaseUtil() {
	  }

	/*
	 * Close a given set of Database resources
	 */
	protected static void closeAllConnections(Connection dbConnection,
			ResultSet rs, PreparedStatement prepStmt) {
		closeResultSet(rs);
		closeStatement(prepStmt);
		closeConnection(dbConnection);
	}

	/*
	 * Close a given connection
	 */
	protected static void closeConnection(Connection dbConnection) {
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				logger.error(
						"Database error. Could not close statement."
								+ e.getMessage(), e);
			}
		}
	}

	/*
	 * Close a given Resultset
	 */
	protected static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.error(
						"Database error. Could not close result set  - "
								+ e.getMessage(), e);
			}
		}
	}

	/*
	 * Close a given statement
	 */
	protected static void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.error(
						"Database error. Could not close statement. Continuing with others. - "
								+ e.getMessage(), e);
			}
		}
	}

	/*
	 * Roll-backs a connection
	 */
	protected static void rollBack(Connection dbConnection) {
		try {
			if (dbConnection != null) {
				dbConnection.rollback();
			}
		} catch (SQLException e1) {
			logger.error("An error occurred while rolling back transactions. ", e1);
		}
	}
	
	/*
	 * Enables the auto-commit of a connection
	 */
	protected static void enableAutoCommit(Connection dbConnection) {
		try {
			if (dbConnection != null) {
				dbConnection.setAutoCommit(true);
			}
		} catch (SQLException e1) {
			logger.error("An error occurred while enabling autocommit. ", e1);
		}
	}
}
