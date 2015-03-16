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

package org.wso2.carbon.ml.integration.ui.test.mlui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationUiBaseTest;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;
import org.wso2.carbon.ml.integration.ui.pages.mlui.MLUIHomePage;
import org.wso2.carbon.ml.integration.ui.pages.mlui.MLUILoginPage;
import org.wso2.carbon.ml.integration.ui.test.exceptions.MLUILoginLogoutTestException;

/**
 * Test case for login and logout of ml UI.
 */
public class MLUILoginLogoutTestCase extends MLIntegrationUiBaseTest {

    private static final Log logger = LogFactory.getLog(MLUILoginLogoutTestCase.class);
    MLUIHomePage mlUIHomePage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getMLUiURL());
    }

    /**
     * Test login to the ml UI using user credentials
     * 
     * @throws MLUILoginLogoutTestException
     */
    @Test(groups = "wso2.ml.ui", description = "verify login to ML UI")
    public void testLoginToMLUI() throws MLUILoginLogoutTestException {
        try {
            MLUILoginPage mlUiLoginPage = new MLUILoginPage(driver);
            // Check whether its the correct page
            Assert.assertTrue(mlUiLoginPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("login.title"))),
                    "This is not the login page.");
            mlUIHomePage = mlUiLoginPage.loginAs(userInfo.getUserName(),userInfo.getPassword());
            //Checks whether it redirects to the home page.
            Assert.assertTrue(mlUIHomePage.isElementPresent(By.xpath(mlUIElementMapper.getElement("create.new.project"))),
                    "Did not redirect to home page.");
        } catch (InvalidPageException e) {
            throw new MLUILoginLogoutTestException("Login to ML UI failed: " + e.getMessage(), e);
        } catch (MLUIPageCreationException e) {
            throw new MLUILoginLogoutTestException("Failed to create a login page: " + e.getMessage(), e);
        }
    }

    /**
     * Test the logout of the ml UI.
     * 
     * @throws MLUILoginLogoutTestException
     */
    @Test(groups = "wso2.ml.ui", description = "verify logut from ML UI", dependsOnMethods = "testLoginToMLUI")
    public void testLogoutFromMLUI() throws MLUILoginLogoutTestException {
        try {
            MLUILoginPage mlUiLoginPage = mlUIHomePage.logout();
            //Checks whether it redirects to the login page after logout.
            Assert.assertTrue(mlUiLoginPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("login.title"))),
                    "Not redirected to login page after logout.");
        } catch (InvalidPageException e) {
            throw new MLUILoginLogoutTestException("Logout from ML UI failed: " + e.getMessage(), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }
}
