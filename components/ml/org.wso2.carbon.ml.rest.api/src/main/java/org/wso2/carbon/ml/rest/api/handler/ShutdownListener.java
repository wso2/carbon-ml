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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.rest.api.handler;

import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This shutdown hook will make the REST API to wait till the resources it used, clean up.
 */
public class ShutdownListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        if (MLCoreServiceValueHolder.getInstance().getThreadExecutor() != null) {
            try {
                MLCoreServiceValueHolder.getInstance().getThreadExecutor()
                        .awaitTermination(1000, TimeUnit.MILLISECONDS);
                MLCoreServiceValueHolder.getInstance().getThreadExecutor().shutdown();
            } catch (InterruptedException ignore) {
            }
        }

        if (MLCoreServiceValueHolder.getInstance().getSparkContext() != null) {
            MLCoreServiceValueHolder.getInstance().getSparkContext().close();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
    }

}
