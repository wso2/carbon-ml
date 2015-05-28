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

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.wso2.carbon.ml.mediator.predict.PredictMediator;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class PredictMediatorTest extends TestCase{

    private String xml;

    public PredictMediatorTest() throws URISyntaxException {

        URL resource = PredictMediatorTest.class.getClassLoader().getResource("test-model");
        String modelStorageLocation = new File(resource.toURI()).getAbsolutePath();

        xml =
                "<predict xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                        "        <model storage-location=\""+modelStorageLocation+"\"/>\n" +
                        "        <features>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"NumPregnancies\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:NumPregnancies\"/>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"TSFT\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:TSFT\"/>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"DPF\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:DPF\"/>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"BMI\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:BMI\"/>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"DBP\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:DBP\"/>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"PG2\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:PG2\"/>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"Age\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:Age\"/>\n" +
                        "            <feature xmlns:ns=\"http://ws.apache.org/axis2\"\n" +
                        "                     name=\"SI2\"\n" +
                        "                     expression=\"$body/ns:getPrediction/ns:features/ns:SI2\"/>\n" +
                        "        </features>\n" +
                        "        <predictionOutput property=\"result\"/>\n" +
                        "    </predict>";

    }

    public void testPredictMediatorFactory() throws URISyntaxException {

        OMElement mediatorElement = SynapseConfigUtils.stringToOM(xml);
        PredictMediatorFactory factory = new PredictMediatorFactory();
        PredictMediator mediator =
                (PredictMediator) factory.createSpecificMediator(mediatorElement, new Properties());

        URL resource = PredictMediatorTest.class.getClassLoader().getResource("test-model");
        String modelStorageLocation = new File(resource.toURI()).getAbsolutePath();
        assertEquals(mediator.getModelStorageLocation(), modelStorageLocation);

        assertEquals("$body/ns:getPrediction/ns:features/ns:NumPregnancies", mediator.getFeatureMappings().get("NumPregnancies").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("NumPregnancies").getNamespaces().get("ns"));

        assertEquals("$body/ns:getPrediction/ns:features/ns:TSFT", mediator.getFeatureMappings().get("TSFT").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("TSFT").getNamespaces().get("ns"));

        assertEquals("$body/ns:getPrediction/ns:features/ns:DPF", mediator.getFeatureMappings().get("DPF").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("DPF").getNamespaces().get("ns"));

        assertEquals("$body/ns:getPrediction/ns:features/ns:BMI", mediator.getFeatureMappings().get("BMI").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("BMI").getNamespaces().get("ns"));

        assertEquals("$body/ns:getPrediction/ns:features/ns:DBP", mediator.getFeatureMappings().get("DBP").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("DBP").getNamespaces().get("ns"));

        assertEquals("$body/ns:getPrediction/ns:features/ns:PG2", mediator.getFeatureMappings().get("PG2").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("PG2").getNamespaces().get("ns"));

        assertEquals("$body/ns:getPrediction/ns:features/ns:Age", mediator.getFeatureMappings().get("Age").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("Age").getNamespaces().get("ns"));

        assertEquals("$body/ns:getPrediction/ns:features/ns:SI2", mediator.getFeatureMappings().get("SI2").getExpression());
        assertEquals("http://ws.apache.org/axis2", mediator.getFeatureMappings().get("SI2").getNamespaces().get("ns"));

        assertEquals("result",mediator.getResultPropertyName());
    }

    public void testMediation() throws AxisFault {

        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        valueHolder.setMlProperties(new Properties());

        OMElement mediatorElement = SynapseConfigUtils.stringToOM(xml);
        PredictMediatorFactory factory = new PredictMediatorFactory();
        PredictMediator mediator =
                (PredictMediator) factory.createSpecificMediator(mediatorElement, new Properties());

        SynapseConfiguration synCfg = new SynapseConfiguration();
        AxisConfiguration config = new AxisConfiguration();
        MessageContext messageContext =
                new Axis2MessageContext(new org.apache.axis2.context.MessageContext(), synCfg,
                        new Axis2SynapseEnvironment(new ConfigurationContext(config),synCfg));
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(new ConfigurationContext(config));
        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        messageContext.setEnvelope(envelope);

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2", "ns");
        OMElement parent = fac.createOMElement("getPrediction", omNs);
        OMElement features = fac.createOMElement("features", omNs);
        addFeatureValues(features);
        parent.addChild(features);
        messageContext.getEnvelope().getBody().addChild(parent);
        mediator.mediate(messageContext);
        assertEquals("1", messageContext.getProperty("result"));
    }

    private static void addFeatureValues(OMElement features) {

        String namespace = "http://ws.apache.org/axis2";
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(namespace, "ns");

        OMElement fixedAcidity = fac.createOMElement("NumPregnancies", omNs);
        fixedAcidity.setText("6");
        features.addChild(fixedAcidity);

        OMElement volatileAcidity = fac.createOMElement("TSFT", omNs);
        volatileAcidity.setText("148");
        features.addChild(volatileAcidity);

        OMElement citricAcid = fac.createOMElement("DPF", omNs);
        citricAcid.setText("72");
        features.addChild(citricAcid);

        OMElement residualSugar = fac.createOMElement("BMI", omNs);
        residualSugar.setText("35");
        features.addChild(residualSugar);

        OMElement chlorides = fac.createOMElement("DBP", omNs);
        chlorides.setText("0");
        features.addChild(chlorides);

        OMElement freeSulfurDioxide = fac.createOMElement("PG2", omNs);
        freeSulfurDioxide.setText("33.6");
        features.addChild(freeSulfurDioxide);

        OMElement totalSulfurDioxide = fac.createOMElement("Age", omNs);
        totalSulfurDioxide.setText("0.627");
        features.addChild(totalSulfurDioxide);

        OMElement density = fac.createOMElement("SI2", omNs);
        density.setText("50");
        features.addChild(density);
    }
}
