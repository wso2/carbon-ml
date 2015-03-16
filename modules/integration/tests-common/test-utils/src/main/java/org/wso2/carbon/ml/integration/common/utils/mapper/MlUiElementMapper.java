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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.integration.common.utils.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MlUiElementMapper {
    public static final Properties uiProperties = new Properties();
    private static final Log logger = LogFactory.getLog(MlUiElementMapper.class);
    private static MlUiElementMapper instance;

    private MlUiElementMapper() {
    }

    public static MlUiElementMapper getInstance() throws IOException {
        if (instance == null) {
            synchronized (MlUiElementMapper.class) {
                if (instance == null) {
                    setStream();
                    instance = new MlUiElementMapper();
                }
            }
        }
        return instance;
    }

    public static Properties setStream() throws IOException {
        InputStream inputStream = MlUiElementMapper.class.getResourceAsStream(MLIntegrationTestConstants
                .ML_UI_ELEMENT_MAPPER);
        if (inputStream.available() > 0) {
            uiProperties.load(inputStream);
            inputStream.close();
            return uiProperties;
        }
        return null;
    }

    public String getElement(String key) {
        if (uiProperties != null) {
            return uiProperties.getProperty(key);
        }
        return null;
    }
}
