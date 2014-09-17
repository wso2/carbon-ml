package org.wso2.carbon.ml.dataset;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.carbon.ml.db.H2Connector;

public class DatabaseHandler {
	H2Connector h2Connector;
	Connection connection = null;
	private static final Log LOGGER = LogFactory.getLog(DatabaseHandler.class);
	
	DatabaseHandler() throws DatabaseHandlerException{
			try {
	            h2Connector = H2Connector.initialize();
	            connection = h2Connector.getConnection();
            } catch (Exception e) {
            	String msg = "Error occured while connecting to database. " + e.getMessage();
    			LOGGER.error(msg,e);
    			throw new DatabaseHandlerException(msg);
            }
	}
	
	public Connection getConnection(){
		return this.connection;
	}
	
	/*
	 * Update the database with all the summary stats of the sample
	 */
	public void updateSummaryStatistics(int dataSourceId, String [] header,String [] type, List <Map<String,Integer>> graphFrequencies,List <Integer> missing,List <Integer> unique, List <DescriptiveStatistics> descriptiveStats) throws Exception {
		try {
			String summaryStat;
			for (int column = 0; column < header.length; column++) {
				// get the json representation of the column
				summaryStat = createJson(column, graphFrequencies, missing, unique, descriptiveStats);
				// put the values to the database table. If the feature already
				// exists, updates the row. if not, inserts as a new row.
				connection.createStatement()
				.execute("MERGE INTO ML_FEATURE(NAME,DATASET,TYPE,SUMMARY,IMPUTE_METHOD,IMPORTANT) VALUES('" +
						header[column] +
						"'," + dataSourceId +
						",'" + type[column] +
						"','" + summaryStat + 
						"','"+new ImputeOption().getMethod()+
						"','TRUE')");
			}
		} catch (SQLException e) {
			String msg="Error occured while updating the database with summary statistics of the data source: "+dataSourceId+"."+e.getMessage();
			LOGGER.error(msg,e);
			throw new DatabaseHandlerException(msg);
        } finally{
			if(connection!=null){
				connection.close();
			}
		}
	}


	/*
	 * Create the json string with summary stat for a given column
	 */
	private String createJson(int column,List <Map<String,Integer>> graphFrequencies,List <Integer> missing,List <Integer> unique, List <DescriptiveStatistics> descriptiveStats) {
		String json = "{";
		String freqs = "[";
		Object[] categoryNames = graphFrequencies.get(column).keySet().toArray();
		for (int i = 0; i < graphFrequencies.get(column).size(); i++) {
			freqs =
					freqs + ",{range:" + categoryNames[i].toString() + ",frequency:" +
							graphFrequencies.get(column).get(categoryNames[i].toString()) + "}";
		}
		freqs = freqs.replaceFirst(",", "") + "]";
		json=json+",unique:"+unique.get(column)+
				",missing:"+missing.get(column)+
				",mean:"+descriptiveStats.get(column).getMean()+
				",median:"+descriptiveStats.get(column).getPercentile(50)+
				",std:"+descriptiveStats.get(column).getStandardDeviation()+
				",skewness:"+descriptiveStats.get(column).getSkewness()+
				",frequencies:"+freqs+
				"}";
		return json;
	}
	
	/*
	 * get the URI of the data source having the given ID, from the database
	 */
	public String getDataSource(int dataSourceId) throws Exception {
		Connection connection = null;
		try {
			H2Connector h2Connector = H2Connector.initialize();
			connection = h2Connector.getConnection();
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT URI FROM ML_DATASET WHERE ID=" +
							dataSourceId);
			if (result.first()) {
				return result.getNString("URI");
			}
			else {
				LOGGER.error("Invalid data source ID.");
			}
		} catch (Exception e) {
			String msg="Error occured while reading the Data source from the database."+e.getMessage();
			LOGGER.error(msg,e);
			throw new DatabaseHandlerException(msg);
		} finally {
			if(connection!=null){
				connection.close();
			}
		}
		return null;
	}
	
	/*
	 * get the default uploading location
	 */
	public String getDefaultUploadLocation() throws DatabaseHandlerException{

		try {
	        ResultSet result = connection.createStatement().executeQuery("SELECT DATASET_UPLOADING_DIR FROM ML_CONFIGURATION");
	        if (result.first()) {
				return result.getNString("DATASET_UPLOADING_DIR");
	        }else {
				LOGGER.error("Default uploading location is not set in the ML_CONFIGURATION database table.");
			}
        } catch (SQLException e) {
        	String msg =
					"Error occured while retrieving the data-source details from the database. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatabaseHandlerException(msg);
        }
		return null;
	}
	
	
	/*
	 *  update details for a given feature
	 */
	public boolean updateFeature(String name, int dataSet, String type, ImputeOption imputeOption, boolean important) throws DatabaseHandlerException{
		try {
	        return connection.createStatement().execute("UPDATE  ML_FEATURE SET TYPE ='" + type +
	           			                                            "',IMPUTE_METHOD='" + imputeOption.getMethod() +
	           			                                            "', IMPORTANT=" + important +
	           			                                            " WHERE name='" + name + "' AND Dataset="+dataSet+";");
        } catch (SQLException e) {
        	String msg = "Error occured while updating the feature : "+name+" of data set: "+dataSet+" ." + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatabaseHandlerException(msg);
        }
	}
}
