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
package org.wso2.carbon.ml.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import water.H2O;
import water.H2OApp;
import water.H2OClientApp;

/**
 * H2O server for Deep Learning.
 */
public class H2OServer {

    private static final Log log = LogFactory.getLog(H2OServer.class);
    private static boolean isH2OServerStarted = false;

    /**
     * Starts H20 server in local mode
     */
    public static void startH2O(String port) {
        if (!isH2OServerStarted) {
            String[] args = new String[2];
            args[0] = "-port";
            args[1] = port;
            H2OApp.main(args);
            H2O.waitForCloudSize(1, 10 * 1000 /* ms */);
            isH2OServerStarted = true;
            log.info("H2o Server has started.");
        } else {
            log.warn("H2O Server is already Running.");
        }
    }

    /**
     * Starts instance in a remote H2O cloud
     * @param ip    IP address of the remote H2O cloud
     * @param port  Port of the remote H2O cloud
     * @param name  Name of the remote H2O cloud
     */
    public static void startH2O(String ip, String port, String name) {
        String[] args = new String[7];
        args[0] = "-port";
        args[1] = port;
        args[2] = "-ip";
        args[3] = ip;
        args[4] = "-name";
        args[5] = name;
        args[6] = "-md5skip";

        H2OClientApp.main(args);
    }

    /**
     * Stop H2O
     */
    public static void stopH2O() {
        if (isH2OServerStarted) {
            H2O.shutdown(0);
            isH2OServerStarted = false;
            log.info("H2O Server has shutdown.");
        } else {
            log.warn("H2O server is not started, hence cannot be stopped");
        }
    }

    public static boolean hasH2OServerStarted() {
        return isH2OServerStarted;
    }
}
