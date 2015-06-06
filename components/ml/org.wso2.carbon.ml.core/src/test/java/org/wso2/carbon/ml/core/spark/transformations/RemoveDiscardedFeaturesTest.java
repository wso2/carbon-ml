package org.wso2.carbon.ml.core.spark.transformations;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

public class RemoveDiscardedFeaturesTest {
    @Test
    public void testDiscardedFeatureRemoval() throws MLModelBuilderException {
        List<Integer> newToOldIndicesList = new ArrayList<Integer>();
        newToOldIndicesList.add(2);
        newToOldIndicesList.add(3);
        newToOldIndicesList.add(5);
        newToOldIndicesList.add(7);
        newToOldIndicesList.add(8);
        int responseIndex = 9;

        RemoveDiscardedFeatures removeDiscardedFeatures = new RemoveDiscardedFeatures(newToOldIndicesList,
                responseIndex);
        String[] result = removeDiscardedFeatures.call(new String[] { "2.2", "3.0", "4.2", "1.1", "2.6", "0.7", "1.44",
                "4.5", "7.7", "0" });
        Assert.assertEquals(result.length, 6);
        Assert.assertEquals(result, new String[] { "4.2", "1.1", "0.7", "4.5", "7.7", "0" });
    }

    @Test
    public void testDiscardedFeatureRemovalWhenResponseVariableIsInMiddle() throws MLModelBuilderException {
        List<Integer> newToOldIndicesList = new ArrayList<Integer>();
        newToOldIndicesList.add(2);
        newToOldIndicesList.add(3);
        newToOldIndicesList.add(5);
        newToOldIndicesList.add(7);
        newToOldIndicesList.add(8);
        int responseIndex = 4;

        RemoveDiscardedFeatures removeDiscardedFeatures = new RemoveDiscardedFeatures(newToOldIndicesList,
                responseIndex);
        String[] result = removeDiscardedFeatures.call(new String[] { "2.2", "3.0", "4.2", "1.1", "0", "0.7", "1.44",
                "4.5", "7.7" });
        Assert.assertEquals(result.length, 6);
        Assert.assertEquals(result, new String[] { "4.2", "1.1", "0.7", "4.5", "7.7", "0" });
    }

    @Test
    public void testDiscardedFeatureRemovalWhenResponseVariableIsAbsent() throws MLModelBuilderException {
        List<Integer> newToOldIndicesList = new ArrayList<Integer>();
        newToOldIndicesList.add(2);
        newToOldIndicesList.add(3);
        newToOldIndicesList.add(5);
        newToOldIndicesList.add(7);
        newToOldIndicesList.add(8);
        int responseIndex = -1;

        RemoveDiscardedFeatures removeDiscardedFeatures = new RemoveDiscardedFeatures(newToOldIndicesList,
                responseIndex);
        String[] result = removeDiscardedFeatures.call(new String[] { "2.2", "3.0", "4.2", "1.1", "0.5", "0.7", "1.44",
                "4.5", "7.7", "9.2" });
        Assert.assertEquals(result.length, 5);
        Assert.assertEquals(result, new String[] { "4.2", "1.1", "0.7", "4.5", "7.7" });
    }

}
