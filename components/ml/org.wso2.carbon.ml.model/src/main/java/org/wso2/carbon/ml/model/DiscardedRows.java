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

package org.wso2.carbon.ml.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.function.Function2;

public class DiscardedRows implements Function2<String[], Integer[], Boolean> {
    public static final Log logger = LogFactory.getLog(DiscardedRows.class);

    @Override
    public Boolean call(String[] tokens, Integer[] indices) throws Exception {
        try {
            Boolean keep = true;
            for (Integer index : indices) {
                if (MLModelConstants.EMPTY.equals(tokens[index]) || MLModelConstants.NA.equals
                        (tokens[index])) {
                    keep = false;
                    break;
                }
            }
            return keep;
        } catch (Exception e) {
            String msg = "An error occurred while removing discarded rows\n" + e
                    .getMessage();
            logger.error(msg, e);
            throw new ModelServiceException(msg);
        }
    }
}
