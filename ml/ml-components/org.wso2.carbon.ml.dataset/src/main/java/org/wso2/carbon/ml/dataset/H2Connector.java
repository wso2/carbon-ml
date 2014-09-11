package org.wso2.carbon.ml.dataset;

import java.sql.Connection;
import java.sql.DriverManager;

/*
 * Singleton class to create a connection to H2 database
 */
public class H2Connector {

	private static H2Connector instance = null;
	
	public static H2Connector Initialize() {
		   if(instance == null) {
		      synchronized(H2Connector.class) { 
		    	  instance = new H2Connector();
		      }
		   }
		   return instance;
		}
	
	private H2Connector() {
		// Exists only to defeat instantiation.
	}

	public Connection createConnection() throws Exception{
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000", "wso2carbon", "wso2carbon");
		return conn;
	}
}