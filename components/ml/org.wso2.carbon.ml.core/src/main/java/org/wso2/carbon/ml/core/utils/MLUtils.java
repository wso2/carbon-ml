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
package org.wso2.carbon.ml.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.commons.domain.config.MLProperty;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;

public class MLUtils {

    /**
     * Get sequentially picked {@link SamplePoints}
     * @param in data as an input stream.
     * @param dataType type of data CSV, TSV
     * @param sampleSize rows x columns 
     * @return sequentially picked {@link SamplePoints}
     * @throws MLMalformedDatasetException
     */
    public static SamplePoints getSamplePoints(InputStream in, String dataType, int sampleSize)
            throws MLMalformedDatasetException {
        if (in == null ) {
            throw new MLMalformedDatasetException("Failed to parse the given null input stream.");
        }
        Reader reader = new InputStreamReader(in);
        CSVFormat dataFormat = DataTypeFactory.getCSVFormat(dataType);
        CSVParser parser = null;
        try {
            parser = new CSVParser(reader, dataFormat.withHeader().withAllowMissingColumnNames(true));
            Map<String, Integer> headerMap = parser.getHeaderMap();
            int recordsCount = 0;
            CSVRecord row;
            int featureSize = headerMap.size();
            int[] missing = new int[featureSize];
            int[] stringCellCount = new int[featureSize];
            
            if (sampleSize >= 0 && featureSize > 0) {
                sampleSize = sampleSize / featureSize;
            }
            // List containing actual data of the sample.
            List<List<String>> columnData = new ArrayList<List<String>>();
            
            for (int i=0; i<featureSize; i++) {
                columnData.add(new ArrayList<String>());
            }
            
            Iterator<CSVRecord> datasetIterator = parser.iterator();
            // Count the number of cells contain strings in each column.
            while (datasetIterator.hasNext() && (recordsCount != sampleSize || sampleSize < 0)) {
                row = datasetIterator.next();
                for (int currentCol = 0; currentCol < featureSize; currentCol++) {
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
            throw new MLMalformedDatasetException("Failed to parse the given input stream. Cause: "+e, e);
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

    private static class DataTypeFactory {
        static CSVFormat getCSVFormat(String dataType) {
            if ("TSV".equalsIgnoreCase(dataType)) {
                return CSVFormat.TDF;
            }
            return CSVFormat.RFC4180;
        }
    }
    
    public static class ColumnSeparatorFactory {
        public static String getColumnSeparator(String dataType) {
            if ("TSV".equalsIgnoreCase(dataType)) {
                return "\t";
            }
            return ",";
        }
    }
    
    /**
     * Retrieve the indices of features where discard row imputaion is applied.
     * 
     * @param workflow      Machine learning workflow
     * @param imputeOption  Impute option
     * @return              Returns indices of features where discard row imputaion is applied
     */
    public static List<Integer> getImputeFeatureIndices(Workflow workflow, String imputeOption) {
        List<Integer> imputeFeatureIndices = new ArrayList<Integer>();
        for (Feature feature : workflow.getFeatures()) {
            if (feature.getImputeOption().equals(imputeOption)) {
                imputeFeatureIndices.add(feature.getIndex());
            }
        }
        return imputeFeatureIndices;
    }
    
    /**
     * Retrieve the index of a feature in the dataset.
     * 
     * @param feature           Feature name
     * @param headerRow         First row (header) in the data file
     * @param columnSeparator   Column separator character
     * @return                  Index of the response variable
     */
    public static int getFeatureIndex(String feature, String headerRow, String columnSeparator) {
        int featureIndex = 0;
        String[] headerItems = headerRow.split(columnSeparator);
        for (int i = 0; i < headerItems.length; i++) {
            if (headerItems[i] != null) {
                String column = headerItems[i].replace("\"", "").trim();
                if (feature.equals(column)) {
                    featureIndex = i;
                    break;
                }
            }
        }        
        return featureIndex;
    }
    
    public static MLDatasetVersion getMLDatsetVersion(int tenantId, long datasetId, String userName, String name, String version, URI targetPath, SamplePoints samplePoints) {
        MLDatasetVersion valueSet = new MLDatasetVersion();
        valueSet.setTenantId(tenantId);
        valueSet.setDatasetId(datasetId);
        valueSet.setName(name);
        valueSet.setVersion(version);
        valueSet.setTargetPath(targetPath);
        valueSet.setSamplePoints(samplePoints);
        valueSet.setUserName(userName);
        return valueSet;
    }
    
    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    /**
     * Get {@link Properties} from a list of {@link MLProperty}
     * @param mlProperties list of {@link MLProperty}
     * @return {@link Properties}
     */
    public static Properties getProperties(List<MLProperty> mlProperties) {
        Properties properties = new Properties();
        for (MLProperty mlProperty : mlProperties) {
            if (mlProperty != null) {
                properties.put(mlProperty.getName(), mlProperty.getValue());
            }
        }
        return properties;
        
    }
    
    public static double[] toDoubleArray(String[] inArray) {
        double[] outArray = new double[inArray.length];
        int idx = 0;
        for (String string : inArray) {
            outArray[idx] = Double.parseDouble(string);
            idx++;
        }
        
        return outArray;
    }
    
}
