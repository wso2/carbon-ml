/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.ml.core.spark.models;

import hex.deeplearning.DeepLearningModel;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.mllib.linalg.Vector;
import water.Key;
import water.fvec.Frame;
import water.serial.ObjectTreeBinarySerializer;
import water.util.FileUtils;

/**
 *
 * @author Thush
 */
public class SparkDeeplearningModel implements Externalizable{
        
    private DeepLearningModel dlModel;
    private String storageLocation;
    
    public void setStorageLocation(String location){
        storageLocation = location;
    }
    /**
     * Set the model
     * @param model model
     */
    public void setDeepLearningModel(DeepLearningModel model){
        this.dlModel = model;        
    }
    
    /**
     * Returns the model
     * @return model
     */
    public DeepLearningModel getDeepLearningModel(){
        return this.dlModel;
    }
    
    /**
     * Predicts the label of a given input
     * @param input input to predict as a vector
     * @return 
     */
    public double predict(Vector input) {      
        double predVal = dlModel.score(input.toArray());
        System.out.println(predVal);
        return predVal;
    }
    
    public double[] predict(Frame inputs) {      
        Frame predVals = dlModel.score(inputs);        
        return predVals.vec(0).toDoubleArray();
    }

    public String getURIStringForLocation(String loc){
        return "file" + loc.substring(1).replace("\\", "/");
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(storageLocation);
        List<Key> keys = new LinkedList<Key>();
        //cannot add published keys, gives nullpointer 
        keys.add(dlModel._key);
        new ObjectTreeBinarySerializer().save(keys, FileUtils.getURI(getURIStringForLocation(storageLocation)));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        storageLocation = (String) in.readObject();
        List<Key> keys = new ObjectTreeBinarySerializer().load(FileUtils.getURI(getURIStringForLocation(storageLocation)));
        this.dlModel = (DeepLearningModel) keys.get(0).get();
    }
}
