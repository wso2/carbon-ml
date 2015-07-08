/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.database.util;

import org.wso2.carbon.ml.commons.domain.SamplePoints;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class MLDBUtil {

    /**
     * Deserialize SamplePoints object InputStream
     *
     * @param data
     * @return
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static SamplePoints getSamplePointsFromInputStream(InputStream data) throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(data);
        SamplePoints samplePoints = (SamplePoints) is.readObject();
        is.close();
        return samplePoints;
    }
}
