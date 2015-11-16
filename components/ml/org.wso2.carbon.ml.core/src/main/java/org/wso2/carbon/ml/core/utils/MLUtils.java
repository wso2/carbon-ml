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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.commons.domain.config.MLProperty;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.core.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.core.spark.transformations.RowsToLines;

/**
 * Common utility methods used in ML core.
 */

public class MLUtils {

    /**
     * Generate a random sample of the dataset using Spark.
     */
    public static SamplePoints getSample(String path, String dataType, int sampleSize, boolean containsHeader,
            String sourceType, int tenantId) throws MLMalformedDatasetException {

        JavaSparkContext sparkContext = null;
        try {
            Map<String, Integer> headerMap = null;
            // List containing actual data of the sample.
            List<List<String>> columnData = new ArrayList<List<String>>();
            CSVFormat dataFormat = DataTypeFactory.getCSVFormat(dataType);

            // java spark context
            sparkContext = MLCoreServiceValueHolder.getInstance().getSparkContext();
            JavaRDD<String> lines;
            // parse lines in the dataset
            lines = sparkContext.textFile(path);
            // validates the data format of the file
            String firstLine = lines.first();
            if (!firstLine.contains("" + dataFormat.getDelimiter())) {
                throw new MLMalformedDatasetException(String.format(
                        "File content does not match the data format. [First Line] %s [Data Format] %s", firstLine,
                        dataType));
            }
            return getSamplePoints(sampleSize, containsHeader, headerMap, columnData, dataFormat, lines);

        } catch (Exception e) {
            throw new MLMalformedDatasetException("Failed to extract the sample points from path: " + path
                    + ". Cause: " + e, e);
        }
    }

    public static String getFirstLine(String filePath) {
        JavaSparkContext sparkContext = MLCoreServiceValueHolder.getInstance().getSparkContext();
        return sparkContext.textFile(filePath).first();
    }
    
    /**
     * Generate a random sample of the dataset using Spark.
     */
    public static SamplePoints getSampleFromDAS(String path, int sampleSize, String sourceType, int tenantId)
            throws MLMalformedDatasetException {

        JavaSparkContext sparkContext = null;
        try {
            Map<String, Integer> headerMap = null;
            // List containing actual data of the sample.
            List<List<String>> columnData = new ArrayList<List<String>>();

            // java spark context
            sparkContext = MLCoreServiceValueHolder.getInstance().getSparkContext();
            JavaRDD<String> lines;
            String headerLine = extractHeaderLine(path, tenantId);
            headerMap = generateHeaderMap(headerLine, CSVFormat.RFC4180);

            // DAS case path = table name
            lines = getLinesFromDASTable(path, tenantId, sparkContext);

            return getSamplePoints(sampleSize, true, headerMap, columnData, CSVFormat.RFC4180, lines);

        } catch (Exception e) {
            throw new MLMalformedDatasetException("Failed to extract the sample points from path: " + path
                    + ". Cause: " + e, e);
        }
    }

    public static JavaRDD<String> getLinesFromDASTable(String tableName, int tenantId, JavaSparkContext sparkContext)
            throws AnalyticsTableNotAvailableException, AnalyticsException {
        JavaRDD<String> lines;
        String tableSchema = extractTableSchema(tableName, tenantId);
        SQLContext sqlCtx = new SQLContext(sparkContext);
        sqlCtx.sql("CREATE TEMPORARY TABLE ML_REF USING org.wso2.carbon.analytics.spark.core.sources.AnalyticsRelationProvider "
                + "OPTIONS ("
                + "tenantId \""
                + tenantId
                + "\", "
                + "tableName \""
                + tableName
                + "\", "
                + "schema \""
                + tableSchema + "\"" + ")");

        DataFrame dataFrame = sqlCtx.sql("select * from ML_REF");
        // Additional auto-generated column "_timestamp" needs to be dropped because it is not in the schema.
        JavaRDD<Row> rows = dataFrame.drop("_timestamp").javaRDD();
        lines = rows.map(new RowsToLines.Builder().separator(CSVFormat.RFC4180.getDelimiter() + "").build());
        return lines;
    }

