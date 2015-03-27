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
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.CarbonUIPageCreationException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;

public class CarbonHomePage extends CarbonPage{

    private static final Log logger = LogFactory.getLog(CarbonHomePage.class);

    public CarbonHomePage(WebDriver driver) throws CarbonUIPageCreationException {
        super(driver);
    }

    /**
     * Logout from the Carbon console.
     * 
     * @return  Reference to Carbon Login Page.
     * @throws  InvalidPageException
     */
    public CarbonLoginPage logout() throws InvalidPageException {
        try {
            driver.findElement(By.xpath(carbonUIElementMapper.getElement("carbon.sign.out.link"))).click();
            return new CarbonLoginPage(driver);
        } catch (CarbonUIPageCreationException e) {
            throw new InvalidPageException("An error occured while creating a Carbon Login Page: " + e.getMessage(), e);
        }
    }
}

