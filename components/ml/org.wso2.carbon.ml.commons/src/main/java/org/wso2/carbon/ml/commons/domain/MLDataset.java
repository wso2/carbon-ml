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
 * Represent a Data-set in ML.
 */
public class MLDataset {

    private long id;
    private String name;
    private int tenantId;
    private String userName;

    /*
     * Type of the data source i.e. hdfs, file, bam etc.
     */
    private String dataSourceType;
    /*
     * Type of the data target i.e. hdfs, file, bam etc. Where in server side the data-set should be stored?
     */
    private String dataTargetType;
    /*
     * Originated path of the data-set.
     */
    private String sourcePath;
    /*
     * Data type i.e. CSV, TSV
     */
    private String dataType;
    private String comments;
    
    /*
     * Version of the data-set
     */
    private String version;
    
    private boolean containsHeader;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDataTargetType() {
        return dataTargetType;
    }

    public void setDataTargetType(String dataTargetType) {
        this.dataTargetType = dataTargetType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isContainsHeader() {
        return containsHeader;
    }

    public void setContainsHeader(boolean containsHeader) {
        this.containsHeader = containsHeader;
    }

    @Override
    public String toString() {
        return "MLDataset [id=" + id + ", name=" + name + ", tenantId=" + tenantId + ", userName=" + userName
                + ", dataSourceType=" + dataSourceType + ", dataTargetType=" + dataTargetType + ", sourcePath="
                + sourcePath + ", dataType=" + dataType + ", comments=" + comments + ", version=" + version
                + ", containsHeader=" + containsHeader + "]";
    }

}
