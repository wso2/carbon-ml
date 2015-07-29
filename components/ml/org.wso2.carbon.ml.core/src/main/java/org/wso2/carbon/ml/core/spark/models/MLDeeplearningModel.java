/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.core.spark.models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Wraps a SparkDeeplearningModel object
 * 
 */
public class MLDeeplearningModel implements Externalizable{

    
    private SparkDeeplearningModel model;
    
    public MLDeeplearningModel(){}
    
    public MLDeeplearningModel(SparkDeeplearningModel model){
        this.model = model;
    }
        
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(model);        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {        
        this.model = (SparkDeeplearningModel) in.readObject();        
    }

    /** Get the SparkDeeplearningModel
     * @return the model
     */
    public SparkDeeplearningModel getModel() {
        return model;
    }

    /** Set the SparkDeeplearningModel
     * @param model the model to set
     */
    public void setModel(SparkDeeplearningModel model) {
        this.model = model;
    }

    
}
