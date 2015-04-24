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

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Registry adapter for ML. Responsible for writing a given input stream to a governance registry
 */

public class RegistryOutputAdapter implements MLOutputAdapter {

    @Override
    public URI write(String outPath, InputStream in) throws MLOutputAdapterException {

        if (in == null || outPath == null) {
            throw new MLOutputAdapterException(String.format(
                    "Null argument values detected. Input stream: %s Out Path: %s", in, outPath));
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(in, byteArrayOutputStream);
            byte[] array = byteArrayOutputStream.toByteArray();

            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            Registry registry = carbonContext.getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            Resource resource = registry.newResource();
            resource.setContent(array);
            registry.put(outPath, resource);
            return URI.create(outPath);

        } catch (RegistryException e) {
            throw new MLOutputAdapterException(String.format("Failed to save the model to registry %s: %s", outPath, e), e);
        } catch (IOException e) {
            throw new MLOutputAdapterException(String.format("Failed to read the model to registry %s: %s", outPath, e), e);
        }
    }
}
