/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.ml.siddhi.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.Feature;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.factories.DatasetType;
import org.wso2.carbon.ml.core.impl.MLIOFactory;
import org.wso2.carbon.ml.core.impl.Predictor;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

public class ModelHandler {

    public static final String FILE_STORAGE_PREFIX = "file";
    public static final String REGISTRY_STORAGE_PREFIX = "registry";
    public static final String PATH_TO_GOVERNANCE_REGISTRY = "/_system/governance";

    private MLModel mlModel;
    private long modelId;

    /**
     *
     * @param modelStorageLocation MLModel storage location
     * @throws ClassNotFoundException
     * @throws java.net.URISyntaxException
     * @throws MLInputAdapterException
     * @throws java.io.IOException
     */
    public ModelHandler(String modelStorageLocation)
            throws ClassNotFoundException, URISyntaxException, MLInputAdapterException, IOException {
        mlModel = retrieveModel(modelStorageLocation);
    }

    /**
     * Retrieve the MLModel from the storage location.
     * @param modelStorageLocation model storage location (file path or registry path)
     * @return the deserialized MLModel object
     * @throws URISyntaxException
     * @throws MLInputAdapterException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static MLModel retrieveModel(String modelStorageLocation)
            throws URISyntaxException, MLInputAdapterException, IOException, ClassNotFoundException {

        String[] modelStorage = modelStorageLocation.trim().split(":");
        String storageType = modelStorage[0];
        if(storageType.equals(REGISTRY_STORAGE_PREFIX)) {
            if(modelStorage[1].startsWith(PATH_TO_GOVERNANCE_REGISTRY)) {
                modelStorageLocation = modelStorage[1].substring(PATH_TO_GOVERNANCE_REGISTRY.length());
            } else {
                modelStorageLocation = modelStorage[1];
            }
        } else if(storageType.equals(FILE_STORAGE_PREFIX)) {
            modelStorageLocation = modelStorage[1];
            storageType = DatasetType.FILE.getValue();
        } else {
            storageType = DatasetType.FILE.getValue();
        }

        MLIOFactory ioFactory = new MLIOFactory(MLCoreServiceValueHolder.getInstance().getMlProperties());
        MLInputAdapter inputAdapter = ioFactory.getInputAdapter(storageType + MLConstants.IN_SUFFIX);
        InputStream in = inputAdapter.read(modelStorageLocation);
        ObjectInputStream ois = new ObjectInputStream(in);
        MLModel mlModel = (MLModel) ois.readObject();
        ois.close();
        return mlModel;

    }

    /**
     * Predict the value using the feature values.
     * @param data  feature values array
     * @return      predicted value
     * @throws      MLModelHandlerException
     */
    public Object predict(String[] data, String outputType) throws MLModelHandlerException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(data);
        Predictor predictor = new Predictor(modelId, mlModel, list);
        List<?> predictions = predictor.predict();
        String predictionStr = predictions.get(0).toString();
        Object prediction = castValue(outputType, predictionStr);
        return prediction;
    }

    /**
     * Cast the given value to the given output type.
     * @param outputType Output data type
     * @param value value to be casted in String
     * @return Value casted to output type object
     */
    private Object castValue(String outputType, String value) {
        if (outputType.equalsIgnoreCase("double")) {
            return Double.parseDouble(value);
        } else if (outputType.equalsIgnoreCase("float")) {
            return Float.parseFloat(value);
        } else if (outputType.equalsIgnoreCase("integer") || outputType.equalsIgnoreCase("int")) {
            return Integer.parseInt(value);
        } else if (outputType.equalsIgnoreCase("long")) {
            return Long.parseLong(value);
        } else if (outputType.equalsIgnoreCase("boolean") || outputType.equalsIgnoreCase("bool")) {
            return Boolean.parseBoolean(value);
        } else {
            return value;
        }
    }

    /**
     * Return the map containing <feature-name, index> pairs
     * @return the <feature-name, feature-index> map of the MLModel
     */
    public Map<String, Integer> getFeatures() {
        List<Feature> features = mlModel.getFeatures();
        Map<String, Integer> featureIndexMap = new HashMap<String, Integer>();
        for(Feature feature : features) {
            featureIndexMap.put(feature.getName(), feature.getIndex());
        }
        return featureIndexMap;
    }
    
    /**
     * Get new to old indices list of this model.
     * @return the new to old indices list of the MLModel
     */
    public List<Integer> getNewToOldIndicesList() {
        return mlModel.getNewToOldIndicesList();
    }

    /**
     * Return the response variable of the model
     * @return the response variable of the MLModel
     */
    public String getResponseVariable() {
        return mlModel.getResponseVariable();
    }

    /**
     * Returns the algorithm class - classification, numerical prediction or clustering
     * @return the algorithm class
     */
    public String getAlgorithmClass() {
        return mlModel.getAlgorithmClass();
    }
}
