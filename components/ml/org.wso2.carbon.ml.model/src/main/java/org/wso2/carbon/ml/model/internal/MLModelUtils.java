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

package org.wso2.carbon.ml.model.internal;

import org.wso2.carbon.ml.model.internal.constants.MLModelConstants;
import org.wso2.carbon.ml.model.internal.dto.MLFeature;
import org.wso2.carbon.ml.model.internal.dto.MLWorkflow;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for various model related tasks
 */
public class MLModelUtils {
    /**
     * Private constructor to prevent any other class from instantiating.
     */
    private MLModelUtils() {
        //
    }

    /**
     * @param feature        Feature name
     * @param headerRow       Header row
     * @param columnSeparator Column separator character
     * @return Index of the response variable
     * @throws ModelServiceException
     */
    public static int getFeatureIndex(String feature, String headerRow,
            String columnSeparator) throws
            ModelServiceException {
        try {
            int featureIndex = 0;
            String[] headerItems = headerRow.split(columnSeparator);
            for (int i = 0; i < headerItems.length; i++) {
                if (feature.equals(headerItems[i])) {
                    featureIndex = i;
                    break;
                }
            }
            return featureIndex;
        } catch (Exception e) {
            throw new ModelServiceException(
                    "An error occured while getting response variable index: " + e.getMessage(),e);
        }
    }

    /**
     * @param datasetURL Dataset URL
     * @return Column separator character
     * @throws ModelServiceException
     */
    public static String getColumnSeparator(String datasetURL) throws
            ModelServiceException {
        try {
            if (datasetURL.endsWith(MLModelConstants.CSV)) {
                return ",";
            } else if (datasetURL.endsWith(MLModelConstants.TSV)) {
                return "\t";
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new ModelServiceException(
                    "An error occured while getting column separator: " + e.getMessage(), e);
        }
    }

    /**
     * @param values List of integer values
     * @return Sum of the list of values as a double
     */
    public static Double sum(List<Integer> values) throws ModelServiceException {
        Double sum = 0.0;
        try {
            for (Integer value : values) {
                sum = sum + value;
            }
        } catch (Exception e) {
            throw new ModelServiceException(
                    "An error occured while calculating sum: " + e.getMessage(), e);
        }
        return sum;
    }

    /**
     * @param workflow     Machine learning workflow
     * @param imputeOption Impute option
     * @return Returns indices of features where discard row imputaion is applied
     * @throws ModelServiceException
     */
    public static List<Integer> getImputeFeatureIndices(MLWorkflow workflow, String imputeOption)
            throws ModelServiceException {
        try {
            List<Integer> imputeFeatureIndices = new ArrayList();
            for (MLFeature feature : workflow.getFeatures()) {
                if (feature.getImputeOption().equals(imputeOption)) {
                    imputeFeatureIndices.add(feature.getIndex());
                }
            }
            return imputeFeatureIndices;
        } catch (Exception e) {
            throw new ModelServiceException(
                    "An error occured while retrieving discarder rows feature indices: "
                    + e.getMessage(), e);
        }
    }


}
