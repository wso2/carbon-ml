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

import org.apache.spark.mllib.clustering.KMeansModel;

/**
 * Wraps Spark's {@link MLKMeansAnomalyDetectionModel} model.
 */
public class MLKMeansAnomalyDetectionModel implements Externalizable {

    private KMeansModel model;
    private double[][] distancesArray;
    private double[][] distancesArrayTestNormal;
    private double[][] distancesArrayTestAnomaly;

    public MLKMeansAnomalyDetectionModel() {
    }

    public MLKMeansAnomalyDetectionModel(KMeansModel model) {
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

        model = (KMeansModel) in.readObject();
    }

    public KMeansModel getModel() {
        return model;
    }

    public void setModel(KMeansModel model) {
        this.model = model;
    }

    public double[][] getDistancesArray() {
        return distancesArray;
    }

    public void setDistancesArray(double[][] distancesArray){
        this.distancesArray = distancesArray;
    }

    public double[][] getDistancesArrayTestNormal() {
        return distancesArrayTestNormal;
    }

    public void setDistancesArrayTestNormal(double[][] distancesArrayTestNormal){
        this.distancesArrayTestNormal = distancesArrayTestNormal;
    }

    public double[][] getDistancesArrayTestAnomaly() {
        return distancesArrayTestAnomaly;
    }

    public void setDistancesArrayTestAnomaly(double[][] distancesArrayTestAnomaly){
        this.distancesArrayTestAnomaly = distancesArrayTestAnomaly;
    }

}

