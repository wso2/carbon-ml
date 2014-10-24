package org.wso2.carbon.ml.model;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;

public class TokensToLabeledPoints implements Function<String[], LabeledPoint> {
    int responseIndex;

    TokensToLabeledPoints(int index) {
        this.responseIndex = index;
    }

    @Override
    public LabeledPoint call(String[] tokens) throws Exception {
        double y = Double.parseDouble(tokens[responseIndex]);
        double[] x = new double[tokens.length];
        for (int i = 0; i < tokens.length; ++i) {
            if (responseIndex != i) {
                x[i] = Double.parseDouble(tokens[i]);
            }
        }
        return new LabeledPoint(y, Vectors.dense(x));
    }
}
