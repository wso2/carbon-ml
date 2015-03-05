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

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;

public class MLIOFactoryTest {
    private MLIOFactory ioFactory;

    @BeforeClass
    public void beforeClass() {
        Properties configuration = new Properties();
        configuration.put("data.hdfs.input", "org.wso2.carbon.ml.core.impl.HdfsInputAdapter");
        configuration.put("data.hdfs.output", "org.wso2.carbon.ml.core.impl.HdfsOutputAdapter");
        configuration.put("target.home", "/tmp");
        ioFactory = new MLIOFactory(configuration);
    }

    @Test
    public void getInputAdapter() {
        MLInputAdapter inputAdapter = ioFactory.getInputAdapter("data.hdfs.input");
        Assert.assertEquals(inputAdapter instanceof HdfsInputAdapter, true);

        // default input adapter is file input adapter
        inputAdapter = ioFactory.getInputAdapter("hdfs");
        Assert.assertEquals(inputAdapter instanceof FileInputAdapter, true);

    }

    @Test
    public void getOutputAdapter() {
        MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter("data.hdfs.output");
        Assert.assertEquals(outputAdapter instanceof HdfsOutputAdapter, true);
        
        // default output adapter is file output adapter
        outputAdapter = ioFactory.getOutputAdapter("hdfs");
        Assert.assertEquals(outputAdapter instanceof FileOutputAdapter, true);
    }

    @Test
    public void getTargetPath() {
        String path = ioFactory.getTargetPath("test-ml.csv");
        Assert.assertEquals(path, "/tmp/test-ml.csv");
    }
}
