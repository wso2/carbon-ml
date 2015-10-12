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
import java.net.URISyntaxException;
import java.util.*;

import org.apache.commons.lang3.ObjectUtils;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.factories.AlgorithmType;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

public class PredictStreamProcessor extends StreamProcessor {


    private ModelHandler[] modelHandlers;
    private String[] modelStorageLocations;
    private String responseVariable;
    private String algorithmClass;
    private String outputType;
    private boolean attributeSelectionAvailable;
    private Map<Integer, int[]> attributeIndexMap; // <feature-index, [event-array-type][attribute-index]> pairs

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
            StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        while (streamEventChunk.hasNext()) {

            StreamEvent event = streamEventChunk.next();
            String[] featureValues = new String[attributeIndexMap.size()];

            for (Map.Entry<Integer, int[]> entry : attributeIndexMap.entrySet()) {
                int featureIndex = entry.getKey();
                int[] attributeIndexArray = entry.getValue();
                Object dataValue = null;
                switch (attributeIndexArray[2]) {
                    case 0 :
                        dataValue = event.getBeforeWindowData()[attributeIndexArray[3]];
                        break;
                    case 2 :
                        dataValue = event.getOutputData()[attributeIndexArray[3]];
                        break;
                }
                featureValues[featureIndex] = String.valueOf(dataValue);
            }

            if (featureValues != null) {
                try {
                    Object[] predictionResults = new Object[modelHandlers.length];
                    Object predictionResult = null;

                    if (AlgorithmType.CLASSIFICATION.getValue().equals(algorithmClass)) {
                        for (int i = 0; i < modelHandlers.length; i++) {
                            predictionResults[i] = modelHandlers[i].predict(featureValues, outputType);
                        }
                        // Gets the majority vote
                        predictionResult = ObjectUtils.mode(predictionResults);
                    } else if (AlgorithmType.NUMERICAL_PREDICTION.getValue().equals(algorithmClass)) {
                        double sum = 0;
                        for (int i = 0; i < modelHandlers.length; i++) {
                            sum += Double.parseDouble(modelHandlers[i].predict(featureValues, outputType).toString());
                        }
                        // Gets the average value of predictions
                        predictionResult = sum / modelHandlers.length;
                    } else {
                        String msg = String.format(
                                "Error while predicting. Prediction is not supported for the algorithm class %s. ",
                                algorithmClass);
                        throw new ExecutionPlanRuntimeException(msg);
                    }

                    Object[] output = new Object[] { predictionResult };
                    complexEventPopulater.populateComplexEvent(event, output);
                } catch (Exception e) {
                    log.error("Error while predicting", e);
                    throw new ExecutionPlanRuntimeException("Error while predicting", e);
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
            ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

        if(attributeExpressionExecutors.length < 2) {
            throw new ExecutionPlanValidationException("ML model storage locations and response variable type have not "
                    + "been defined as the first two parameters");
        } else if(attributeExpressionExecutors.length == 2) {
            attributeSelectionAvailable = false;    // model-storage-location, data-type
        } else {
            attributeSelectionAvailable = true;  // model-storage-location, data-type, stream-attributes list
        }

        // model-storage-location
        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
            Object constantObj = ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
            String allPaths = (String) constantObj;
            modelStorageLocations = allPaths.split(",");
        } else {
            throw new ExecutionPlanValidationException(
                    "ML model storage-location has not been defined as the first parameter");
        }

        // data-type
        Attribute.Type outputDatatype;
        if (attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor) {
            Object constantObj = ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            outputType = (String) constantObj;
            outputDatatype = getOutputAttributeType(outputType);
        } else {
            throw new ExecutionPlanValidationException(
                    "Response variable type has not been defined as the second parameter");
        }

        modelHandlers = new ModelHandler[modelStorageLocations.length];
        for (int i = 0; i < modelStorageLocations.length; i++) {
            try {
                modelHandlers[i] = new ModelHandler(modelStorageLocations[i]);
            } catch (ClassNotFoundException e) {
                logError(i, e);
            } catch (URISyntaxException e) {
                logError(i, e);
            } catch (MLInputAdapterException e) {
                logError(i, e);
            } catch (IOException e) {
                logError(i, e);
            }
        }

        // Validate response variables
        HashSet<String> responseVariables = new HashSet<String>();
        for (int i = 0; i < modelStorageLocations.length; i++) {
            responseVariables.add(modelHandlers[i].getResponseVariable());
        }
        if (responseVariables.size() > 1) {
            throw new ExecutionPlanCreationException("Response variables of models are not equal");
        }
        responseVariable = modelHandlers[0].getResponseVariable();

        // Validate algorithm classes
        HashSet<String> algorithmClasses = new HashSet<String>();
        for (int i = 0; i < modelHandlers.length; i++) {
            algorithmClasses.add(modelHandlers[i].getAlgorithmClass());
        }
        if (algorithmClasses.size() > 1) {
            throw new ExecutionPlanRuntimeException("Algorithm classes are not equal");
        }
        algorithmClass = modelHandlers[0].getAlgorithmClass();

        // Validate features
        HashSet<Map<String, Integer>> features = new HashSet<Map<String, Integer>>();
        for (int i = 0; i < modelHandlers.length; i++) {
            features.add(modelHandlers[i].getFeatures());
        }
        if (features.size() > 1) {
            throw new ExecutionPlanRuntimeException("Features in models are not equal");
        }

        return Arrays.asList(new Attribute(responseVariable, outputDatatype));
    }

    @Override
    public void start() {
        try {
            populateFeatureAttributeMapping();
        } catch (Exception e) {
            log.error("Error while retrieving ML-models", e);
            throw new ExecutionPlanCreationException("Error while retrieving ML-models" + "\n" + e.getMessage());
        }
    }

    /**
     * Match the attribute index values of stream with feature index value of the model
     * 
     * @throws Exception
     */
    private void populateFeatureAttributeMapping() throws Exception {
        attributeIndexMap = new HashMap<Integer, int[]>();
        Map<String, Integer> featureIndexMap = modelHandlers[0].getFeatures();
        List<Integer> newToOldIndicesList = modelHandlers[0].getNewToOldIndicesList();

        if (attributeSelectionAvailable) {
            for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
                if (expressionExecutor instanceof VariableExpressionExecutor) {
                    VariableExpressionExecutor variable = (VariableExpressionExecutor) expressionExecutor;
                    String variableName = variable.getAttribute().getName();
                    if (featureIndexMap.get(variableName) != null) {
                        int featureIndex = featureIndexMap.get(variableName);
                        int newFeatureIndex = newToOldIndicesList.indexOf(featureIndex);
                        attributeIndexMap.put(newFeatureIndex, variable.getPosition());
                    } else {
                        throw new ExecutionPlanCreationException(
                                "No matching feature name found in the models for the attribute : " + variableName);
                    }
                }
            }
        } else {
            String[] attributeNames = inputDefinition.getAttributeNameArray();
            for (String attributeName : attributeNames) {
                if (featureIndexMap.get(attributeName) != null) {
                    int featureIndex = featureIndexMap.get(attributeName);
                    int newFeatureIndex = newToOldIndicesList.indexOf(featureIndex);
                    int[] attributeIndexArray = new int[4];
                    attributeIndexArray[2] = 2; // get values from output data
                    attributeIndexArray[3] = inputDefinition.getAttributePosition(attributeName);
                    attributeIndexMap.put(newFeatureIndex, attributeIndexArray);
                } else {
                    throw new ExecutionPlanCreationException(
                            "No matching feature name found in the models for the attribute : " + attributeName);
                }
            }
        }
    }

