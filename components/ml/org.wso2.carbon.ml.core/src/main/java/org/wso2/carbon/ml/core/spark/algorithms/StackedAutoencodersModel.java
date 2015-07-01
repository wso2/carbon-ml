/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.carbon.ml.core.spark.algorithms;

import hex.Model;
import hex.deeplearning.DeepLearningModel;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.mllib.linalg.Vector;
import water.Key;
import water.fvec.Frame;

/**
 *
 * @author Thush
 */
public class StackedAutoencodersModel implements Serializable{
    
    private static final Log log = LogFactory.getLog(StackedAutoencodersModel.class);
    private transient DeepLearningModel dlModel;
    private transient List<Key> keys;
    
    public void setDeepLearningModelKeys(List<Key> keys){
        this.keys = keys;
    }
    
    public List<Key> getDeepLearningModelKeys(){
        return keys;
    }
    
    public void setDeepLearningModel(DeepLearningModel model){
        this.dlModel = model;        
    }
    public DeepLearningModel getDeepLearningModel(){
        return this.dlModel;
    }
    
    public double predict(Vector input) {      
        double predVal = dlModel.score(input.toArray());
        return predVal;
    }
}
