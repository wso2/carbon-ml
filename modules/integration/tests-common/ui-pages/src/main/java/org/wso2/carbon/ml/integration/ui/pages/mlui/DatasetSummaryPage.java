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

package org.wso2.carbon.ml.integration.ui.pages.mlui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;

public class DatasetSummaryPage extends MLUIPage {
    private static final Log logger = LogFactory.getLog(DatasetSummaryPage.class);

    /**
     * Creates a dataset summary page
     *
     * @param web driver instance
     * @throws MLUIPageCreationException 
     */
    public DatasetSummaryPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }
    
    /**
     * Gets the web driver of the page.
     * @return Web driver instance of the page
     */
    public WebDriver getDriver() {
        return driver;
    }
}