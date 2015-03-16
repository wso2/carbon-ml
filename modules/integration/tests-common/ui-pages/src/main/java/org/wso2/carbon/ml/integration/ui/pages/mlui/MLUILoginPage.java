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
import org.openqa.selenium.WebElement;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;

public class MLUILoginPage extends MLUIPage {
    private static final Log logger = LogFactory.getLog(MLUILoginPage.class);

    /**
     * Creates a reference to the login page
     * 
     * @param driver instance of the web driver
     * @throws InvalidPageException
     */
    public MLUILoginPage(WebDriver driver) throws MLUIPageCreationException {
            super(driver);
    }

    /**
     * Provide facility to log into the ml UI using user credentials
     * 
     * @param userName login user name
     * @param password login password
     * @return reference to Home page
     * @throws InvalidPageException
     */
    public MLUIHomePage loginAs(String userName, String password) throws InvalidPageException {
        try {
            logger.info("Login as " + userName);
            WebElement userNameField = driver.findElement(By.xpath(mlUIElementMapper.getElement("login.username")));
            WebElement passwordField = driver.findElement(By.xpath(mlUIElementMapper.getElement("login.password")));
            userNameField.sendKeys(userName);
            passwordField.sendKeys(password);
            driver.findElement(By.xpath(mlUIElementMapper.getElement("login.submit.button"))).click();
            return new MLUIHomePage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create a Home Page", e);
        }
    }
}