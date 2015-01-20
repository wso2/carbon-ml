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

package org.wso2.carbon.ml.model.spark.dto;

import org.wso2.carbon.ml.database.dto.ModelSummary;
import org.wso2.carbon.ml.model.internal.constants.MLModelConstants;

import java.io.Serializable;
import java.util.List;

public class ClusterModelSummary implements ModelSummary, Serializable {

    private List<Integer> clusters;

    @Override public String getModelSummaryType() {
        return MLModelConstants.CLUSTER_MODEL_SUMMARY;
    }

    public List<Integer> getClusters() {
        return clusters;
    }

    public void setClusters(List<Integer> clusters) {
        this.clusters = clusters;
    }
}
