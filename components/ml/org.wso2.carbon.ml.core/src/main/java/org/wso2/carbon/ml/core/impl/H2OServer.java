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
import water.H2OClient;
import water.H2OClientApp;

/**
 * H2O server for Deep Learning.
 */
public class H2OServer {

    private static final Log log = LogFactory.getLog(H2OServer.class);
    private static boolean H2OServerStarted = false;

    /**
     * Starts H20 server in local mode
     */
    public static void startH2O() {
        if (!H2OServerStarted) {
            H2OApp.main(new String[0]);
            H2O.waitForCloudSize(1, 10 * 1000 /* ms */);
            H2OServerStarted = true;
        } else {
            log.info("H2O Server is already Running");
        }
    }

    /**
     * Stop H2O
     */
    public static void stopH2O() {
        if (H2OServerStarted) {
            H2O.shutdown(0);
            log.info("H2O Server has shut down");
        } else {
            log.info("H2O Server is not running");
        }
    }

    /**
     * Connects to a remote H2O cloud
     * @param port port of the remote cloud
     * @param ip host IP of the remote cloud
     * @param name name of the remote cloud
     */
    public static void startH2O(String port, String ip, String name) {
        String[] args = new String[6];

        args[0] = "-port";
        args[1] = port;
        args[2] = "-ip";
        args[3] = ip;
        args[4] = "-name";
        args[5] = name;

        H2OClientApp.main(args);
        H2OClient.waitForCloudSize(1, 10 * 1000 /* ms */);
    }

    public static boolean hasH2OServerStarted() {
        return H2OServerStarted;
    }
}