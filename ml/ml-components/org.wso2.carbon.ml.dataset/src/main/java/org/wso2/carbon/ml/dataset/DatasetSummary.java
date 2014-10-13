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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class DatasetSummary {
	private static final Log logger = LogFactory.getLog(DatasetSummary.class);
	private List<Integer> numericDataColPosstions = new ArrayList<Integer>();
	private List<Integer> stringDataColPosstions = new ArrayList<Integer>();
	private List<List<String>> columnData = new ArrayList<List<String>>();
	private List<DescriptiveStatistics> descriptiveStats = new ArrayList<DescriptiveStatistics>();
	private List<SortedMap<?, Integer>> graphFrequencies = new ArrayList<SortedMap<?, Integer>>();
	private EmpiricalDistribution[] histogram;
	private int[] missing;
	private int[] unique;
	private Map<String, Integer> headerMap;
	private FeatureType[] type;
	private static final int CATEGORY_THRESHOLD = 20;
	private int recordsCount = 0;

	/**
	 * get a summary of a sample from the given csv file, including
	 * descriptive-stats, missing values, unique values and etc. to display in
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
			// get the file-path of the data source
			String dataSource = dbHandler.getDataSource(dataSourceId);
			if (dataSource != null) {
				logger.debug("Data Source: " + dataSource);
				// read the input data file
				File csvData = new File(dataSource);
				CSVParser parser =
						CSVParser.parse(csvData, Charset.defaultCharset(),
						                CSVFormat.RFC4180.withHeader());
				Iterator<CSVRecord> datasetIterator = parser.iterator();

				// get the header
				headerMap = parser.getHeaderMap();

				// Initialize the lists
				initilize();

				// Find the columns contains String data.
				findColumnDataType(parser.iterator(), noOfRecords);

				// Calculate descriptive statistics
				calculateDescriptiveStats(datasetIterator, seperator);

				// Calculate frequencies of each bin of the features
				calculateFrequencies(noOfIntervals);

				// Update the database with calculated summary stats
				String[] header =
						headerMap.keySet().toArray(new String[parser.getHeaderMap()
						                                      .keySet().size()]);
				dbHandler.updateSummaryStatistics(dataSourceId, header, type, graphFrequencies,
				                                  missing, unique, descriptiveStats);
				return headerMap.size();
			} else {
				msg = "Data source not found.";
				logger.error(msg);
				throw new DatasetServiceException(msg);
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
	}

	/*
	 * initialize the Lists and arrays
	 */
	private void initilize() {
		int noOfFeatures = headerMap.size();
		missing = new int[noOfFeatures];
		unique = new int[noOfFeatures];
		type = new FeatureType[noOfFeatures];
		histogram = new EmpiricalDistribution[noOfFeatures];
		for (int i = 0; i < noOfFeatures; i++) {
			descriptiveStats.add(new DescriptiveStatistics());
			graphFrequencies.add(new TreeMap<String, Integer>());
			columnData.add(new ArrayList<String>());
		}
	}

	/*
	 * find the columns with Categorical data and Numerical data.
	 * store the data to a list.
	 */
	private void findColumnDataType(Iterator<CSVRecord> datasetIterator, int noOfRecords) {
		CSVRecord row;
		int[] stringCellCount = new int[headerMap.size()];
		while (datasetIterator.hasNext() && recordsCount != noOfRecords) {
			row = datasetIterator.next();
			// for each cell in the row
			for (int currentCol = 0; currentCol < headerMap.size(); currentCol++) {
				if (!"".equalsIgnoreCase(row.get(currentCol))) {
					// count the number of cell contain strings
					if (!NumberUtils.isNumber(row.get(currentCol))) {
						stringCellCount[currentCol]++;
					}

				} else {
					// if the cell is empty, increase the missing value count
					missing[currentCol]++;
				}
				// append the cell to the respective column
				columnData.get(currentCol).add(row.get(currentCol));
			}
			recordsCount++;
		}
		logger.debug("Sample size: " + recordsCount);

		/*
		 * If atleast one cell contains strings, then the cell is considered to
		 * has string data.
		 */
		for (int col = 0; col < headerMap.size(); col++) {
			if (stringCellCount[col] > 0) {
				stringDataColPosstions.add(col);
				type[col] = FeatureType.CATEGORICAL;
			} else {
				numericDataColPosstions.add(col);
				type[col] = FeatureType.NUMERICAL;
			}
		}
	}

	/*
	 * Calculate descriptive statistics for Numerical columns
	 */
	private void calculateDescriptiveStats(Iterator<CSVRecord> datasetIterator, String seperator) {
		double cellValue;
		int currentCol;

		// iterate through each column
		for (currentCol = 0; currentCol < headerMap.size(); currentCol++) {
			// if the column is numerical
			if (numericDataColPosstions.contains(currentCol)) {
				// convert each cell value to double and append to the
				// descriptive-stats object
				for (int row = 0; row < columnData.get(currentCol).size(); row++) {
					if (!columnData.get(currentCol).get(row).isEmpty()) {
						cellValue = Double.parseDouble(columnData.get(currentCol).get(row));
						descriptiveStats.get(currentCol).addValue(cellValue);
					}
				}
			}
		}
	}

	/*
	 * Calculate the frequencies of each bin (i.e. each category/interval),
	 * needed to plot bar graphs/histograms.
	 * Calculate unique value counts.
	 */
	private void calculateFrequencies(int noOfIntervals) {
		Iterator<Integer> numericColumns = numericDataColPosstions.iterator();
		Iterator<Integer> stringColumns = stringDataColPosstions.iterator();
		int currentCol;

		// Iterate through all Columns with String data
		while (stringColumns.hasNext()) {
			currentCol = stringColumns.next();
			SortedMap<String, Integer> frequencies = new TreeMap<String, Integer>();
			// create a unique set from the column
			Set<String> uniqueSet = new HashSet<String>(columnData.get(currentCol));
			// count the frequencies in each unique value
			unique[currentCol] = uniqueSet.size();
			for (String uniqueValue : uniqueSet) {
				frequencies.put(uniqueValue.toString(),
				                Collections.frequency(columnData.get(currentCol), uniqueValue));
			}
			graphFrequencies.set(currentCol, frequencies);
		}

		// Iterate through all Columns with Numerical data
		while (numericColumns.hasNext()) {
			currentCol = numericColumns.next();
			// create a unique set from the column
			Set<String> uniqueSet = new HashSet<String>(columnData.get(currentCol));
			// if the unique values are less than or equal to
			// maximum-category-limit
			unique[currentCol] = uniqueSet.size();
			if (unique[currentCol] <= CATEGORY_THRESHOLD) {
				// change the data type to categorical
				type[currentCol] = FeatureType.CATEGORICAL;
				// calculate the category frequencies
				claculateCategoryFreqs(currentCol, uniqueSet);
			} else {
				// if the unique values are more than twenty, calculate interval
				// frequencies
				claculateIntervalFreqs(currentCol, noOfIntervals);
			}
		}
	}

	/*
	 * Calculate the frequencies of each category of a column
	 */
	private void claculateCategoryFreqs(int currentCol, Set<String> uniqueSet) {
		SortedMap<Double, Integer> frequencies = new TreeMap<Double, Integer>();
		for (String uniqueValue : uniqueSet) {
			frequencies.put(Double.parseDouble(uniqueValue),
			                Collections.frequency(columnData.get(currentCol), uniqueValue));
		}
		graphFrequencies.set(currentCol, frequencies);
	}

	/*
	 * Calculate the frequencies of each interval of a column
	 */
	private void claculateIntervalFreqs(int currentCol, int intervals) {
		SortedMap<Double, Integer> frequencies = new TreeMap<Double, Integer>();
		double[] data = new double[columnData.get(currentCol).size()];

		// create an array from the column data
		for (int row = 0; row < columnData.get(currentCol).size(); row++) {
			if (!columnData.get(currentCol).get(row).isEmpty()) {
				data[row] = Double.parseDouble(columnData.get(currentCol).get(row));
			}
		}
		// create equal partitions
		histogram[currentCol] = new EmpiricalDistribution(intervals);
		histogram[currentCol].load(data);

		// get the frequency of each partition
		int bin = 0;
		for (SummaryStatistics stats : histogram[currentCol].getBinStats()) {
			frequencies.put(bin++ * 1.0, (int) stats.getN());
		}
		graphFrequencies.set(currentCol, frequencies);
	}

	/*
	 * returns the raw-data of the sample
	 */
	protected List<List<String>> getDataSample() {
		return columnData;
	}

	/*
	 * returns the header names and their positions in the data-set
	 */
	protected Map<String, Integer> getHeader() {
		return headerMap;
	}
}
