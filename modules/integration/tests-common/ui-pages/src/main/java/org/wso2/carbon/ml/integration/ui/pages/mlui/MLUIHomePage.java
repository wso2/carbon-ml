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
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;

public class MLUIHomePage extends MLUIPage {

    private static final Log logger = LogFactory.getLog(MLUIHomePage.class);

    /**
     * Creates a reference to the home page
     * 
     * @param driver    Web-driver instance
     * @throws          MLUIPageCreationException 
     */
    public MLUIHomePage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }
    
    /**
     * Create a new Project
     * 
     * @return  Data import page
     * @throws  InvalidPageException
     */
    public DataImportPage createProject() throws InvalidPageException {
        try {
            driver.findElement(By.xpath(mlUIElementMapper.getElement("create.new.project"))).click();
            return new DataImportPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create a Data Import Page: ", e);
        }
    }

    /**
     * Logout from the ml UI
     * 
     * @return  Login page
     * @throws  InvalidPageException
     */
    public MLUILoginPage logout() throws InvalidPageException {
        try {
        	driver.findElement(By.xpath(mlUIElementMapper.getElement("logged.in.user"))).click();
            driver.findElement(By.xpath(mlUIElementMapper.getElement("logout"))).click();
            return new MLUILoginPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create a Login Page: ", e);
        }
    }
}

