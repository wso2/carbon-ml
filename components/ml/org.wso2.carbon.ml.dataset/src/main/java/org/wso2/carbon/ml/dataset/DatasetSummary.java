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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.wso2.carbon.ml.dataset.constants.FeatureType;
import org.wso2.carbon.ml.dataset.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.dataset.exceptions.DatasetSummaryException;

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

/**
 * Class Generate Summary statistics for a data-set.
 */
public class DatasetSummary {
	private static final Log logger = LogFactory.getLog(DatasetSummary.class);

	// List containing positions of columns with numerical data.
	private List<Integer> numericDataColumnPositions = new ArrayList<Integer>();
	// List containing positions of columns with string data.
	private List<Integer> stringDataColumnPositions = new ArrayList<Integer>();
	// List containing actual data of the sample.
	private List<List<String>> columnData = new ArrayList<List<String>>();
	// List containing descriptive statistics for each feature.
	private List<DescriptiveStatistics> descriptiveStats = new ArrayList<DescriptiveStatistics>();
	// List containing bin frequencies for each feature.
	private List<SortedMap<?, Integer>> graphFrequencies = new ArrayList<SortedMap<?, Integer>>();
	// Array containing histograms of each feature in the data-set.
	private EmpiricalDistribution[] histogram;
	// Array containing number of missing values of each feature in the data-set.
	private int[] missing;
	// Array containing number of unique values of each feature in the data-set.
	private int[] unique;
	// Array containing data-type of each feature in the data-set.
	private FeatureType[] type;
	// Map containing indices and names of features of the data-set.
	private Map<String, Integer> headerMap;

	private String datasetID;
	private CSVParser parser;

	/**
	 * Constructor to create the parser for the data-set and initialize the lists.
	 * 
	 * @param csvDataFile File object of the data-set csv file
	 * @param datasetID Unique Identifier of the data-set
	 * @throws DatasetSummaryException
	 */
	protected DatasetSummary(File csvDataFile, String datasetID) throws DatasetSummaryException {
		this.datasetID = datasetID;
		try {
			this.parser =
					CSVParser.parse(csvDataFile, Charset.defaultCharset(),
					                CSVFormat.RFC4180.withHeader());
			// get the header.
			this.headerMap = this.parser.getHeaderMap();
			int noOfFeatures = this.headerMap.size();
			//initialize the lists.
			this.missing = new int[noOfFeatures];
			this.unique = new int[noOfFeatures];
			this.type = new FeatureType[noOfFeatures];
			this.histogram = new EmpiricalDistribution[noOfFeatures];
			for (int i = 0; i < noOfFeatures; i++) {
				this.descriptiveStats.add(new DescriptiveStatistics());
				this.graphFrequencies.add(new TreeMap<String, Integer>());
				this.columnData.add(new ArrayList<String>());
			}
		} catch (IOException e) {
			throw new DatasetSummaryException("Error occured while reading from the dataset " +
					datasetID + ": " + e.getMessage(), e);
		}
	}
	