    private static SamplePoints getSamplePoints(int sampleSize, boolean containsHeader, Map<String, Integer> headerMap,
            List<List<String>> columnData, CSVFormat dataFormat, JavaRDD<String> lines) {
        int featureSize;
        int[] missing;
        int[] stringCellCount;
        int[] decimalCellCount;
        // take the first line
        String firstLine = lines.first();
        // count the number of features
        featureSize = getFeatureSize(firstLine, dataFormat);

        List<Integer> featureIndices = new ArrayList<Integer>();
        for (int i = 0; i < featureSize; i++) {
            featureIndices.add(i);
        }

        String columnSeparator = String.valueOf(dataFormat.getDelimiter());
        HeaderFilter headerFilter = new HeaderFilter.Builder().header(lines.first()).build();
        JavaRDD<String> data = lines.filter(headerFilter).cache();
        Pattern pattern = MLUtils.getPatternFromDelimiter(columnSeparator);
        LineToTokens lineToTokens = new LineToTokens.Builder().separator(pattern).build();
        JavaRDD<String[]> tokens = data.map(lineToTokens);

        // remove from cache
        data.unpersist();
        // add to cache
        tokens.cache();

        missing = new int[featureSize];
        stringCellCount = new int[featureSize];
        decimalCellCount = new int[featureSize];
        if (sampleSize >= 0 && featureSize > 0) {
            sampleSize = sampleSize / featureSize;
        }
        for (int i = 0; i < featureSize; i++) {
            columnData.add(new ArrayList<String>());
        }

        if (headerMap == null) {
            // generate the header map
            if (containsHeader) {
                headerMap = generateHeaderMap(lines.first(), dataFormat);
            } else {
                headerMap = generateHeaderMap(featureSize);
            }
        }

        // take a random sample
        List<String[]> sampleLines = tokens.takeSample(false, sampleSize);

        // remove from cache
        tokens.unpersist();

        // iterate through sample lines
        for (String[] columnValues : sampleLines) {
            for (int currentCol = 0; currentCol < featureSize; currentCol++) {
                // Check whether the row is complete.
                if (currentCol < columnValues.length) {
                    // Append the cell to the respective column.
                    columnData.get(currentCol).add(columnValues[currentCol]);

                    if (MLConstants.MISSING_VALUES.contains(columnValues[currentCol])) {
                        // If the cell is empty, increase the missing value count.
                        missing[currentCol]++;
                    } else {
                        // check whether a column value is a string
                        if (!NumberUtils.isNumber(columnValues[currentCol])) {
                            stringCellCount[currentCol]++;
                        } else if (columnValues[currentCol].indexOf('.') != -1) {
                            // if it is a number and has the decimal point
                            decimalCellCount[currentCol]++;
                        }
                    }
                } else {
                    columnData.get(currentCol).add(null);
                    missing[currentCol]++;
                }
            }
        }

        SamplePoints samplePoints = new SamplePoints();
        samplePoints.setHeader(headerMap);
        samplePoints.setSamplePoints(columnData);
        samplePoints.setMissing(missing);
        samplePoints.setStringCellCount(stringCellCount);
        samplePoints.setDecimalCellCount(decimalCellCount);
        return samplePoints;
    }

