package org.wso2.carbon.ml.core.spark.transformations;

import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DoubleArrayToLabeledPointTest {

  @Test
  public void testTransformation() {
    DoubleArrayToLabeledPoint doubleArrayToLabeledPoint = new DoubleArrayToLabeledPoint();
    LabeledPoint labeledPoint = doubleArrayToLabeledPoint.call(new double[]{1.1,2.0,2.1,3.4,0});
    Assert.assertNotNull(labeledPoint);
    Assert.assertEquals(labeledPoint.label(), 0.0);
    Assert.assertEquals(labeledPoint.features(), Vectors.dense(new double[]{1.1,2.0,2.1,3.4}));
  }
}
