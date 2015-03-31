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

import java.io.File;

import javax.xml.xpath.XPathExpressionException;

import org.testng.Assert;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Instance;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.test.utils.common.HomePageGenerator;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * This is the base class for all Integration tests. Provides functions that are common 
 * to all the integration tests in ML.
 */
public abstract class MLBaseTest {

    protected AutomationContext mlAutomationContext;
    protected Tenant tenantInfo;
    protected User userInfo;
    protected TestUserMode userMode;
    protected Instance instance;

    protected void init() throws MLIntegrationBaseTestException {
        try {
            this.mlAutomationContext = new AutomationContext(MLIntegrationTestConstants.ML_SERVER_NAME, 
                    TestUserMode.SUPER_TENANT_ADMIN);
            //get the current tenant as the userType(TestUserMode)
            this.tenantInfo = this.mlAutomationContext.getContextTenant();
            //get the user information initialized with the system
            this.userInfo = this.tenantInfo.getContextUser();
            this.instance = mlAutomationContext.getInstance();
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("Failed to get the ML automation context: ", e);
        } 
    }
    
    
    /**
     * Get the non-secured URL of a given service.
     * 
     * @param serviceName   Name of the service of which URL is needed.
     * @return              Non-secured URL of the service.
     * @throws              MLIntegrationBaseTestException
     */
    protected String getServiceUrlHttp(String serviceName) throws MLIntegrationBaseTestException {
        String serviceUrl;
        try {
            serviceUrl = this.mlAutomationContext.getContextUrls().getServiceUrl() + "/" + serviceName;
            validateServiceUrl(serviceUrl, this.tenantInfo);
            return serviceUrl;
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the service (http) URL: ", e);
        }
        
    }

    
    /**
     * Get the secured URL of a given service.
     * 
     * @param serviceName   Name of the service of which URL is needed.
     * @return              Secured URL of the service.
     * @throws              MLIntegrationBaseTestException
     */
    protected String getServiceUrlHttps(String serviceName) throws MLIntegrationBaseTestException {
        String serviceUrl;
        try {
            serviceUrl = this.mlAutomationContext.getContextUrls().getSecureServiceUrl() + "/" + serviceName;
            validateServiceUrl(serviceUrl, this.tenantInfo);
            return serviceUrl;
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the secure service (https) "
            		+ "URL: ", e);
        }
    }

    /**
     * Get the URL of the carbon console login
     * 
     * @return  URL of the carbon console login
     * @throws  MLIntegrationBaseTestException
     */
    protected String getCarbonLoginURL() throws MLIntegrationBaseTestException {
        try {
            return HomePageGenerator.getProductHomeURL(this.mlAutomationContext);
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the Carbon login URL", e);
        }
    }

    /**
     * Get the web-app URL
     * 
     * @return  Web-app URL
     * @throws  MLIntegrationBaseTestException
     */
    protected String getMLUiUrl() throws MLIntegrationBaseTestException {
        try {
        	String mlWebAppUrl = UrlGenerationUtil.getWebAppURL(this.tenantInfo, this.mlAutomationContext.getInstance())
        			.split("\\/t\\/")[0] + MLIntegrationTestConstants.ML_UI;
        	return mlWebAppUrl;
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the ML UI URL: ", e);
        }
    }

    /**
     * Checks whether the URl is a valid one for the tenant.
     * 
     * @param serviceUrl    URL to be validated.
     * @param tenant        logged in tenant.
     */
    protected void validateServiceUrl(String serviceUrl, Tenant tenant) {
        // if user mode is null can not validate the service url
        if (this.userMode != null) {
            if (this.userMode == TestUserMode.TENANT_ADMIN || userMode == TestUserMode.TENANT_USER) {
                Assert.assertTrue(serviceUrl.contains("/t/" + tenant.getDomain() + "/"), "invalid service url for"
                		+ " tenant. " + serviceUrl);
            } else {
                Assert.assertFalse(serviceUrl.contains("/t/"), "Invalid service url:" + serviceUrl + " for tenant: " + tenant);
            }
        }
    }
    
    /**
     * Retrieves the absolute path of the model storage directory
     * 
     * @return  Absolute path of the model storage directory
     */
    protected String getModelStorageDirectory() {
        File modelFileStorage = new File(MLIntegrationTestConstants.FILE_STORAGE_LOCATION);
        if (!modelFileStorage.exists() || !modelFileStorage.isDirectory() ) {
            modelFileStorage.mkdirs();
        }
        return modelFileStorage.getAbsolutePath();
    }
}