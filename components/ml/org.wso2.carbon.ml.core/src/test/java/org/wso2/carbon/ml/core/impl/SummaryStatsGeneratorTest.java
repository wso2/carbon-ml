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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.commons.domain.config.MLConfiguration;
import org.wso2.carbon.ml.core.exceptions.MLConfigurationParserException;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.core.utils.MLUtils.DataTypeFactory;

/**
 * TODO: incomplete test case, we need to have a small data-set and validate each function.
 */
public class SummaryStatsGeneratorTest {
    private SummaryStatsGenerator summaryGen;

    @BeforeClass
    public void beforeClass() throws URISyntaxException, MLInputAdapterException, MLConfigurationParserException {
        MLInputAdapter inputAdapter = new FileInputAdapter();

        /*
         * Dataset Columns: 55 Rows: 1000
         */
        String uriString = "src/test/resources/fcSample.csv";
        URI uri;
        SamplePoints samplePoints = null;
        InputStream in = null;
        uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
        in = inputAdapter.read(uri);

        try {

            // extract limited set of points
            int size = 10000;
            samplePoints = getSamplePoints(in, "csv", size, true);
            Assert.assertNotNull(samplePoints);

        } catch (MLMalformedDatasetException e) {
            Assert.assertNull(e);
        }
        MLConfigurationParser parser = new MLConfigurationParser();
        MLConfiguration config = parser.getMLConfiguration("src/test/resources/machine-learner.xml");
        summaryGen = new SummaryStatsGenerator(1, 1, config.getSummaryStatisticsSettings(), samplePoints);
    }

    @Test
    public void calculateDescriptiveStats() {
        Assert.assertNotNull(summaryGen.calculateDescriptiveStats());
    }

    @Test
    public void calculateNumericColumnFrequencies() {
        Assert.assertNotNull(summaryGen.calculateNumericColumnFrequencies());
    }

    @Test
    public void calculateStringColumnFrequencies() {
        Assert.assertNotNull(summaryGen.calculateStringColumnFrequencies());
    }

    @Test
    public void calculateIntervalFreqs() {
        Assert.assertNotNull(summaryGen.calculateIntervalFreqs(0, 2));
    }

    @Test
    public void identifyColumnDataType() {
        Assert.assertNotNull(summaryGen.identifyColumnDataType());
    }
    
    /**
     * Get sequentially picked {@link SamplePoints}
     * 
     * @param in data as an input stream.
     * @param dataType type of data CSV, TSV
     * @param sampleSize rows x columns
     * @return sequentially picked {@link SamplePoints}
     * @throws MLMalformedDatasetException
     */
    private static SamplePoints getSamplePoints(InputStream in, String dataType, int sampleSize, boolean containsHeader)
            throws MLMalformedDatasetException {
        if (in == null) {
            throw new MLMalformedDatasetException("Failed to parse the given null input stream.");
        }
        Reader reader = new InputStreamReader(in);
        CSVFormat dataFormat = DataTypeFactory.getCSVFormat(dataType);
        CSVParser parser = null;
        Map<String, Integer> headerMap = null;
        int recordsCount = 0;
        CSVRecord row;
        int featureSize = 0;
        int[] missing = null, stringCellCount = null;
        // List containing actual data of the sample.
        List<List<String>> columnData = new ArrayList<List<String>>();

        try {
            if (containsHeader) {
                parser = new CSVParser(reader, dataFormat.withHeader().withAllowMissingColumnNames(true));
                headerMap = parser.getHeaderMap();
                featureSize = headerMap.size();
            } else {
                parser = new CSVParser(reader, dataFormat.withSkipHeaderRecord(true).withAllowMissingColumnNames(true));
            }

            boolean isInitialized = false;
            Iterator<CSVRecord> datasetIterator = parser.iterator();
            // Count the number of cells contain strings in each column.
            while (datasetIterator.hasNext() && (recordsCount != sampleSize || sampleSize < 0)) {
                row = datasetIterator.next();

                if (!isInitialized) {
                    if (!containsHeader) {
                        featureSize = row.size();
                        headerMap = MLUtils.generateHeaderMap(featureSize);
                    }
                    missing = new int[featureSize];
                    stringCellCount = new int[featureSize];
                    if (sampleSize >= 0 && featureSize > 0) {
                        sampleSize = sampleSize / featureSize;
                    }
                    for (int i = 0; i < featureSize; i++) {
                        columnData.add(new ArrayList<String>());
                    }
                    isInitialized = true;
                }

                for (int currentCol = 0; currentCol < featureSize; currentCol++) {
                    // Check whether the row is complete.
                    if (currentCol < row.size()) {
                        // Append the cell to the respective column.
                        columnData.get(currentCol).add(row.get(currentCol));

                        if (row.get(currentCol).isEmpty()) {
                            // If the cell is empty, increase the missing value count.
                            missing[currentCol]++;
                        } else {
                            if (!NumberUtils.isNumber(row.get(currentCol))) {
                                stringCellCount[currentCol]++;
                            }
                        }
                    } else {
                        columnData.get(currentCol).add(null);
                        missing[currentCol]++;
                    }
                }
                recordsCount++;
            }
            SamplePoints samplePoints = new SamplePoints();
            samplePoints.setHeader(headerMap);
            samplePoints.setSamplePoints(columnData);
            samplePoints.setMissing(missing);
            samplePoints.setStringCellCount(stringCellCount);
            return samplePoints;
        } catch (Exception e) {
            throw new MLMalformedDatasetException("Failed to parse the given input stream. Cause: " + e, e);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    throw new MLMalformedDatasetException("Failed to close the CSV parser.", e);
                }
            }
        }
    }
}
