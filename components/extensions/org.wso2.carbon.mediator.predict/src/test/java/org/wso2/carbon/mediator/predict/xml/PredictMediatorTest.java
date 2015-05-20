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

package org.wso2.carbon.mediator.predict.xml;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfigUtils;
import org.wso2.carbon.mediator.predict.PredictMediator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

public class PredictMediatorTest extends TestCase{

    private String xml;

    public PredictMediatorTest() throws URISyntaxException {

        xml =
                "<predict xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                        "        <model storage-location=\"/test-model\"/>\n" +
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


    public void testPredictMediatorFactory() {

        OMElement mediatorElement = SynapseConfigUtils.stringToOM(xml);
        PredictMediatorFactory factory = new PredictMediatorFactory();
        PredictMediator mediator =
                (PredictMediator) factory.createSpecificMediator(mediatorElement, new Properties());

        assertEquals(mediator.getModelStorageLocation(), "/test-model");

        assertEquals(mediator.getFeatureMappings().get("NumPregnancies").getExpression(), "$body/ns:getPrediction/ns:features/ns:NumPregnancies");
        assertEquals(mediator.getFeatureMappings().get("NumPregnancies").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("TSFT").getExpression(), "$body/ns:getPrediction/ns:features/ns:TSFT");
        assertEquals(mediator.getFeatureMappings().get("TSFT").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("DPF").getExpression(), "$body/ns:getPrediction/ns:features/ns:DPF");
        assertEquals(mediator.getFeatureMappings().get("DPF").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("BMI").getExpression(), "$body/ns:getPrediction/ns:features/ns:BMI");
        assertEquals(mediator.getFeatureMappings().get("BMI").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("DBP").getExpression(), "$body/ns:getPrediction/ns:features/ns:DBP");
        assertEquals(mediator.getFeatureMappings().get("DBP").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("PG2").getExpression(), "$body/ns:getPrediction/ns:features/ns:PG2");
        assertEquals(mediator.getFeatureMappings().get("PG2").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("Age").getExpression(), "$body/ns:getPrediction/ns:features/ns:Age");
        assertEquals(mediator.getFeatureMappings().get("Age").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getFeatureMappings().get("SI2").getExpression(), "$body/ns:getPrediction/ns:features/ns:SI2");
        assertEquals(mediator.getFeatureMappings().get("SI2").getNamespaces().get("ns"), "http://ws.apache.org/axis2");

        assertEquals(mediator.getResultPropertyName(), "result");
    }
}
