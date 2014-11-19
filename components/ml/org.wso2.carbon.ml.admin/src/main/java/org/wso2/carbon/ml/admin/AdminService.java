/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="mlAdminService" immediate="true"
 */
public class AdminService {
    private static final Log logger = LogFactory.getLog(AdminService.class);


    protected void activate(ComponentContext context) {
        try {
            AdminService adminService = new AdminService();
            context.getBundleContext().registerService(AdminService.class.getName(),
                                                       adminService, null);
            logger.info("ML Admin Service Started");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext context) {
        logger.info("ML Admin Service Stopped");
    }


    public DataUploadSettings getDataUploadSettings() throws AdminServiceException {
        try {
           MLConfigurationParser mlConfigurationParser = new MLConfigurationParser();
            return mlConfigurationParser.getDataUploadSettings();
        } catch (Exception ex) {
            String msg = "An error occurred while reading dataset upload settings";
            logger.error(msg, ex);
            throw new AdminServiceException(msg);
        }
    }
}
