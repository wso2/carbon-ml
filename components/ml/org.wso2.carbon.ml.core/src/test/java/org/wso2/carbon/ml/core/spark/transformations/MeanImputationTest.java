package org.wso2.carbon.ml.core.spark.transformations;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

public class MeanImputationTest {
    HashMap<Integer, Double> meanImputationValues;

    public MeanImputationTest() {
        meanImputationValues = new HashMap<Integer, Double>();
        meanImputationValues.put(0, 1.2);
        meanImputationValues.put(1, 2.2);
        meanImputationValues.put(2, 3.2);
        meanImputationValues.put(3, 4.2);
    }

    @Test
    public void testMeanImputation() throws MLModelBuilderException {
        MeanImputation meanImputation = new MeanImputation.Builder().imputations(meanImputationValues).build();
        String[] meanImputedRow = meanImputation.call(new String[] { "1.2", "2.5", "3.5", "4.3" });
        Assert.assertEquals(meanImputedRow, new String[] { "1.2", "2.5", "3.5", "4.3" });
        meanImputedRow = meanImputation.call(new String[] { "1.2", "?", "3.5", "4.3" });
        Assert.assertEquals(meanImputedRow, new String[] { "1.2", "2.2", "3.5", "4.3" });
        meanImputedRow = meanImputation.call(new String[] { "1.2", "2.5", "NA", "4.3" });
        Assert.assertEquals(meanImputedRow, new String[] { "1.2", "2.5", "3.2", "4.3" });
        meanImputedRow = meanImputation.call(new String[] { "1.2", "2.5", "3.5", "" });
        Assert.assertEquals(meanImputedRow, new String[] { "1.2", "2.5", "3.5", "4.2" });
    }
}
