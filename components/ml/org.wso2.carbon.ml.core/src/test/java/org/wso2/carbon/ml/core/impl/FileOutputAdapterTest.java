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
import org.testng.annotations.Test;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.impl.FileInputAdapter;

public class FileOutputAdapterTest {
    @Test
    public void testWriteDataset() throws URISyntaxException, MLInputAdapterException {
        MLInputAdapter inputAdapter = new FileInputAdapter();
        String uriString = "src/test/resources/fcSample.csv";
        URI uri;
        String outPath;
        InputStream in = null;

        // test a full path
        uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
        in = inputAdapter.readDataset(uri);
        Assert.assertNotNull(in);
        
        // test stream write
        try {
            File outFile = File.createTempFile("FileOutputAdapterTestOutput", ".csv");
            outPath = outFile.getAbsolutePath();
        } catch (IOException e) {
            outPath = System.getProperty("user.dir") + File.separator + uriString+".out.csv";
        }
        
        MLOutputAdapter outputAdapter = new FileOutputAdapter();
        try {
            outputAdapter.write(outPath, in);
        } catch (MLOutputAdapterException e) {
            // there shouldn't be any exception
            Assert.assertNull(e);
        }
        
        in = null;
        try {
            outputAdapter.write(outPath, in);
        } catch (Exception e) {
            Assert.assertEquals(e instanceof MLOutputAdapterException, true);
        }
        
    }
}
