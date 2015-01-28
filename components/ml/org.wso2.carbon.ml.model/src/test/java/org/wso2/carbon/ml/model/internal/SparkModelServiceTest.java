/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.ml.model.internal;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.commons.domain.ClusterPoint;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.ML_ALGORITHMS_CONFIG_XML;

public class SparkModelServiceTest {

    @Test
    public void testGetClusterPoints() throws Exception {
        String mlAlgorithmsConfig = "src/test/resources/ml-algorithms.xml";
        String sparkConfig = "src/test/resources/spark-config.xml";
        SparkModelService sparkModelService = new SparkModelService(mlAlgorithmsConfig,sparkConfig);
        List<String> features = new ArrayList();
        features.add("x");
        features.add("y");
        String datasetURL = "src/test/resources/kMeansTest.csv";
        List<ClusterPoint> clusterPoints = sparkModelService.getClusterPoints(datasetURL, features, 3);
        int cluster1Count = 0;
        for (ClusterPoint clusterPoint : clusterPoints){
            if (clusterPoint.getCluster() == 1)
            {
                cluster1Count+=1;
            }
        }
        Assert.assertEquals(cluster1Count,5);
    }
}
