/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.core.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.wso2.carbon.ml.commons.domain.FeatureType;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.domain.SummaryStatisticsSettings;
import org.wso2.carbon.ml.dataset.exceptions.DatasetSummaryException;
import org.wso2.carbon.ml.dataset.exceptions.MLConfigurationParserException;

/**
 * Responsible for generating summary stats for a given set of sample points.
 */
public class SummaryStatsGenerator implements Runnable {
    private SummaryStatisticsSettings summarySettings;
    private static final Log logger = LogFactory.getLog(SummaryStatsGenerator.class);
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
    // Array containing number of unique values of each feature in the data-set.
    private int[] unique;
    // Array containing data-type of each feature in the data-set.
    private String[] type;
    // Map containing indices and names of features of the data-set.
    private Map<String, Integer> headerMap;

    private String datasetVersionId;

    public SummaryStatsGenerator(String datasetVersionId, SummaryStatisticsSettings summaryStatsSettings,
            SamplePoints samplePoints) throws MLConfigurationParserException {
        this.datasetVersionId = datasetVersionId;
        this.summarySettings = summaryStatsSettings;
        this.headerMap = samplePoints.getHeader();
        this.columnData = samplePoints.getSamplePoints();
        int noOfFeatures = this.headerMap.size();
        // Initialize the lists.
        this.unique = new int[noOfFeatures];
        this.type = new String[noOfFeatures];
        this.histogram = new EmpiricalDistribution[noOfFeatures];
        for (int i = 0; i < noOfFeatures; i++) {
            this.descriptiveStats.add(new DescriptiveStatistics());
            this.graphFrequencies.add(new TreeMap<String, Integer>());
        }
    }

