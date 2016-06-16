package org.wso2.carbon.ml.core.spark.transformations;

import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.core.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pekasa on 16.06.16.
 */
public class SerializeParameter {
    public static void main(String []args) throws IOException {

        Util serialize = new Util();
        List<Map<String, String>> paramsBaseAlgorithms= getParameters();

        String PARAMETERS_BASE_ALGORITHM = serialize.serializeObjectToString(paramsBaseAlgorithms);
        // how to get parameters of meta algorithm
    }

    public static List<Map<String, String>> getParameters (){
        Map<String, String>  parameters = new HashMap<String, String>();
        parameters.put(MLConstants.IMPURITY , "entropy");
        parameters.put(MLConstants.MAX_DEPTH,"5" );
        parameters.put(MLConstants.MAX_BINS, "32" );
        parameters.put(MLConstants.NUM_CLASSES, "3");
        parameters.put(MLConstants.NUM_TREES, "3");
        parameters.put(MLConstants.FEATURE_SUBSET_STRATEGY, "auto");
        parameters.put(MLConstants.SEED, "12345");
        List<Map<String, String>> parametersList = new ArrayList<Map<String, String>>();
        parametersList.add(parameters);
        parametersList.add(parameters);


        return parametersList ;
    }
}