    /**
     * Return the Attribute.Type defined by the data-type argument
     * @param dataType data type of the output attribute
     * @return Attribute.Type object corresponding to the dataType
     */
    private Attribute.Type getOutputAttributeType(String dataType) throws ExecutionPlanValidationException {

        if (dataType.equalsIgnoreCase("double")) {
            return Attribute.Type.DOUBLE;
        } else if (dataType.equalsIgnoreCase("float")) {
            return Attribute.Type.FLOAT;
        } else if (dataType.equalsIgnoreCase("integer") || dataType.equalsIgnoreCase("int")) {
            return Attribute.Type.INT;
        } else if (dataType.equalsIgnoreCase("long")) {
            return Attribute.Type.LONG;
        } else if (dataType.equalsIgnoreCase("string")) {
            return Attribute.Type.STRING;
        } else if (dataType.equalsIgnoreCase("boolean") || dataType.equalsIgnoreCase("bool")) {
            return Attribute.Type.BOOL;
        } else {
            throw new ExecutionPlanValidationException("Invalid data-type defined for response variable.");
        }
    }

    private void logError(int modelId, Exception e) {
        log.error("Error while retrieving ML-model : " + modelStorageLocations[modelId], e);
        throw new ExecutionPlanCreationException(
                "Error while retrieving ML-model : " + modelStorageLocations[modelId] + "\n" + e.getMessage());
    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
