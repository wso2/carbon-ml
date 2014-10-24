package org.wso2.carbon.ml.model;

import org.apache.spark.api.java.function.Function2;

public class DiscardedRows implements Function2<String[], Integer[], Boolean> {
    @Override
    public Boolean call(String[] tokens, Integer[] indices) throws Exception {
        Boolean keep = true;
        for (Integer index : indices) {
            if ("".equals(tokens[index]) || "NA".equals(tokens[index])) {
                keep = false;
                break;
            }
        }
        return keep;
    }
}
