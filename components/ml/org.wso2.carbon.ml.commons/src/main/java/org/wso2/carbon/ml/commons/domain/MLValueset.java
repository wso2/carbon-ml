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
package org.wso2.carbon.ml.core.domain;

import java.net.URI;

import org.wso2.carbon.ml.commons.domain.SamplePoints;

/**
 * Represent a Value-set in ML.
 */
public class MLValueset {

    private String name;
    private int tenantId;
    
    /*
     * Target server side path of the data-set.
     */
    private URI targetPath;
    private SamplePoints samplePoints;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public URI getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(URI targetPath) {
        this.targetPath = targetPath;
    }

    public SamplePoints getSamplePoints() {
        return samplePoints;
    }

    public void setSamplePoints(SamplePoints samplePoints) {
        this.samplePoints = samplePoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
