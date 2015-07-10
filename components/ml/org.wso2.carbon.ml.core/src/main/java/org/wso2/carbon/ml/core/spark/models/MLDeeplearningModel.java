/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.ml.core.spark.models;

import hex.Model;
import hex.deeplearning.DeepLearningModel;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;
import java.util.List;
import water.Key;
import water.serial.ObjectTreeBinarySerializer;
import water.util.FileUtils;

/**
 *
 * @author Thush
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

    /**
     * @return the model
     */
    public SparkDeeplearningModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(SparkDeeplearningModel model) {
        this.model = model;
    }

    
}
