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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.commons.domain.Workflow;

/**
 * Represent configuration objects require to build a model.
 */
public class MLModelConfigurationContext {

    private long modelId;
    private MLModelNew model;
    private Workflow facts;
    private JavaSparkContext sparkContext;
    private JavaRDD<String> lines;
    private String headerRow;
    private String columnSeparator;
    private String[] dataToBePredicted;
    private Map<String,String> summaryStatsOfFeatures;
    /**
     * Encodings list.
     * index - index of the feature. (last index is response variable)
     * value - encodings.
     */
    private List<Map<String, Integer>> encodings;
    /**
     * new to old index mapping for feature set (without response)
     */
    private List<Integer> newToOldIndicesList;
    private int responseIndex;
    
    /**
     * Key - feature index
     * Value - feature name
     */
    private SortedMap<Integer,String> includedFeaturesMap;
    
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
        if (dataToBePredicted == null) {
            this.dataToBePredicted = new String[0];
        } else {
            this.dataToBePredicted = Arrays.copyOf(dataToBePredicted, dataToBePredicted.length);
        }
    }
    public MLModelNew getModel() {
        return model;
    }
    public void setModel(MLModelNew model) {
        this.model = model;
    }
    public Map<String,String> getSummaryStatsOfFeatures() {
        return summaryStatsOfFeatures;
    }
    public void setSummaryStatsOfFeatures(Map<String,String> summaryStatsOfFeatures) {
        this.summaryStatsOfFeatures = summaryStatsOfFeatures;
    }
    public List<Map<String, Integer>> getEncodings() {
        return encodings;
    }
    public void setEncodings(List<Map<String, Integer>> encodings) {
        this.encodings = encodings;
    }
    public List<Integer> getNewToOldIndicesList() {
        return newToOldIndicesList;
    }
    public void setNewToOldIndicesList(List<Integer> oldToNewIndicesList) {
        this.newToOldIndicesList = oldToNewIndicesList;
    }
    public SortedMap<Integer,String> getIncludedFeaturesMap() {
        return includedFeaturesMap;
    }
    public void setIncludedFeaturesMap(SortedMap<Integer,String> includedFeaturesMap) {
        this.includedFeaturesMap = includedFeaturesMap;
    }
    public int getResponseIndex() {
        return responseIndex;
    }
    public void setResponseIndex(int responseIndex) {
        this.responseIndex = responseIndex;
    }
    
    
    
}
