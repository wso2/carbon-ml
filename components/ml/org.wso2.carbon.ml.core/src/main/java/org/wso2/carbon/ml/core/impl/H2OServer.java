/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.ml.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import water.H2O;
import water.H2OApp;

/**
 *
 * @author Thush
 */
public class H2OServer {

    private static final Log log = LogFactory.getLog(H2OServer.class);
    private static boolean H2OServerStarted = false;

    public static void startH2O() {
        if (!H2OServerStarted) {
            H2OApp.main(new String[0]);
            H2O.waitForCloudSize(1, 10 * 1000 /* ms */);
            H2OServerStarted = true;
        } else {
            log.info("H2O Server is already Running");
        }
    }

    public static boolean hasH2OServerStarted() {
        return H2OServerStarted;
    }
}
