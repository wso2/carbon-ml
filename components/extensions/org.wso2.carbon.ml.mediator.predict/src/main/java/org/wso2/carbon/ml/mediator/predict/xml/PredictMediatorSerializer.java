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

package org.wso2.carbon.ml.mediator.predict.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapsePathSerializer;
import org.wso2.carbon.ml.mediator.predict.PredictMediator;
import org.wso2.carbon.ml.mediator.predict.PredictMediatorConstants;

import java.util.Map;

public class PredictMediatorSerializer extends AbstractMediatorSerializer {

    /**
     *  Build the Predict mediator configuration from PredictMediator instance
     *
     *  Predict mediator configuration template
     *  <predict>
     *       <model storage-location="string"/>
     *       <features>
     *           <feature name="string" expression="xpath|json-path"/>+
     *       </features>
     *       <predictionOutput property="string"/>
     *   </predict>
     *
     * @param mediator MLMediator instance
     * @return Predict Mediator configuration OMElement
     */
    @Override
    protected OMElement serializeSpecificMediator(Mediator mediator) {

        assert mediator instanceof PredictMediator : "predict mediator is expected";

        PredictMediator predictMediator = (PredictMediator) mediator;
        OMElement predictElement = fac.createOMElement(PredictMediatorConstants.PREDICT_QNAME.getLocalPart(), synNS);

        // <model>
        OMElement modelConfiguration = fac.createOMElement(PredictMediatorConstants.MODEL_QNAME.getLocalPart(), synNS);
        modelConfiguration.addAttribute(fac.createOMAttribute(PredictMediatorConstants.STORAGE_LOCATION_ATT.getLocalPart(),
                nullNS, predictMediator.getModelStorageLocation()));
        predictElement.addChild(modelConfiguration);

        // <features>
        OMElement features = fac.createOMElement(PredictMediatorConstants.FEATURES_QNAME.getLocalPart(), synNS);
        // <feature>+
        for(Map.Entry<String, SynapsePath> entry : predictMediator.getFeatureMappings().entrySet()) {
            String featureName = entry.getKey();
            SynapsePath expression = entry.getValue();
            OMElement feature = fac.createOMElement(PredictMediatorConstants.FEATURE_QNAME.getLocalPart(), synNS);
            feature.addAttribute(fac.createOMAttribute(PredictMediatorConstants.NAME_ATT.getLocalPart(), nullNS, featureName));
            SynapsePathSerializer.serializePath(expression, feature, PredictMediatorConstants.EXPRESSION_ATT.getLocalPart());
            features.addChild(feature);
        }
        predictElement.addChild(features);

        // <predictionOutput>
        OMElement prediction = fac.createOMElement(PredictMediatorConstants.PREDICTION_OUTPUT_QNAME.getLocalPart(), synNS);
        prediction.addAttribute(fac.createOMAttribute(PredictMediatorConstants.PROPERTY_ATT.getLocalPart(),
                nullNS, predictMediator.getResultPropertyName()));
        predictElement.addChild(prediction);
        return predictElement;
    }

    @Override
    public String getMediatorClassName() {
        return PredictMediator.class.getName();
    }
}
