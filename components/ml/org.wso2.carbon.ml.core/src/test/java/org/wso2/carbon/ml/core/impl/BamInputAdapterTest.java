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

import java.io.InputStream;
import java.net.URI;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;

/**
 * Testing {@link BamInputAdapter}. This test needs a WSO2 BAM up and running with a known table.
 * Then the test can be run by providing the complete url for the table.
 * e.g: mvn clean test -Dbam.table.url="http://localhost:9763/analytics/tables/tableName"
 */
public class BamInputAdapterTest {
    private String bamTableUrl = null;

    @BeforeMethod
    protected void checkEnvironment() {
        bamTableUrl = System.getProperty("bam.table.url");
        if (bamTableUrl == null || bamTableUrl.isEmpty()) {
            throw new SkipException("Skipping tests because WSO2 BAM table is not available.");
        }
    }

    @Test
    public void testReadDataset() {
        BAMInputAdapter bamInputAdaptor = new BAMInputAdapter();
        InputStream inputStream = null;
        try {
            inputStream = bamInputAdaptor.read(new URI(bamTableUrl));
            Assert.assertNotNull(inputStream);
            Assert.assertEquals(inputStream.available() != 0, true);
        } catch (Exception e) {
            Assert.assertEquals(e instanceof MLInputAdapterException, true);
        }
    }
}
