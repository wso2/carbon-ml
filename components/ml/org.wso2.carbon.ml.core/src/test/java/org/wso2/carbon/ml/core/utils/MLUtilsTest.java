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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.impl.FileInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;

public class MLUtilsTest {
    @BeforeClass
    public void beforeClass() {
    }

    @Test
    public void getSamplePoints() throws URISyntaxException, MLInputAdapterException {
        MLInputAdapter inputAdapter = new FileInputAdapter();

        /*
         * Dataset Columns: 55 Rows: 1000
         */
        String uriString = "src/test/resources/fcSample.csv";
        URI uri;
        InputStream in = null;
        uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
        in = inputAdapter.readDataset(uri);
        SamplePoints samplePoints;
        try {

            // extract limited set of points
            int size = 10000;
            samplePoints = MLUtils.getSamplePoints(in, "csv", size);
            List<List<String>> columnData = samplePoints.getSamplePoints();
            Assert.assertEquals(samplePoints.getSamplePoints().size(), 55);
            int totalPoints = columnData.get(0).size();
            Assert.assertEquals(totalPoints, size / 55);

            // extract all points
            in = inputAdapter.readDataset(uri);
            samplePoints = MLUtils.getSamplePoints(in, "csv", -1);
            columnData = samplePoints.getSamplePoints();
            totalPoints = columnData.get(0).size();
            Assert.assertEquals(totalPoints, 1000);

        } catch (MLMalformedDatasetException e) {
            Assert.assertNull(e);
        }

        // malformed document
        uriString = "src/test/resources/fcMalformedSample.csv";
        uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
        in = inputAdapter.readDataset(uri);
        try {
            samplePoints = MLUtils.getSamplePoints(in, "csv", 10);
        } catch (Exception e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e instanceof MLMalformedDatasetException, true);
        }

        // null input stream
        in = null;
        try {
            samplePoints = MLUtils.getSamplePoints(in, "csv", 10);
        } catch (Exception e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e instanceof MLMalformedDatasetException, true);
        }

        // test TSV file
        uriString = "src/test/resources/fcSample.tsv";
        uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
        in = inputAdapter.readDataset(uri);
        try {
            samplePoints = MLUtils.getSamplePoints(in, "tsv", 550);
            List<List<String>> columnData = samplePoints.getSamplePoints();
            Assert.assertEquals(samplePoints.getSamplePoints().size(), 55);
            int totalPoints = columnData.get(0).size();
            Assert.assertEquals(totalPoints, 550 / 55);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }
    
    @Test
    public void getDate() {
        String date = MLUtils.getDate();
        Assert.assertEquals(date.contains("-"), true);
        Assert.assertEquals(date.contains("_"), true);
        Assert.assertEquals(!date.contains(":"), true);
    }
}