    public static String extractTableSchema(String path, int tenantId) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        if (path == null) {
            return null;
        }
        AnalyticsDataAPI analyticsDataApi = (AnalyticsDataAPI) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(AnalyticsDataAPI.class, null);
        // table schema will be something like; <col1_name> <col1_type>,<col2_name> <col2_type>
        StringBuilder sb = new StringBuilder();
        AnalyticsSchema analyticsSchema = analyticsDataApi.getTableSchema(tenantId, path);
        Map<String, ColumnDefinition> columnsMap = analyticsSchema.getColumns();
        for (Iterator<Map.Entry<String, ColumnDefinition>> iterator = columnsMap.entrySet().iterator(); iterator
                .hasNext();) {
            Map.Entry<String, ColumnDefinition> column = iterator.next();
            sb.append(column.getKey() + " " + column.getValue().getType().name() + ",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    public static String extractHeaderLine(String path, int tenantId) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        if (path == null) {
            return null;
        }

        AnalyticsDataAPI analyticsDataApi = (AnalyticsDataAPI) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(AnalyticsDataAPI.class, null);
        // header line will be something like; <col1_name>,<col2_name>
        StringBuilder sb = new StringBuilder();
        AnalyticsSchema analyticsSchema = analyticsDataApi.getTableSchema(tenantId, path);
        Map<String, ColumnDefinition> columnsMap = analyticsSchema.getColumns();
        for (String columnName : columnsMap.keySet()) {
            sb.append(columnName + ",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    public static class DataTypeFactory {
        public static CSVFormat getCSVFormat(String dataType) {
            if ("TSV".equalsIgnoreCase(dataType)) {
                return CSVFormat.TDF;
            }
            return CSVFormat.RFC4180;
        }
    }

    public static class ColumnSeparatorFactory {
        public static String getColumnSeparator(String dataType) {
            CSVFormat csvFormat = DataTypeFactory.getCSVFormat(dataType);
            return csvFormat.getDelimiter() + "";
        }
    }

    /**
     * Retrieve the indices of features where discard row imputaion is applied.
     * 
     * @param workflow Machine learning workflow
     * @param imputeOption Impute option
     * @return Returns indices of features where discard row imputaion is applied
     */
    public static List<Integer> getImputeFeatureIndices(Workflow workflow, List<Integer> newToOldIndicesList,
            String imputeOption) {
        List<Integer> imputeFeatureIndices = new ArrayList<Integer>();
        for (Feature feature : workflow.getFeatures()) {
            if (feature.getImputeOption().equals(imputeOption) && feature.isInclude() == true) {
                int currentIndex = feature.getIndex();
                int newIndex = newToOldIndicesList.indexOf(currentIndex) != -1 ? newToOldIndicesList
                        .indexOf(currentIndex) : currentIndex;
                imputeFeatureIndices.add(newIndex);
            }
        }
        return imputeFeatureIndices;
    }

    /**
     * Retrieve the index of a feature in the dataset.
     * 
     * @param feature Feature name
     * @param headerRow First row (header) in the data file
     * @param columnSeparator Column separator character
     * @return Index of the response variable
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

    /**
     * Retrieve the index of a feature in the dataset.
     */
    public static int getFeatureIndex(String featureName, List<Feature> features) {
        if (featureName == null || features == null) {
            return -1;
        }
        for (Feature feature : features) {
            if (featureName.equals(feature.getName())) {
                return feature.getIndex();
            }
        }
        return -1;
    }

    /**
     * @param workflow Workflow
     * @return A list of indices of features to be included in the model
     */
    public static SortedMap<Integer, String> getIncludedFeaturesAfterReordering(Workflow workflow,
            List<Integer> newToOldIndicesList, int responseIndex) {
        SortedMap<Integer, String> inlcudedFeatures = new TreeMap<Integer, String>();
        List<Feature> features = workflow.getFeatures();
        for (Feature feature : features) {
            if (feature.isInclude() == true && feature.getIndex() != responseIndex) {
                int currentIndex = feature.getIndex();
                int newIndex = newToOldIndicesList.indexOf(currentIndex);
                inlcudedFeatures.put(newIndex, feature.getName());
            }
        }
        return inlcudedFeatures;
    }

    /**
     * @param workflow Workflow
     * @return A list of indices of features to be included in the model
     */
    public static SortedMap<Integer, String> getIncludedFeatures(Workflow workflow, int responseIndex) {
        SortedMap<Integer, String> inlcudedFeatures = new TreeMap<Integer, String>();
        List<Feature> features = workflow.getFeatures();
        for (Feature feature : features) {
            if (feature.isInclude() == true && feature.getIndex() != responseIndex) {
                inlcudedFeatures.put(feature.getIndex(), feature.getName());
            }
        }
        return inlcudedFeatures;
    }

    /**
     * @param tenantId Tenant ID of the current user
     * @param datasetId ID of the datstet
     * @param userName Name of the current user
     * @param name Dataset name
     * @param version Dataset version
     * @param targetPath path of the stored data set
     * @return Dataset Version Object
     */
    public static MLDatasetVersion getMLDatsetVersion(int tenantId, long datasetId, String userName, String name,
            String version, String targetPath) {
        MLDatasetVersion valueSet = new MLDatasetVersion();
        valueSet.setTenantId(tenantId);
        valueSet.setDatasetId(datasetId);
        valueSet.setName(name);
        valueSet.setVersion(version);
        valueSet.setTargetPath(targetPath);
        valueSet.setUserName(userName);
        return valueSet;
    }

    /**
     * @return Current date and time
     */
    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Get {@link Properties} from a list of {@link MLProperty}
     * 
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

    /**
     * @param inArray String array
     * @return Double array
     */
    public static double[] toDoubleArray(String[] inArray) {
        double[] outArray = new double[inArray.length];
        int idx = 0;
        for (String string : inArray) {
            outArray[idx] = Double.parseDouble(string);
            idx++;
        }

        return outArray;
    }

    public static Map<String, Integer> generateHeaderMap(int numberOfFeatures) {
        Map<String, Integer> headerMap = new HashMap<String, Integer>();
        for (int i = 1; i <= numberOfFeatures; i++) {
            headerMap.put("V" + i, i - 1);
        }
        return headerMap;
    }

    public static Map<String, Integer> generateHeaderMap(String line, CSVFormat format) {
        Map<String, Integer> headerMap = new HashMap<String, Integer>();
        String[] values = line.split("" + format.getDelimiter());
        int i = 0;
        for (String value : values) {
            headerMap.put(value, i);
            i++;
        }
        return headerMap;
    }

    public static int getFeatureSize(String line, CSVFormat format) {
        String[] values = line.split("" + format.getDelimiter());
        return values.length;
    }
    
    public static String[] getFeatures(String line, CSVFormat format) {
        String[] values = line.split("" + format.getDelimiter());
        return values;
    }

    /**
     * format an error message.
     */
    public static String getErrorMsg(String customMessage, Exception ex) {
        if (ex != null) {
            return customMessage + " Cause: " + ex.getClass().getName() + " - " + ex.getMessage();
        }
        return customMessage;
    }

    /**
     * Utility method to get key from value of a map.
     *
     * @param map Map to be searched for a key
     * @param value Value of the key
     */
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Utility method to convert a String array to CSV/TSV row string.
     *
     * @param array String array to be converted
     * @param delimiter Delimiter to be used (comma for CSV tab for TSV)
     * @return CSV/TSV row string
     */
    public static String arrayToCsvString(String[] array, char delimiter) {
        StringBuilder arrayString = new StringBuilder();
        for(String arrayElement : array) {
            arrayString.append(arrayElement);
            arrayString.append(delimiter);
        }
        return arrayString.toString();
    }
    
    /**
     * Generates a pattern to represent CSV or TSV format.
     * 
     * @param delimiter "," or "\t"
     * @return Pattern
     */
    public static Pattern getPatternFromDelimiter(String delimiter) {
        return Pattern.compile(delimiter + "(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");
    }
}
