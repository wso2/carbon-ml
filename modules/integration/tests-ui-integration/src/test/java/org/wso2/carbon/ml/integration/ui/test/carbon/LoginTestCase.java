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

package org.wso2.carbon.ml.integration.ui.test.carbon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.ml.integration.ui.pages.carbon.CarbonHomePage;
import org.wso2.carbon.ml.integration.ui.pages.carbon.CarbonLoginPage;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationUiBaseTest;

/**
 * Contains test case for Login in to Carbon Management console
 */
public class LoginTestCase extends MLIntegrationUiBaseTest {

    private static final Log logger = LogFactory.getLog(LoginTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getCarbonLoginURL());
    }

    @Test(groups = "wso2.ml.ui", description = "verify login to ml server")
    public void testLogin() throws Exception {
        CarbonLoginPage carbonLogin = new CarbonLoginPage(driver);
        CarbonHomePage carbonHome = carbonLogin.loginAs(userInfo.getUserName(),userInfo.getPassword());
        carbonHome.logout();
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}