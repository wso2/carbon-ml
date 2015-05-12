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

package org.wso2.carbon.ml.integration.ui.pages.carbon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.common.utils.mapper.CarbonUiElementMapper;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.CarbonUIPageCreationException;

import java.io.IOException;

/**
 * Base class for various Pages of Carbon management console
 */
public abstract class CarbonPage {
    private static final Log logger = LogFactory.getLog(CarbonPage.class);
    protected WebDriver driver;
    protected CarbonUiElementMapper carbonUIElementMapper;

    /**
     * Creates a dataset summary page
     *
     * @param driver    Instance of the web driver
     * @throws          CarbonUIPageCreationException 
     */
    public CarbonPage(WebDriver driver) throws CarbonUIPageCreationException {
        try {
            this.driver = driver;
            this.carbonUIElementMapper = CarbonUiElementMapper.getInstance();
        } catch (IOException e) {
            throw new CarbonUIPageCreationException("An error occured while retrieving the Carbon UI element mapper: "
            		+ e.getMessage(), e);
        }
    }
    
    /**
     * Gets the web driver of the page.
     * 
     * @return  Web driver instance of the page
     */
    public WebDriver getDriver() {
        return driver;
    }
    
    /**
     * This method check whether the given element is present in the page
     * 
     * @param by    By element to be present
     * @return      Boolean value indicating the existence of the element
     */
    public boolean isElementPresent(By by) {
        try {
            this.driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Count the number of given elements in the page
     * 
     * @param page  Page of which the elements to me search
     * @param by    By element to be count
     * @return      Number of elements
     */
    public int getElementCount(By by) {
        return this.driver.findElements(by).size();
    }
}