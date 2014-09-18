package org.wso2.carbon.ml.dataset;

import java.sql.Connection;
import java.sql.DriverManager;


/*
 * Singleton class to create a connection to H2 database
 */
public class H2Connector {

	private static volatile H2Connector instance = null;
	private static Connection conn;

	public static H2Connector initialize() throws Exception {
		try {
			if (instance == null) {
				synchronized (H2Connector.class) {
					instance = new H2Connector();
					Class.forName("org.h2.Driver");
					conn = DriverManager
							.getConnection(
									"jdbc:h2:repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000",
									"wso2carbon", "wso2carbon");
				}
			}
			return instance;
		} catch (Exception ex) {
			System.out
					.println("Error in DB initialization: " + ex.getMessage());
			throw ex;
		}
	}

	private H2Connector() {
		// Exists only to defeat instantiation.
	}

	public Connection getConnection() throws Exception {
		return conn;
	}
}