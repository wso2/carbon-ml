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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;

/**
 * Implementation of {@link MLInputAdapter} for regular file system.
 */
public class FileInputAdapter implements MLInputAdapter {

    @Override
    public InputStream read(String path) throws MLInputAdapterException {
        try {
            FileInputStream inputStream = new FileInputStream(new File(path));
            return inputStream;
        } catch (FileNotFoundException e) {
            throw new MLInputAdapterException(String.format("Failed to read the data-set from uri %s: %s", path,e), e);
        } catch (IllegalArgumentException e) {
            throw new MLInputAdapterException(String.format("Failed to read the data-set from uri %s: %s", path,e), e);
        }
    }

}
