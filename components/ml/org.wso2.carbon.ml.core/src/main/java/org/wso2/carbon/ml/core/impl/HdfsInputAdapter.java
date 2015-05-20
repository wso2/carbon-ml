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
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

/**
 * Implementation of {@link MLInputAdapter} for Hadoop file system.
 */
public class HdfsInputAdapter implements MLInputAdapter {

    /**
     * 
     */
    @Override
    public InputStream read(URI uri) throws MLInputAdapterException {
        try {
            if (!uri.isAbsolute()) {
                String uriString = uri.toString();
                if (!uriString.startsWith("hdfs://")) {
                    if (MLCoreServiceValueHolder.getInstance().getHdfsUrl() != null) {
                        uriString = MLCoreServiceValueHolder.getInstance().getHdfsUrl().concat(uriString);
                    } else {
                        uriString = "hdfs://localhost:9000".concat(uriString);
                    }
                    try {
                        uri = new URI(uriString);
                    } catch (URISyntaxException ignore) {
                    }
                }
            }
            Configuration conf = new Configuration();
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            FileSystem file = FileSystem.get(uri, conf);
            FSDataInputStream inputStream = file.open(new Path(uri));
            return inputStream;
        } catch (Exception e) {
            throw new MLInputAdapterException(String.format("Failed to read the data-set from uri %s: %s", uri, e), e);
        }
    }

}
