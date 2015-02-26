package org.wso2.carbon.ml.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;

public class MLUtils {

    public static SamplePoints getSamplePoints(InputStream in, String dataType, int sampleSize)
            throws MLMalformedDatasetException {
        Reader reader = new InputStreamReader(in);
        CSVFormat dataFormat = DataTypeFactory.getCSVFormat(dataType);
        CSVParser parser = null;
        try {
            parser = new CSVParser(reader, dataFormat.withHeader().withAllowMissingColumnNames(true));
            Map<String, Integer> headerMap = parser.getHeaderMap();
            int recordsCount = 0;
            CSVRecord row;
            int[] stringCellCount = new int[headerMap.size()];
            // List containing actual data of the sample.
            List<List<String>> columnData = new ArrayList<List<String>>();
            Iterator<CSVRecord> datasetIterator = parser.iterator();
            // Count the number of cells contain strings in each column.
            while (datasetIterator.hasNext() && recordsCount != sampleSize) {
                row = datasetIterator.next();
                for (int currentCol = 0; currentCol < headerMap.size(); currentCol++) {
                    if (!row.get(currentCol).isEmpty()) {
                        if (!NumberUtils.isNumber(row.get(currentCol))) {
                            stringCellCount[currentCol]++;
                        }
                    }

                    // Append the cell to the respective column.
                    columnData.get(currentCol).add(row.get(currentCol));
                }
                recordsCount++;
            }
            SamplePoints samplePoints = new SamplePoints();
            samplePoints.setHeader(headerMap);
            samplePoints.setSamplePoints(columnData);
            return samplePoints;
        } catch (IOException e) {
            throw new MLMalformedDatasetException("Failed to parse the given input stream.", e);
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
}
