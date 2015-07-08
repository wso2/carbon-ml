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
package org.wso2.carbon.ml.core.spark.models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.spark.mllib.tree.model.DecisionTreeModel;

/**
 * Wraps Spark's {@link DecisionTreeModel} model.
 */
public class MLDecisionTreeModel implements Externalizable {
    private DecisionTreeModel model;

    public MLDecisionTreeModel() {
    }
    
    public MLDecisionTreeModel(DecisionTreeModel model) {
        this.model = model;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeObject(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        model = (DecisionTreeModel) in.readObject();
    }

    public DecisionTreeModel getModel() {
        return model;
    }

    public void setModel(DecisionTreeModel model) {
        this.model = model;
    }

}
