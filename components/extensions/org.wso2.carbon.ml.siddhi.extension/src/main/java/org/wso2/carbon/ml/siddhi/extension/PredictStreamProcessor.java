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

import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictStreamProcessor extends StreamProcessor {

    private ModelHandler modelHandler;
    private String modelStorageLocation;
    private String responseVariable;
    private String outputType;
    private boolean attributeSelectionAvailable;
    private Map<Integer, int[]> attributeIndexMap;           // <feature-index, [event-array-type][attribute-index]> pairs

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
                    Object predictionResult = modelHandler.predict(featureValues, outputType);
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
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

        if(attributeExpressionExecutors.length < 2) {
            throw new ExecutionPlanValidationException("ML model storage location and response variable type have not " +
                    "been defined as the first two parameters");
        } else if(attributeExpressionExecutors.length == 2) {
            attributeSelectionAvailable = false;    // model-storage-location, data-type
        } else {
            attributeSelectionAvailable = true;  // model-storage-location, data-type, stream-attributes list
        }

        // model-storage-location
        if(attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
            Object constantObj = ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
            modelStorageLocation = (String) constantObj;
        } else {
            throw new ExecutionPlanValidationException("ML model storage-location has not been defined as the first parameter");
        }

        // data-type
        Attribute.Type outputDatatype;
        if(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor) {
            Object constantObj = ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            outputType = (String) constantObj;
            outputDatatype = getOutputAttributeType(outputType);
        } else {
            throw new ExecutionPlanValidationException("Response variable type has not been defined as the second parameter");
        }

        try {
            modelHandler = new ModelHandler(modelStorageLocation);
            responseVariable = modelHandler.getResponseVariable();
        } catch (Exception e) {
            log.error("Error while retrieving ML-model : " + modelStorageLocation, e);
            throw new ExecutionPlanCreationException("Error while retrieving ML-model : " + modelStorageLocation + "\n" + e.getMessage());
        }
        return Arrays.asList(new Attribute(responseVariable, outputDatatype));
    }

    @Override
    public void start() {
        try {
            populateFeatureAttributeMapping();
        } catch (Exception e) {
            log.error("Error while retrieving ML-model : " + modelStorageLocation, e);
            throw new ExecutionPlanCreationException("Error while retrieving ML-model : " + modelStorageLocation + "\n" + e.getMessage());
        }
    }

    /**
     * Match the attribute index values of stream with feature index value of the model
     * @throws Exception
     */
    private void populateFeatureAttributeMapping() throws Exception {
        attributeIndexMap = new HashMap<Integer, int[]>();
        Map<String, Integer> featureIndexMap = modelHandler.getFeatures();
        List<Integer> newToOldIndicesList = modelHandler.getNewToOldIndicesList();

        if(attributeSelectionAvailable) {
            for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
                if(expressionExecutor instanceof VariableExpressionExecutor) {
                    VariableExpressionExecutor variable = (VariableExpressionExecutor) expressionExecutor;
                    String variableName = variable.getAttribute().getName();
                    if (featureIndexMap.get(variableName) != null) {
                        int featureIndex = featureIndexMap.get(variableName);
                        int newFeatureIndex = newToOldIndicesList.indexOf(featureIndex);
                        attributeIndexMap.put(newFeatureIndex, variable.getPosition());
                    } else {
                        throw new ExecutionPlanCreationException("No matching feature name found in the model " +
                                "for the attribute : " + variableName);
                    }
                }
            }
        } else {
            String[] attributeNames = inputDefinition.getAttributeNameArray();
            for(String attributeName : attributeNames) {
                if (featureIndexMap.get(attributeName) != null) {
                    int featureIndex = featureIndexMap.get(attributeName);
                    int newFeatureIndex = newToOldIndicesList.indexOf(featureIndex);
                    int[] attributeIndexArray = new int[4];
                    attributeIndexArray[2] = 2; // get values from output data
                    attributeIndexArray[3] = inputDefinition.getAttributePosition(attributeName);
                    attributeIndexMap.put(newFeatureIndex, attributeIndexArray);
                } else {
                    throw new ExecutionPlanCreationException("No matching feature name found in the model " +
                            "for the attribute : " + attributeName);
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
