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

package org.wso2.carbon.ml.integration.common.utils;

import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;
import org.wso2.carbon.ml.integration.common.utils.mapper.MlUiElementMapper;

/**
 * This is the base class for all UI Integration tests of ML. Provides functions that are common 
 * to all the UI integration tests in ML.
 */
public abstract class MLIntegrationUiBaseTest extends MLIntegrationBaseTest{

    protected MlUiElementMapper mlUIElementMapper;
    protected WebDriver driver;

    protected void init() throws MLIntegrationBaseTestException {
        try {
            super.init();
            this.mlUIElementMapper = MlUiElementMapper.getInstance();
        } catch (IOException e) {
            throw new MLIntegrationBaseTestException("Failed to read from the element mapper: " + e.getMessage(), e);
        }
    }
}