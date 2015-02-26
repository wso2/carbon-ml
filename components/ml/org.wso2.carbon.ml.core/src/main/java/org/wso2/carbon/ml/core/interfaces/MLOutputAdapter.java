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

import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;

/**
 * This interface should be implemented, if you need to store a given data-set.
 */
public interface MLOutputAdapter {

    /**
     * Write a given data-set in a given target path.
     * 
     * @param uri {@link URI} to the targeted path. eg: /test.txt It's up to the implementation to determine the full
     *            connection url.
     * @param in {@link InputStream} to be written. This should not be closed by this method.
     * @throws MLOutputAdapterException on a write failure.
     */
    void writeDataset(URI uri, InputStream in) throws MLOutputAdapterException;
}
