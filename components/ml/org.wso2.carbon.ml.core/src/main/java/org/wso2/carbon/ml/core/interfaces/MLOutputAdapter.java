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

import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;

/**
 * This interface should be implemented, if you need to write an input stream to a given path of a specific data source.
 */
public interface MLOutputAdapter {

    /**
     * Write a given input stream to a given target path.
     * 
     * @param outPath targeted path. eg: /test.txt 
     * @param in {@link InputStream} to be written. This should be closed by this method.
     * @throws MLOutputAdapterException on a write failure.
     */
    void write(String outPath, InputStream in) throws MLOutputAdapterException;
}
