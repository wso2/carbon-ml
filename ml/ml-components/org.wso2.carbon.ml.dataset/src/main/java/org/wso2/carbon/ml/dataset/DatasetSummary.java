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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class DatasetSummary {
	private static final Log logger = LogFactory.getLog(DatasetSummary.class);
	private List<Integer> numericDataColPosstions = new ArrayList<Integer>();
	private List<Integer> stringDataColPosstions = new ArrayList<Integer>();
	private List<List<Double>> numericDataColumns = new ArrayList<List<Double>>();
	private List<List<String>> stringDataColumns = new ArrayList<List<String>>();
	private List<DescriptiveStatistics> descriptiveStats = new ArrayList<DescriptiveStatistics>();
	private List<Map<String, Integer>> graphFrequencies = new ArrayList<Map<String, Integer>>();
	private EmpiricalDistribution[] histograms;
	private int[] missing;
	private int[] unique;
	private String[] header;
	private FeatureType[] type;
	private static final int CATEGORY_THRESHOLD = 20;

	/**
	 * get a summary of a sample from the given csv file, including
	 * descriptive-stats, missing points, unique values and etc. to display in
	 * the data view.
	 *
	 * @param dataSourceId
	 * @param noOfRecords
	 * @param noOfIntervals
	 * @param seperator
	 * @return
	 * @throws DatasetServiceException
	 */
	public int generateSummary(String dataSourceId, int noOfRecords, int noOfIntervals,
	                           String seperator) throws DatasetServiceException {
		String msg;
		try {
			DatabaseHandler dbHandler = new DatabaseHandler();
			// get the uri of the data source
			String dataSource = dbHandler.getDataSource(dataSourceId);
			if (dataSource != null) {
				logger.debug("Data Source: " + dataSource);
				// read the input data file
				InputStream dataStream = new FileInputStream(new File(dataSource));
				BufferedReader dataReader =
						new BufferedReader(
						                   new InputStreamReader(
						                                         dataStream,
						                                         Charset.defaultCharset()));
				String firstLine;
				// if the header row is not empty
				if ((firstLine = dataReader.readLine()) != null) {
					header = firstLine.split(seperator);

					// Find the columns contains String data. If successful:
					if (findColumnDataType(dataSource, seperator)) {
						// Initialize the lists
						initilize();
						// Calculate descriptive statistics
						calculateDescriptiveStats(dataReader, noOfRecords, seperator);
						// Calculate frequencies of each bin of the feature
						calculateFrequencies(noOfIntervals);
						// Update the database with calculated summary stats
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
				dataReader.close();
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
		int noOfFeatures = header.length;
		missing = new int[noOfFeatures];
		unique = new int[noOfFeatures];
		type = new FeatureType[noOfFeatures];
		histograms = new EmpiricalDistribution[noOfFeatures];
		for (int i = 0; i < noOfFeatures; i++) {
			descriptiveStats.add(new DescriptiveStatistics());
			graphFrequencies.add(new HashMap<String, Integer>());

			// if the current column is in the numerical data positions list
			if (numericDataColPosstions.contains(i)) {
				// set the data type to numerical
				type[i] = FeatureType.NUMERICAL;
				// add to the numerical data columns list
				numericDataColumns.add(new ArrayList<Double>());
			} else {
				// if the current column is in the categorical data positions
				// list, set the data type to categorical
				type[i] = FeatureType.CATEGORICAL;
				// add to the categorical data columns list
				stringDataColumns.add(new ArrayList<String>());
			}
		}
	}

	/*
	 * Read from the csv file and find descriptive stats, and
	 * unique vales
	 */
	private void calculateDescriptiveStats(BufferedReader dataReader, int noOfRecords,
	                                       String seperator) throws DatasetServiceException {
		String line;
		String[] data;
		double cellValue;
		Iterator<Integer> numericColumns;
		Iterator<Integer> stringColumns;
		int currentCol;

		// iterate through each row
		int row = 0;
		try {
			while ((line = dataReader.readLine()) != null && row != noOfRecords) {
				data = line.split(seperator,header.length);
				// create two iterators for two types of data columns
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
						descriptiveStats.get(currentCol).addValue(cellValue);
						// append the cell value to the respective column
						numericDataColumns.get(numericDataColPosstions.indexOf(currentCol))
						.add(cellValue);
					} else {
						missing[currentCol]++;
					}
				}

				// iterate through each string column in a row
				while (stringColumns.hasNext()) {
					currentCol = stringColumns.next();
					// if the cell is not empty
					if (currentCol < data.length && !data[currentCol].isEmpty()) {
						// append the cell value to the respective column
						stringDataColumns.get(stringDataColPosstions.indexOf(currentCol))
						.add(data[currentCol]);
					} else {
						missing[currentCol]++;
					}
				}
				row++;
			}
			logger.debug("Sample size: " + row);
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
	 * Calculate the frequencies of each bin (i.e. each category/interval),
	 * needed to plot bar graphs/histograms.
	 * Calculate missing value counts.
	 */
	private void calculateFrequencies(int intervals) {
		Iterator<Integer> numericColumns = numericDataColPosstions.iterator();
		Iterator<Integer> stringColumns = stringDataColPosstions.iterator();
		int currentCol;

		//Iterate through all Columns with String data
		while (stringColumns.hasNext()) {
			currentCol = stringColumns.next();
			Map<String, Integer> frequencies = new HashMap<String, Integer>();
			// count the frequencies in each category.
			Set<String> uniqueSet =
					new HashSet<String>(
							stringDataColumns.get(stringDataColPosstions.indexOf(currentCol)));
			unique[currentCol] = uniqueSet.size();
			for (String uniqueValue : uniqueSet) {
				frequencies.put(uniqueValue.toString(),
				                Collections.frequency(stringDataColumns.get(stringDataColPosstions.indexOf(currentCol)),
				                                      uniqueValue));
			}
			graphFrequencies.set(currentCol, frequencies);
		}

		//Iterate through all Columns with Numerical data
		while (numericColumns.hasNext()) {
			currentCol = numericColumns.next();
			Set<Double> uniqueSet =
					new HashSet<Double>(
							numericDataColumns.get(numericDataColPosstions.indexOf(currentCol)));
			// if the unique values are less than or equal to maximum-category-limit
			unique[currentCol] = uniqueSet.size();
			if (unique[currentCol] <= CATEGORY_THRESHOLD) {
				// change the data type to categorical
				type[currentCol] = FeatureType.CATEGORICAL;
				//calculate the category frequencies
				claculateCategoryFreqs(currentCol, uniqueSet);
			} else {
				// if the  unique values are more than twenty, calculate interval frequencies
				claculateIntervalFreqs(currentCol, intervals);
			}
		}
	}

	/*
	 * Calculate the frequencies of each category of a column
	 */
	private void claculateCategoryFreqs(int currentCol, Set<Double> uniqueSet) {
		Map<String, Integer> frequencies = new HashMap<String, Integer>();
		for (Double uniqueValue : uniqueSet) {
			frequencies.put(uniqueValue.toString(),
			                Collections.frequency(numericDataColumns.get(numericDataColPosstions.indexOf(currentCol)),
			                                      uniqueValue));
		}
		graphFrequencies.set(currentCol, frequencies);
	}

	/*
	 * Calculate the frequencies of each interval of a column
	 */
	private void claculateIntervalFreqs(int currentCol, int intervals) {
		Map<String, Integer> frequencies = new HashMap<String, Integer>();
		int columnIndex = numericDataColPosstions.indexOf(currentCol);
		double[] columnData =
				ArrayUtils.toPrimitive(numericDataColumns.get(columnIndex)
				                       .toArray(new Double[numericDataColumns.get(columnIndex)
				                                           .size()]));
		histograms[currentCol] = new EmpiricalDistribution(intervals);
		histograms[currentCol].load(columnData);
		int bin = 0;
		for (SummaryStatistics stats : histograms[currentCol].getBinStats()) {
			frequencies.put(String.valueOf(bin++), (int) stats.getN());
		}
		graphFrequencies.set(currentCol, frequencies);
	}

	/*
	 * find the columns with Categorical data and Numerical data
	 */
	private boolean findColumnDataType(String dataSource, String seperator) throws Exception {
		InputStream dataStream;
		BufferedReader dataReader = null;
		try {
			dataStream = new FileInputStream(new File(dataSource));
			dataReader =
					new BufferedReader(new InputStreamReader(dataStream,
					                                         Charset.defaultCharset()));
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
					"Error occured while reading from the file: " + dataSource + "." +
							e.getMessage();
			logger.error(msg, e);
			throw new DatasetServiceException(msg);
		} finally {
			if (dataReader != null) {
				dataReader.close();
			}
		}
	}

	/*
	 * check whether a given String represents a number or not.
	 */
	// TODO : compile
	private boolean isNumeric(String inputData) {
		return inputData.matches("[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");
	}
}
