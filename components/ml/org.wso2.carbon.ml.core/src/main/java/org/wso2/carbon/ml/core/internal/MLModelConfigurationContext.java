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
package org.wso2.carbon.ml.core.internal;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.wso2.carbon.ml.commons.domain.Workflow;

/**
 * Represent configuration objects require to build a model.
 */
public class MLModelConfigurationContext {

    private long modelId;
    private Workflow facts;
    private JavaSparkContext sparkContext;
    private JavaRDD<String> lines;
    private String headerRow;
    private String columnSeparator;
    private String[] dataToBePredicted;
    
    public long getModelId() {
        return modelId;
    }
    public void setModelId(long modelId) {
        this.modelId = modelId;
    }
    public Workflow getFacts() {
        return facts;
    }
    public void setFacts(Workflow facts) {
        this.facts = facts;
    }
    public JavaSparkContext getSparkContext() {
        return sparkContext;
    }
    public void setSparkContext(JavaSparkContext sparkContext) {
        this.sparkContext = sparkContext;
    }
    public JavaRDD<String> getLines() {
        return lines;
    }
    public void setLines(JavaRDD<String> lines) {
        this.lines = lines;
    }
    public String getHeaderRow() {
        return headerRow;
    }
    public void setHeaderRow(String headerRow) {
        this.headerRow = headerRow;
    }
    public String getColumnSeparator() {
        return columnSeparator;
    }
    public void setColumnSeparator(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }
    public String[] getDataToBePredicted() {
        return dataToBePredicted;
    }
    public void setDataToBePredicted(String[] dataToBePredicted) {
        this.dataToBePredicted = dataToBePredicted;
    }
    
    
    
}
