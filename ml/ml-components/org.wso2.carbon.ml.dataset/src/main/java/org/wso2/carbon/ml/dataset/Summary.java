package org.wso2.carbon.ml.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class Summary {
	private static final Log logger = LogFactory.getLog(Summary.class);
	private List <Integer> numericDataColPosstions = new ArrayList <Integer>();	// column numbers of the columns contain numerical values
	private List <Integer> stringDataColPosstions = new ArrayList <Integer>();	// column numbers of the columns contain string values
	private List <List <Double>> numericDataColumns = new ArrayList <List<Double>>();	//Holds actual data column-wise
	private List <List <String>> stringDataColumns = new ArrayList <List<String>>();
	private List <DescriptiveStatistics> descriptiveStats = new ArrayList <DescriptiveStatistics>();	//holds descriptive statistics of each column
	private List <Integer> missing = new ArrayList <Integer>();	//numbers of missing values in each column
	private List <Integer> unique = new ArrayList <Integer>();	//numbers of unique values in each column
	private List <HashMap<String,Integer>> graphFrequencies = new ArrayList <HashMap<String,Integer>>();		//frequencies of each interval/category of every column
	private String [] header;	//header names
	private String [] type;	//data type
	private enum dataTypes {CATEGORICAL, NUMERICAL};

	/*
	 * get a summary of a sample from the given csv file, including
	 * descriptive-stats, missing points, unique values and etc. to display in
	 * the data view.
	 */
	public void calculateSummary(String dataSource, int dataSourceId, int noOfRecords,
	                             int noOfIntervals, String seperator) throws Exception {
		Configuration configuration = new Configuration();
		FileSystem fileSystem = FileSystem.get(configuration);
		configuration.addResource(new Path(fileSystem.getWorkingDirectory() +
				"/repository/conf/advanced/hive-site.xml"));
		fileSystem.setConf(configuration);
		logger.info("Data Source: " + dataSource);
		logger.info("Sample size: " + noOfRecords);

		// read the csv file
		FSDataInputStream dataStream = fileSystem.open(new Path(dataSource));
		BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataStream));

		// read headers
		header = dataReader.readLine().split(seperator);

		// find the columns contains String data
		findColumnDataType(new BufferedReader(
		                                      new InputStreamReader(
		                                                            fileSystem.open(new Path(
		                                                                                     dataSource)))),
		                                                                                     seperator);
		initilize();
		findDescriptives(dataReader, noOfRecords, seperator);
		calculateFrequencies(noOfRecords, noOfIntervals);
		updateDatabase(dataSourceId);
	}

	
	/*
	 * initialize the Lists and arrays
	 */
	private void initilize() {
		type = new String[header.length];
		for (int i = 0; i < header.length; i++) {
			descriptiveStats.add(new DescriptiveStatistics());
			graphFrequencies.add(new HashMap<String, Integer>());
			// if the current column is in the numerical data positions list
			if (numericDataColPosstions.contains(i)) {
				// set the data type to numerical
				type[i] = dataTypes.NUMERICAL.toString();
				// add to the numerical data columns list
				numericDataColumns.add(new ArrayList<Double>());
			// if the current column is in the categorical data	
			} else { 
				// positions list
				// set the data type to categorical
				type[i] = dataTypes.CATEGORICAL.toString();
				// add to the categorical data columns list
				stringDataColumns.add(new ArrayList<String>());
			}
			missing.add(0);
			unique.add(0);
		}
	}
	

	/*
	 * Read from the csv file and find descriptive stats, missing values and
	 * unique vales
	 */
	private void findDescriptives(BufferedReader dataReader, int noOfRecords, String seperator)
			throws IOException {
		String[] data = new String[header.length];
		double cellValue;
		Iterator<Integer> numericColumns;
		Iterator<Integer> stringColumns;
		int currentCol;

		// iterate through each row
		for (int row = 0; row < noOfRecords; row++) {
			data = dataReader.readLine().split(seperator);

			numericColumns = numericDataColPosstions.iterator();
			stringColumns = stringDataColPosstions.iterator();

			// iterate through each numeric column in a row
			while (numericColumns.hasNext()) {
				currentCol = numericColumns.next();
				// if the cell is not empty
				if (!data[currentCol].isEmpty()) {
					// convert the cell value to double
					cellValue = Double.parseDouble(data[currentCol]);

					// append the value of the cell to the descriptive-stats of
					// the respective column
					descriptiveStats.get(currentCol).addValue(cellValue);

					// if the value is unqie, update the unique value count of the column
					if (!numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
							.contains(cellValue)) {
						unique.set(currentCol, unique.get(currentCol).intValue() + 1);
					}

					// append the cell value to the respective column
					numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
					.add(cellValue);
				} else {
					missing.set(currentCol, missing.get(currentCol).intValue() + 1);
				}
			}

			// iterate through each string column in a row
			while (stringColumns.hasNext()) {
				currentCol = stringColumns.next();
				// if the cell is not empty
				if (currentCol < data.length && !data[currentCol].isEmpty()) {
					// update the unique value count of the column
					if (!stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
							.contains(currentCol)) {
						unique.set(currentCol, unique.get(currentCol).intValue() + 1);
					}

					// append the cell value to the respective column
					stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
					.add(data[currentCol]);
				} else {
					missing.set(currentCol, missing.get(currentCol).intValue() + 1);
				}
			}
		}
	}

	
	/*
	 * calculate the frequencies of each bin (i.e. each category/interval),
	 * needed to plot bar graphs/pie charts/histograms
	 */
	private void calculateFrequencies(int noOfRecords, int intervals) {
		Iterator<Integer> numericColumns = numericDataColPosstions.iterator();
		Iterator<Integer> stringColumns = stringDataColPosstions.iterator();
		int currentCol;

		/*
		 * Iterate through all Columns with String data
		 */
		while (stringColumns.hasNext()) {
			currentCol = stringColumns.next();
			HashMap<String, Integer> frequencies = new HashMap<String, Integer>();
			// count the frequencies in each category
			// Iterate through all the rows in the column (number of rows can be
			// different due to missing values)
			for (int row = 0; row < stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
					.size(); row++) {
				// if the category has appeared before, increment the frequency
				if (frequencies.containsKey(stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
				                            .get(row))) {
					frequencies.put(stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
					                .get(row),
					                frequencies.get(stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
					                                .get(row)) + 1);
				} else {
					// if the category appeared for the first time, set the
					// frequency to one
					frequencies.put(stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
					                .get(row), 1);
				}
			}
			graphFrequencies.set(currentCol, frequencies);
		}

		/*
		 * Iterate through all Columns with Numerical data
		 */
		while (numericColumns.hasNext()) {
			currentCol = numericColumns.next();

			// if the column has categorical data (i.e. unique values are less
			// than or equal to twenty)
			if (unique.get(currentCol).intValue() <= 20) {
				// change the data type to categorical
				type[currentCol] = dataTypes.CATEGORICAL.toString();
				claculateCategoryFreqs(currentCol);
				
			} else {
				// if the column has Quantitative data (i.e. unique values are
				// more than twenty)
				claculateIntervalFreqs(currentCol, intervals);
			}
		}
	}

	/*
	 * Calculate the frequencies of each category of a column
	 */
	public void claculateCategoryFreqs(int currentCol){
		HashMap<String, Integer> frequencies = new HashMap<String, Integer>();
		// count the frequencies in each category
		// Iterate through all the rows in the column (number of rows
		// can be different due to missing values)
		for (int row = 0; row < numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
				.size(); row++) {
			// if the category has appeared before, increment the
			// frequency
			if (frequencies.containsKey(String.valueOf(numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
			                                           .get(row)))) {
				frequencies.put(String.valueOf(numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
				                               .get(row)),
				                               frequencies.get(String.valueOf(numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
				                                                              .get(row))) + 1);
			} else {
				// if the category appeared for the first time, set the
				// frequency to one
				frequencies.put(String.valueOf(numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
				                               .get(row)), 1);
			}
		}
		graphFrequencies.set(currentCol, frequencies);
	}
	
	/*
	 * Calculate the frequencies of each interval of a column
	 */
	public void claculateIntervalFreqs(int currentCol, int intervals){
		HashMap<String, Integer> frequencies = new HashMap<String, Integer>();

		// initialize the frequencies of all the intervals to zero. Each
		// interval is identified by the interval number
		for (int i = 0; i < intervals; i++) {
			frequencies.put(String.valueOf(i), 0);
		}

		// define the size of an interval
		double intervalSize =
				(descriptiveStats.get(currentCol).getMax() - descriptiveStats.get(currentCol)
						.getMin()) /
						intervals;
		double lowerBound;

		// Iterate through all the rows in the column (number of rows
		// can be different due to missing values)
		for (int row = 0; row < numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
				.size(); row++) {
			// set the initial lower bound to the data-minimum
			lowerBound = descriptiveStats.get(currentCol).getMin();
			// check to which interval does the data point belongs
			for (int interval = 0; interval < intervals; interval++) {
				// if found
				if (lowerBound <= numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
						.get(row) &&
						numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
						.get(row) < lowerBound + intervalSize) {
					// increase the frequency of that interval by one
					frequencies.put(String.valueOf(interval),
					                frequencies.get(String.valueOf(interval)) + 1);
					break;
				}
				// set the lower bound to the lower bound of the next
				// interval
				lowerBound = lowerBound + intervalSize;
			}
		}
		graphFrequencies.set(currentCol, frequencies);

	}
	
	
	/*
	 * find the columns with Categorical data and Numerical data
	 */
	private void findColumnDataType(BufferedReader dataReader, String seperator) throws IOException {
		dataReader.readLine();
		String[] data;
		while (true) {
			data = dataReader.readLine().split(seperator);
			// finds the first row with no empty cells
			if (!Arrays.asList(data).contains("")) {
				// for each cell in the row
				for (int col = 0; col < data.length; col++) {
					// add the column number to the numericColPossitions, if the
					// cell contain numeric data
					if (isNumeric(data[col])) {
						numericDataColPosstions.add(col);
					} else {
						stringDataColPosstions.add(col);
					}
				}
				break;
			}
		}
	}

	
	/*
	 * check whether a given String represents a number or not.
	 */
	public boolean isNumeric(String inputData) {
		return inputData.matches("[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
	}

	
	/*
	 * Update the database with all the summary stats of the sample
	 */
	public void updateDatabase(int dataSourceId) throws Exception {
		H2Connector h2Connector = H2Connector.initialize();
		Connection connection = null;
        try {
	        connection = h2Connector.getConnection();
			String summaryStat;
			for (int column = 0; column < header.length; column++) {
				// get the json representation of the column
				summaryStat = createJson(column);
				// put the values to the database table. If the feature already
				// exists, updates the row. if not, inserts as a new row.
				connection.createStatement()
				.execute("MERGE INTO ML_FEATURE(NAME,DATASET,TYPE,SUMMARY,IMPUTE_METHOD,IMPORTANT) VALUES('" +
						header[column] +
						"'," +
						dataSourceId +
						",'" +
						type[column] +
						"','" + summaryStat + "','IGNORE','TRUE')");
			}
        } catch (Exception e) {
        	logger.error("Could not connect to the database.");
        	throw e;
        } finally{
        	connection.close();
        }
	}

	
	/*
	 * Create the json string with summary stat for a given column
	 */
	private String createJson(int column) {
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
}
