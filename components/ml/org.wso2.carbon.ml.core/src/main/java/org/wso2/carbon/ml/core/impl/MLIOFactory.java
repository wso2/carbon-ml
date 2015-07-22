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
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.utils.MLConstants;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

/**
 * MLIOFactory is responsible for generating the concrete classes of the IO Adapter interfaces based on the user
 * configuration.
 */
public class MLIOFactory {
    private static final Log log = LogFactory.getLog(MLIOFactory.class);
    private Properties configuration = new Properties();

    public MLIOFactory(Properties properties) {
        configuration = properties;
    }

    public MLInputAdapter getInputAdapter(String type) {
        Class<?> c;
        String className = configuration.getProperty(type);
        try {
            c = Class.forName(className);
            MLInputAdapter inputAdapter = (MLInputAdapter) c.newInstance();
            return inputAdapter;
        } catch (Exception e) {
            log.warn(String.format(
                    "Failed to load/instantiate the class: %s . Hence, the default implementation %s will be used.",
                    className, FileInputAdapter.class.getName()));
        }
        return new FileInputAdapter();
    }

    public MLOutputAdapter getOutputAdapter(String type) {
        Class<?> c;
        String className = configuration.getProperty(type);
        try {
            c = Class.forName(className);
            MLOutputAdapter outputAdapter = (MLOutputAdapter) c.newInstance();
            return outputAdapter;
        } catch (Exception e) {
            log.warn(String.format(
                    "Failed to load/instantiate the class: %s . Hence, the default implementation %s will be used.",
                    className, FileOutputAdapter.class.getName()));
        }
        return new FileOutputAdapter();
    }

    public String getTargetPath(String fileName) {
        String targetDir = MLCoreServiceValueHolder.getInstance().getDatasetStorage().getStorageDirectory();

        return targetDir + File.separator + fileName;

    }

}
