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

package org.wso2.carbon.ml.mediator.predict;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.mediators.AbstractMediator;
import org.jaxen.JaxenException;
import org.wso2.carbon.ml.mediator.predict.util.ModelHandler;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class PredictMediator extends AbstractMediator {

    private String resultPropertyName;
    private Map<String, SynapsePath> featureMappings;
    private String modelStorageLocation;
    private boolean isUpdated;

    public PredictMediator() {
        featureMappings = new HashMap<String, SynapsePath>();
        isUpdated = false;
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog synLog = getLog(messageContext);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : predict mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }
        String prediction = getPredictionFromModel(messageContext);
        messageContext.setProperty(resultPropertyName, prediction);

        synLog.traceOrDebug("End : predict mediator");
        this.isUpdated = false;
        return true;
    }

    /**
     * Extract the relevant element values from the messageContext
     * Map the actual parameter values with the model variables
     * @param messageContext MessageContext of the mediating message
     * @return the string value of the prediction
     */
    private String getPredictionFromModel(MessageContext messageContext) {

        try {
            String prediction = ModelHandler.getInstance(modelStorageLocation, featureMappings, isUpdated).getPrediction(messageContext);
            return prediction;
        } catch (JaxenException e) {
            handleException("Error while extracting feature values ", e, messageContext);
        } catch (MLModelHandlerException e) {
            handleException("Error while predicting value from the model ", e, messageContext);
        } catch (MLModelBuilderException e) {
            handleException("Error while building the Model ", e, messageContext);
        } catch (ClassNotFoundException e) {
            handleException("Error while building the Model ", e, messageContext);
        } catch (IOException e) {
            handleException("Error while retrieving the Model ", e, messageContext);
        } catch (MLInputAdapterException e) {
            handleException("Error while retrieving the Model ", e, messageContext);
        } catch (URISyntaxException e) {
            handleException("Error while retrieving the Model ", e, messageContext);
        }
        return null;
    }

    /**
     * Set the message context property name
     * @param propertyName message context property name
     */
    public void setResultPropertyName(String propertyName) {
        this.isUpdated = true;
        this.resultPropertyName = propertyName;
    }

    /**
     * Add feature mapping
     * @param featureName feature name
     * @param synapsePath synapse path to extract the feature value
     */
    public void addFeatureMapping(String featureName, SynapsePath synapsePath) {
        this.isUpdated = true;
        featureMappings.put(featureName, synapsePath);
    }

    /**
     * Get the property name to which the prediction value is set
     * @return the message context property name to which the prediction value is set
     */
    public String getResultPropertyName() {
        return resultPropertyName;
    }

    /**
     * Get the feature mappings map
     * @return the map containing <feature-name, synapse-path> pairs
     */
    public Map<String, SynapsePath> getFeatureMappings() {
        return featureMappings;
    }

    /**
     * Set model storage location
     * @param modelStorageLocation Path of the MLModel file
     */
    public void setModelStorageLocation(String modelStorageLocation) {
        this.isUpdated = true;
        this.modelStorageLocation = modelStorageLocation;
    }

    /**
     * Get model storage location
     * @return the Path to MLModel file
     */
    public String getModelStorageLocation() {
        return modelStorageLocation;
    }
}
