/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.ml.rest.api.neuralNetworks;

/**
 * This class is to handle Hidden Layer details of the Neural Network.
 */
public class HiddenLayerDetails {
    int hiddenNodes;
    String weightInit;
    String activationAlgo;

    /**
     * Constructor of the HiddenLayerDetails class.
     *
     */
    public HiddenLayerDetails(int hiddenNodes, String weightInit, String activationAlgo) {
        this.hiddenNodes = hiddenNodes;
        this.weightInit = weightInit;
        this.activationAlgo = activationAlgo;
    }
}