	/**
	 * get a summary of a sample from the given csv file, including
	 * descriptive-stats, missing values, unique values and etc. to display in
	 * the data view.
	 * 
	 * @param sampleSize Number of records to be
	 * @param noOfIntervals
	 * @param categoricalThreshold
	 * @param include
	 * @return
	 * @throws DatasetSummaryException
	 */
	protected int generateSummary(int sampleSize, int noOfIntervals, int categoricalThreshold,
	                              boolean include)
	                            		  throws DatasetSummaryException {
		try {
			// Find the columns containing String and Numeric data.
			findColumnDataType(this.parser.iterator(), sampleSize);
			// Calculate descriptive statistics.
			calculateDescriptiveStats();
			// Calculate frequencies of each bin of the String features.
			calculateStringColumnFrequencies(noOfIntervals);
			// Calculate frequencies of each bin of the Numerical features.
			calculateNumericColumnFrequencies(categoricalThreshold, noOfIntervals);
			// Update the database with calculated summary statistics.
			String[] header = this.headerMap.keySet().toArray(new String[0]);

			DatabaseHandler dbHandler = DatabaseHandler.getDatabaseHandler();
			dbHandler.updateSummaryStatistics(this.datasetID, header, this.type,
			                                  this.graphFrequencies,
			                                  this.missing, this.unique,
			                                  this.descriptiveStats, include);
			return this.headerMap.size();
		} catch (DatabaseHandlerException e) {
			throw new DatasetSummaryException(
			                                  "Error occured while Calculating summary statistics for dataset " +
			                                		  this.datasetID + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Finds the columns with Categorical data and Numerical data. Stores the
	 * raw-data in a list.
	 *
	 * @param datasetIterator
	 *            Iterator for the csv parser
	 * @param sampleSize
	 *            Size of the sample
	 */
	private void findColumnDataType(Iterator<CSVRecord> datasetIterator, int sampleSize) {
		int recordsCount = 0;
		CSVRecord row;
		int[] stringCellCount = new int[this.headerMap.size()];
		while (datasetIterator.hasNext() && recordsCount != sampleSize) {
			row = datasetIterator.next();
			// for each cell in the row.
			for (int currentCol = 0; currentCol < this.headerMap.size(); currentCol++) {
				if (!"".equalsIgnoreCase(row.get(currentCol))) {
					// count the number of cell contain strings.
					if (!NumberUtils.isNumber(row.get(currentCol))) {
						stringCellCount[currentCol]++;
					}
				} else {
					// if the cell is empty, increase the missing value count.
					this.missing[currentCol]++;
				}
				// append the cell to the respective column.
				this.columnData.get(currentCol).add(row.get(currentCol));
			}
			recordsCount++;
		}
		logger.debug("Sample size: " + recordsCount);

		// If atleast one cell contains strings, then the cell is considered to
		// has string data.
		for (int col = 0; col < headerMap.size(); col++) {
			if (stringCellCount[col] > 0) {
				this.stringDataColumnPositions.add(col);
				this.type[col] = FeatureType.CATEGORICAL;
			} else {
				this.numericDataColumnPositions.add(col);
				this.type[col] = FeatureType.NUMERICAL;
			}
		}
	}

	/**
	 * Calculate descriptive statistics for Numerical columns.
	 */
	private void calculateDescriptiveStats() {
		double cellValue;
		int currentCol;

		// iterate through each column.
		for (currentCol = 0; currentCol < this.headerMap.size(); currentCol++) {
			// if the column is numerical.
			if (this.numericDataColumnPositions.contains(currentCol)) {
				// convert each cell value to double and append to the
				// descriptive-statistics object.
				for (int row = 0; row < this.columnData.get(currentCol).size(); row++) {
					if (!this.columnData.get(currentCol).get(row).isEmpty()) {
						cellValue = Double.parseDouble(columnData.get(currentCol).get(row));
						this.descriptiveStats.get(currentCol).addValue(cellValue);
					}
				}
			}
		}
	}

	/**
	 * Calculate the frequencies of each category in String columns, needed to
	 * plot bar graphs/histograms.
	 * Calculate unique value counts.
	 *
	 * @param noOfIntervals
	 *            Number of intervals to be calculated
	 */
	private void calculateStringColumnFrequencies(int noOfIntervals) {

		Iterator<Integer> stringColumns = this.stringDataColumnPositions.iterator();
		int currentCol;
		// Iterate through all Columns with String data.
		while (stringColumns.hasNext()) {
			currentCol = stringColumns.next();
			SortedMap<String, Integer> frequencies = new TreeMap<String, Integer>();
			// create a unique set from the column.
			Set<String> uniqueSet = new HashSet<String>(this.columnData.get(currentCol));
			// count the frequencies in each unique value.
			this.unique[currentCol] = uniqueSet.size();
			for (String uniqueValue : uniqueSet) {
				frequencies.put(uniqueValue.toString(),
				                Collections.frequency(this.columnData.get(currentCol), uniqueValue));
			}
			graphFrequencies.set(currentCol, frequencies);
		}
	}

	/**
	 * Calculate the frequencies of each category/interval of Numerical data
	 * columns.
	 *
	 * @param categoricalThreshold
	 *            Threshold for number of categories, to be considered as
	 *            discrete data
	 * @param noOfIntervals
	 *            Number of intervals to be calculated for continuous data
	 */
	private void calculateNumericColumnFrequencies(int categoricalThreshold, int noOfIntervals) {
		Iterator<Integer> numericColumns = this.numericDataColumnPositions.iterator();
		int currentCol;
		// Iterate through all Columns with Numerical data.
		while (numericColumns.hasNext()) {
			currentCol = numericColumns.next();
			// create a unique set from the column.
			Set<String> uniqueSet = new HashSet<String>(this.columnData.get(currentCol));
			// if the unique values are less than or equal to
			// maximum-category-limit.
			this.unique[currentCol] = uniqueSet.size();
			if (this.unique[currentCol] <= categoricalThreshold) {
				// change the data type to categorical.
				this.type[currentCol] = FeatureType.CATEGORICAL;
				// calculate the category frequencies.
				SortedMap<Double, Integer> frequencies = new TreeMap<Double, Integer>();
				for (String uniqueValue : uniqueSet) {
					frequencies.put(Double.parseDouble(uniqueValue),
					                Collections.frequency(this.columnData.get(currentCol),
					                                      uniqueValue));
				}
				this.graphFrequencies.set(currentCol, frequencies);
			} else {
				// if unique values are more than the threshold, calculate
				// interval frequencies.
				claculateIntervalFreqs(currentCol, noOfIntervals);
			}
		}
	}

	/**
	 * Calculate the frequencies of each interval of a column.
	 *
	 * @param column
	 *            Column of which the frequencies are to be calculated
	 * @param intervals
	 *            Number of intervals to be split
	 */
	private void claculateIntervalFreqs(int column, int intervals) {
		SortedMap<Integer, Integer> frequencies = new TreeMap<Integer, Integer>();
		double[] data = new double[this.columnData.get(column).size()];

		// create an array from the column data.
		for (int row = 0; row < columnData.get(column).size(); row++) {
			if (!this.columnData.get(column).get(row).isEmpty()) {
				data[row] = Double.parseDouble(this.columnData.get(column).get(row));
			}
		}
		// create equal partitions.
		this.histogram[column] = new EmpiricalDistribution(intervals);
		this.histogram[column].load(data);

		// get the frequency of each partition.
		int bin = 0;
		for (SummaryStatistics stats : this.histogram[column].getBinStats()) {
			frequencies.put(bin++, (int) stats.getN());
		}
		this.graphFrequencies.set(column, frequencies);
	}

	/**
	 * Retrieve the sample.
	 *
	 * @return SamplePoints object containing raw-data of the sample
	 */
	protected SamplePoints samplePoints() {
		SamplePoints samplPoints = new SamplePoints();
		samplPoints.setHeader(this.headerMap);
		samplPoints.setSamplePoints(this.columnData);
		return samplPoints;
	}
}
