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
import java.net.URI;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.impl.FileInputAdapter;

/**
 * Testing {@link HdfsInputAdapter} and {@link HdfsOutputAdapter}. This test needs HDFS/Hadoop to be running. You have
 * to run the tests like: mvn clean test -Dhdfs.available="true" Optionally, you can provide a hdfs url via
 * -Dhdfs.url="hdfs://localhost:9000". Default HDFS url will be hdfs://localhost:9000.
 */
public class HdfsInputAndOutputAdapterTest {
    private String hdfsUrl = "hdfs://localhost:9000";

    @BeforeMethod
    protected void checkEnvironment() {
        boolean isHdfsAvailable = Boolean.parseBoolean(System.getProperty("hdfs.available"));
        if (!isHdfsAvailable) {
            throw new SkipException("Skipping tests because HDFS is not available.");
        } else {
            if (System.getProperty("hdfs.url") != null) {
                hdfsUrl = System.getProperty("hdfs.url");
            }
        }
    }

    @Test
    public void testReadDataset() throws URISyntaxException, MLInputAdapterException, IOException {
        MLInputAdapter fileInAdapter = new FileInputAdapter();
        String filePrefix = "file://";
        String uriString = "src/test/resources/fcSample.csv";
        URI uri;
        InputStream in = null;

        // read a local file and get an input stream
        uri = new URI(filePrefix + System.getProperty("user.dir") + File.separator + uriString);
        in = fileInAdapter.read(uri);
        Assert.assertNotNull(in);

        // write it to hdfs
        MLOutputAdapter hdfsOutAdapter = new HdfsOutputAdapter();
        String outPath = hdfsUrl + "/ml-out5/fcSample.csv";
        try {
            hdfsOutAdapter.write(outPath, in);
        } catch (MLOutputAdapterException e1) {
            Assert.assertNull(e1);
        }

        // read back from hdfs
        MLInputAdapter hdfsInAdapter = new HdfsInputAdapter();
        try {
            in = hdfsInAdapter.read(URI.create(outPath));
            Assert.assertNotNull(in);
            Assert.assertEquals(in.available() != 0, true);

        } catch (MLInputAdapterException e1) {
            Assert.assertNull(e1);
        }
    }
}
