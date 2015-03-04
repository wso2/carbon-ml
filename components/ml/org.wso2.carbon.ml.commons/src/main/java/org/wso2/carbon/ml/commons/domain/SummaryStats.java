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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Represent SummaryStats in ML.
 */
public class SummaryStats {

    // Map containing indices and names of features of the data-set.
    private Map<String, Integer> headerMap;
    // Array containing data-type of each feature in the data-set.
    private String[] type;
    // List containing bin frequencies for each feature.
    private List<SortedMap<?, Integer>> graphFrequencies = new ArrayList<SortedMap<?, Integer>>();
    private int[] missing;
    // Array containing number of unique values of each feature in the data-set.
    private int[] unique;
    // List containing descriptive statistics for each feature.
    private List<DescriptiveStatistics> descriptiveStats = new ArrayList<DescriptiveStatistics>();
    
    public SummaryStats(Map<String, Integer> headerMap, String[] type, List<SortedMap<?, Integer>> graphFrequencies,
            int[] missing, int[] unique, List<DescriptiveStatistics> descriptiveStats) {
        super();
        this.headerMap = headerMap;
        this.type = type;
        this.graphFrequencies = graphFrequencies;
        this.missing = missing;
        this.unique = unique;
        this.descriptiveStats = descriptiveStats;
    }

    public Map<String, Integer> getHeaderMap() {
        return headerMap;
    }

    public String[] getType() {
        return type;
    }

    public List<SortedMap<?, Integer>> getGraphFrequencies() {
        return graphFrequencies;
    }

    public int[] getMissing() {
        return missing;
    }

    public int[] getUnique() {
        return unique;
    }

    public List<DescriptiveStatistics> getDescriptiveStats() {
        return descriptiveStats;
    }

    
}
