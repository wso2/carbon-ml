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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.exceptions.MLConfigurationParserException;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.utils.MLUtils;

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
        in = inputAdapter.readDataset(uri);

        try {

            // extract limited set of points
            int size = 10000;
            samplePoints = MLUtils.getSamplePoints(in, "csv", size);
            Assert.assertNotNull(samplePoints);

        } catch (MLMalformedDatasetException e) {
            Assert.assertNull(e);
        }
        MLConfigurationParser config = new MLConfigurationParser("src/test/resources/ml-config.xml");
        summaryGen = new SummaryStatsGenerator(1, config.getSummaryStatisticsSettings(), samplePoints);
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
}
