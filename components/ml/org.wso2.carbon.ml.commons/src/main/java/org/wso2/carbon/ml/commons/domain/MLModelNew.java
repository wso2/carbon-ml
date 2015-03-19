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
package org.wso2.carbon.ml.commons.domain;

/**
 * Represent a Model in ML.
 */
public class MLModelNew {

    private long id;
    private String name;
    private int tenantId;
    private String userName;
    private String createdTime;
    private long analysisId;
    private long versionSetId;
    private String storageType;
    private String storageDirectory;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getStorageDirectory() {
        return storageDirectory;
    }

    public void setStorageDirectory(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(long analysisId) {
        this.analysisId = analysisId;
    }

    public long getValueSetId() {
        return versionSetId;
    }

    public void setValueSetId(long valueSetId) {
        this.versionSetId = valueSetId;
    }

    @Override
    public String toString() {
        return "MLModelNew [id=" + id + ", name=" + name + ", tenantId=" + tenantId + ", userName=" + userName
                + ", createdTime=" + createdTime + ", analysisId=" + analysisId + ", valueSetId=" + versionSetId
                + ", storageType=" + storageType + ", storageDirectory=" + storageDirectory + "]";
    }

}
