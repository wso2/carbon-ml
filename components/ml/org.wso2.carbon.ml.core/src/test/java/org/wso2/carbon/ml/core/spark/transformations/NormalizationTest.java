package org.wso2.carbon.ml.core.spark.transformations;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

public class NormalizationTest {

    private final List<Double> max;
    private final List<Double> min;

    public NormalizationTest() {
        this.max = new ArrayList<Double>();
        this.min = new ArrayList<Double>();
        
        max.add(10.0);
        max.add(5.0);
        max.add(50.0);
        max.add(1.0);

        min.add(2.0);
        min.add(0.0);
        min.add(10.0);
        min.add(1.0);
    }

    @Test
    public void testNormalization() throws MLModelBuilderException {

        Normalization normalization = new Normalization.Builder().minMax(max, min).build();
        double[] normalizedRow = normalization.call(new double[] { 1.0, 7.0, 30.0, 1.0 });
        Assert.assertEquals(normalizedRow, new double[] { 0.0, 1.0, 0.5, 0.5 });
        normalizedRow = normalization.call(new double[] { 5.0, 3.0, 20.0, 2.0 });
        Assert.assertEquals(normalizedRow, new double[] { 0.375, 0.6, 0.25, 1.0 });
        normalizedRow = normalization.call(new double[] { 9.0, -5.0, 8.0, 0.0 });
        Assert.assertEquals(normalizedRow, new double[] { 0.875, 0.0, 0.0, 0.0 });
        normalizedRow = normalization.call(new double[] { 10.0, 6.0, 45.0, 3.0 });
        Assert.assertEquals(normalizedRow, new double[] { 1.0, 1.0, 0.875, 1.0 });
    }
}
