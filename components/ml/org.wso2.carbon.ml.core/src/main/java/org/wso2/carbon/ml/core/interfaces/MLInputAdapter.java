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
package org.wso2.carbon.ml.core.interfaces;

import java.io.InputStream;
import java.net.URI;

import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;

/**
 * This interface should be implemented, if you need to read data from a given data-set.
 */
public interface MLInputAdapter {

    /**
     * Read a given data-set.
     * 
     * @param uri {@link URI} to the data-set. eg: hdfs://localhost:9000/test.txt, file:///home/wso2/test.txt
     * @return the given data-set as an {@link InputStream}
     * @throws MLInputAdapterException on a read failure.
     */
    InputStream readDataset(URI uri) throws MLInputAdapterException;
}
