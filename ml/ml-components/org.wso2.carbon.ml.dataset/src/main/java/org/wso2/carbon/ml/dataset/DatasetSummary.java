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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class DatasetSummary {
	private final Log logger = LogFactory.getLog(DatasetSummary.class);
	private List<Integer> numericDataColPosstions = new ArrayList<Integer>();
	private List<Integer> stringDataColPosstions = new ArrayList<Integer>();
	private List<List<Double>> numericDataColumns = new ArrayList<List<Double>>();
	private List<List<String>> stringDataColumns = new ArrayList<List<String>>();
	private List<DescriptiveStatistics> descriptiveStats = new ArrayList<DescriptiveStatistics>();
	private List<Integer> missing = new ArrayList<Integer>();
	private List<Integer> unique = new ArrayList<Integer>();
	private List<Map<String, Integer>> graphFrequencies = new ArrayList<Map<String, Integer>>();
	private String[] header; // header names
	private FeatureType[] type; // feature type array

	/*
	 * get a summary of a sample from the given csv file, including
	 * descriptive-stats, missing points, unique values and etc. to display in
	 * the data view.
	 */
	public int generateSummary(String dataSourceId, int noOfRecords, int noOfIntervals,
	                           String seperator) throws DatasetServiceException {
		String msg;
		try {
			Configuration configuration = new Configuration();
			FileSystem fileSystem = FileSystem.get(configuration);
			DatabaseHandler dbHandler = new DatabaseHandler();

			// get the uri of the data source
			String dataSource = dbHandler.getDataSource(dataSourceId);
			if (dataSource != null) {
				logger.info("Data Source: " + dataSource);

				// read the input data file
				FSDataInputStream dataStream = fileSystem.open(new Path(dataSource));
				BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataStream));

				String firstLine;
				// if the header row is not empty
				if ((firstLine = dataReader.readLine()) != null) {
					header = firstLine.split(seperator);

					// Find the columns contains String data
					if (findColumnDataType(new BufferedReader(
					                                          new InputStreamReader(
					                                                                fileSystem.open(new Path(
					                                                                                         dataSource)))),
					                                                                                         seperator)) {
						// initialze the lists
						initilize();

						// Calculate mean,median, standard deviation,
						// skewness, missing values and unique values
						calculateDescriptiveStats(dataReader, noOfRecords, seperator);

						// Calculate frequencies of each category/interval of
						// the feature
						calculateFrequencies(noOfIntervals);

						// Update the database with calculated summary statistics
						dbHandler.updateSummaryStatistics(dataSourceId, header, type,
						                                  graphFrequencies, missing, unique,
						                                  descriptiveStats);
						return header.length;
					} else {
						msg = "Error occured while Calculating summary statistics.";
					}
				} else {
					msg = "Header row of the data source: " + dataSource + " is empty.";
				}
			} else {
				msg = "Data source not found.";
			}
		} catch (IOException e) {
			msg =
					"Error occured while reading from the data source with ID: " + dataSourceId +
					". " + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		} catch (Exception e) {
			msg = "Error occured while Calculating summary statistics." + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
		logger.error(msg);
		throw new DatasetServiceException(msg);
	}

	/*
	 * initialize the Lists and arrays
	 */
	private void initilize() {
		type = new FeatureType[header.length];
		for (int i = 0; i < header.length; i++) {
			descriptiveStats.add(new DescriptiveStatistics());
			graphFrequencies.add(new HashMap<String, Integer>());
			// if the current column is in the numerical data positions list
			if (numericDataColPosstions.contains(i)) {
				// set the data type to numerical
				type[i] = FeatureType.NUMERICAL;
				// add to the numerical data columns list
				numericDataColumns.add(new ArrayList<Double>());
				// if the current column is in the categorical data positions
				// list
			} else {
				// set the data type to categorical
				type[i] = FeatureType.CATEGORICAL;
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
	private void calculateDescriptiveStats(BufferedReader dataReader, int noOfRecords,
	                                       String seperator) throws DatasetServiceException {
		String line;
		String[] data = new String[header.length];
		double cellValue;
		Iterator<Integer> numericColumns;
		Iterator<Integer> stringColumns;
		int currentCol;

		// iterate through each row
		int row = 0;
		try {
			while ((line = dataReader.readLine()) != null && row != noOfRecords) {
				data = line.split(seperator);
				numericColumns = numericDataColPosstions.iterator();
				stringColumns = stringDataColPosstions.iterator();

				// iterate through each numeric column in a row
				while (numericColumns.hasNext()) {
					currentCol = numericColumns.next();
					// if the cell is not empty
					if (!data[currentCol].isEmpty()) {
						// convert the cell value to double
						cellValue = Double.parseDouble(data[currentCol]);

						// append the value of the cell to the descriptive-stats
						// of
						// the respective column
						descriptiveStats.get(currentCol).addValue(cellValue);

						// if the value is unique, update the unique value count
						// of
						// the column
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
				row++;
			}
			logger.info("Sample size: " + row);
		} catch (NumberFormatException e) {
			String msg =
					"Error occured while reading values from the data source." +
							e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		} catch (IOException e) {
			String msg = "Error occured while accessing the data source." + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * calculate the frequencies of each bin (i.e. each category/interval),
	 * needed to plot bar graphs/pie charts/histograms
	 */
	private void calculateFrequencies(int intervals) {
		Iterator<Integer> numericColumns = numericDataColPosstions.iterator();
		Iterator<Integer> stringColumns = stringDataColPosstions.iterator();
		int currentCol;

		/*
		 * Iterate through all Columns with String data
		 */
		while (stringColumns.hasNext()) {
			currentCol = stringColumns.next();
			Map<String, Integer> frequencies = new HashMap<String, Integer>();
			// count the frequencies in each category.
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
				type[currentCol] = FeatureType.CATEGORICAL;
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
	private void claculateCategoryFreqs(int currentCol) {
		Map<String, Integer> frequencies = new HashMap<String, Integer>();
		/*
		 * count the frequencies in each category. Iterate through all the rows
		 * in the column (number of rows can be different due to missing values)
		 */
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
	private void claculateIntervalFreqs(int currentCol, int intervals) {
		Map<String, Integer> frequencies = new HashMap<String, Integer>();

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
						numericDataColumns.get(numericDataColPosstions.indexOf(currentCol)).get(row) < lowerBound +
						intervalSize) {
					// increase the frequency of that interval by one
					frequencies.put(String.valueOf(interval),
					                frequencies.get(String.valueOf(interval)) + 1);
					break;
				}
				// set the lower bound to the lower bound of the next interval
				lowerBound = lowerBound + intervalSize;
			}
		}
		graphFrequencies.set(currentCol, frequencies);
	}

	/*
	 * find the columns with Categorical data and Numerical data
	 */
	private boolean findColumnDataType(BufferedReader dataReader, String seperator)
			throws DatasetServiceException {
		try {
			// ignore header row
			dataReader.readLine();
			String[] data;
			String line;
			while ((line = dataReader.readLine()) != null) {
				data = line.split(seperator);
				// if the row has no empty cells
				if (!Arrays.asList(data).contains("")) {
					// for each cell in the row
					for (int col = 0; col < data.length; col++) {
						// add the column number to the numericColPossitions, if
						// the cell contain numeric data
						if (isNumeric(data[col])) {
							numericDataColPosstions.add(col);
						} else {
							stringDataColPosstions.add(col);
						}
					}
					return true;
				}
			}
			logger.error("Data set does not contain any non-empty rows.");
			return false;
		} catch (IOException e) {
			String msg =
					"Error occured while identifying data types of columns in the data set: " +
							dataReader + "." + e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * check whether a given String represents a number or not.
	 */
	private boolean isNumeric(String inputData) {
		return inputData.matches("[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
	}
}