    /**
     * get a summary of a sample from the given CSV file, including descriptive-statistics, missing values, unique
     * values and etc. to display in the data view.
     */
    @Override
    public void run() {

        // process the sample points and generate summary stats

        try {
            // Find the columns containing String and Numeric data.
            identifyColumnDataType();
            // Calculate descriptive statistics.
            calculateDescriptiveStats();
            // Calculate frequencies of each bin of the String features.
            calculateStringColumnFrequencies();
            // Calculate frequencies of each bin of the Numerical features.
            calculateNumericColumnFrequencies();
            // TODO Update the database with calculated summary statistics.
            // DatabaseService dbService = MLDatasetServiceValueHolder.getDatabaseService();
            // dbService.updateSummaryStatistics(this.datasetID, headerMap, this.type, this.graphFrequencies,
            // this.missing, this.unique, this.descriptiveStats, true);
            if (logger.isDebugEnabled()) {
                logger.debug("Summary statistics successfully generated for dataset: " + datasetVersionId);
            }
        } catch (Exception e) {
            logger.error("Error occurred while Calculating summary statistics " + "for dataset "
                    + this.datasetVersionId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Finds the columns with Categorical data and Numerical data. Stores the raw-data in a list.
     *
     * @param datasetIterator Iterator for the CSV parser.
     * @param sampleSize Size of the sample.
     * @throws DatasetSummaryException
     */
    protected String[] identifyColumnDataType() {
        int[] stringCellCount = new int[this.headerMap.size()];

        // If atleast one cell contains strings, then the column is considered to has string data.
        for (int col = 0; col < headerMap.size(); col++) {
            if (stringCellCount[col] > 0) {
                this.stringDataColumnPositions.add(col);
                this.type[col] = FeatureType.CATEGORICAL;
            } else {
                this.numericDataColumnPositions.add(col);
                this.type[col] = FeatureType.NUMERICAL;
            }
        }

        return type;
    }

    /**
     * Calculate descriptive statistics for Numerical columns.
     */
    protected List<DescriptiveStatistics> calculateDescriptiveStats() {
        double cellValue;
        int currentCol;
        // Iterate through each column.
        for (currentCol = 0; currentCol < this.headerMap.size(); currentCol++) {
            // If the column is numerical.
            if (this.numericDataColumnPositions.contains(currentCol)) {
                // Convert each cell value to double and append to the
                // Descriptive-statistics object.
                for (int row = 0; row < this.columnData.get(currentCol).size(); row++) {
                    if (!this.columnData.get(currentCol).get(row).isEmpty()) {
                        cellValue = Double.parseDouble(columnData.get(currentCol).get(row));
                        this.descriptiveStats.get(currentCol).addValue(cellValue);
                    }
                }
            }
        }

        return descriptiveStats;
    }

    /**
     * Calculate the frequencies of each category in String columns, needed to plot bar graphs/histograms. Calculate
     * unique value counts.
     *
     * @param noOfIntervals Number of intervals to be calculated.
     */
    protected List<SortedMap<?, Integer>> calculateStringColumnFrequencies() {

        Iterator<Integer> stringColumns = this.stringDataColumnPositions.iterator();
        int currentCol;
        // Iterate through all Columns with String data.
        while (stringColumns.hasNext()) {
            currentCol = stringColumns.next();
            SortedMap<String, Integer> frequencies = new TreeMap<String, Integer>();
            // Create a unique set from the column.
            Set<String> uniqueSet = new HashSet<String>(this.columnData.get(currentCol));
            // Count the frequencies in each unique value.
            this.unique[currentCol] = uniqueSet.size();
            for (String uniqueValue : uniqueSet) {
                frequencies.put(uniqueValue.toString(),
                        Collections.frequency(this.columnData.get(currentCol), uniqueValue));
            }
            graphFrequencies.set(currentCol, frequencies);
        }

        return graphFrequencies;
    }

    /**
     * Calculate the frequencies of each category/interval of Numerical data columns.
     *
     * @param categoricalThreshold Threshold for number of categories, to be considered as discrete data.
     * @param noOfIntervals Number of intervals to be calculated for continuous data
     */
    protected List<SortedMap<?, Integer>> calculateNumericColumnFrequencies() {
        int categoricalThreshold = summarySettings.getCategoricalThreshold();
        int noOfIntervals = summarySettings.getHistogramBins();
        Iterator<Integer> numericColumns = this.numericDataColumnPositions.iterator();
        int currentCol;
        // Iterate through all Columns with Numerical data.
        while (numericColumns.hasNext()) {
            currentCol = numericColumns.next();
            // Create a unique set from the column.
            Set<String> uniqueSet = new HashSet<String>(this.columnData.get(currentCol));
            // If the unique values are less than or equal to maximum-category-limit.
            this.unique[currentCol] = uniqueSet.size();
            if (this.unique[currentCol] <= categoricalThreshold) {
                // Change the data type to categorical.
                this.type[currentCol] = FeatureType.CATEGORICAL;
                // Calculate the category frequencies.
                SortedMap<Double, Integer> frequencies = new TreeMap<Double, Integer>();
                for (String uniqueValue : uniqueSet) {
                    if (!uniqueValue.isEmpty()) {
                        frequencies.put(Double.parseDouble(uniqueValue),
                                Collections.frequency(this.columnData.get(currentCol), uniqueValue));
                    }
                }
                this.graphFrequencies.set(currentCol, frequencies);
            } else {
                // If unique values are more than the threshold, calculate interval frequencies.
                calculateIntervalFreqs(currentCol, noOfIntervals);
            }
        }

        return graphFrequencies;
    }

    /**
     * Calculate the frequencies of each interval of a column.
     *
     * @param column Column of which the frequencies are to be calculated.
     * @param intervals Number of intervals to be split.
     */
    protected List<SortedMap<?, Integer>> calculateIntervalFreqs(int column, int intervals) {
        SortedMap<Integer, Integer> frequencies = new TreeMap<Integer, Integer>();
        double[] data = new double[this.columnData.get(column).size()];
        // Create an array from the column data.
        for (int row = 0; row < columnData.get(column).size(); row++) {
            if (!this.columnData.get(column).get(row).isEmpty()) {
                data[row] = Double.parseDouble(this.columnData.get(column).get(row));
            }
        }
        // Create equal partitions.
        this.histogram[column] = new EmpiricalDistribution(intervals);
        this.histogram[column].load(data);

        // Get the frequency of each partition.
        int bin = 0;
        for (SummaryStatistics stats : this.histogram[column].getBinStats()) {
            frequencies.put(bin++, (int) stats.getN());
        }
        this.graphFrequencies.set(column, frequencies);

        return graphFrequencies;
    }
}
