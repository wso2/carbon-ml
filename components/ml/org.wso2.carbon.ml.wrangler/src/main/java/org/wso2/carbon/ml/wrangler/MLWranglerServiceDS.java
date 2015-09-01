/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.ml.wrangler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;

public class MLWranglerServiceDS {
	private static final Log log = LogFactory.getLog(MLWranglerServiceDS.class);
	private MLWranglerService wranglerService;

	protected void activate(ComponentContext context) {
		try {
			wranglerService = new MLWranglerService();
			context.getBundleContext().registerService(WranglerService.class.getName(), wranglerService, null);

		} catch (Throwable e) {
			log.error("Could not create ModelService: " + e.getMessage(), e);
		}
	}

	protected void deactivate(ComponentContext context) {

	}

	protected void setConfigurationContextService(ConfigurationContextService contextService) {

	}

	protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

	}
}
