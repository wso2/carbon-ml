package org.wso2.carbon.ml.core.spark.transformations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicEncoderTest {
    List<Map<String, Integer>> encodings;

    public BasicEncoderTest() {
        encodings = new ArrayList<Map<String, Integer>>();
        encodings.add(getEncoding(new String[] { "a", "b", "c" }));
        encodings.add(getEncoding(new String[] {}));
        encodings.add(getEncoding(new String[] {}));
        encodings.add(getEncoding(new String[] { "aa", "bb" }));
        encodings.add(getEncoding(new String[] { "aaa", "bbb", "ccc", "ddd" }));
        encodings.add(getEncoding(new String[] {}));
        encodings.add(getEncoding(new String[] { "0", "1" }));
    }

    @Test
    public void testBasicEncoding() {
        BasicEncoder basicEncoder = new BasicEncoder(encodings);
        String[] encodedTokens = basicEncoder.call(new String[] { "b", "2.3", "4.3", "aa", "ddd", "1.2", "0" });
        Assert.assertEquals(encodedTokens, new String[] { "1", "2.3", "4.3", "0", "3", "1.2", "0" });
        encodedTokens = basicEncoder.call(new String[] { "c", "2.3", "4.3", "aa", "ccc", "1.2", "1" });
        Assert.assertEquals(encodedTokens, new String[] { "2", "2.3", "4.3", "0", "2", "1.2", "1" });
    }
    
    @Test
    public void testBasicEncodingWithMissingValues() {
        BasicEncoder basicEncoder = new BasicEncoder(encodings);
        String[] encodedTokens = basicEncoder.call(new String[] { "b", "2.3", "4.3", "aa", "eee", "1.2", "0" });
        Assert.assertEquals(encodedTokens, new String[] { "1", "2.3", "4.3", "0", "eee", "1.2", "0" });
        encodedTokens = basicEncoder.call(new String[] { "d", "2.3", "4.3", "aa", "ccc", "1.2", "1" });
        Assert.assertEquals(encodedTokens, new String[] { "d", "2.3", "4.3", "0", "2", "1.2", "1" });
    }

    @Test
    public void testBasicEncodingWithNullEncodings() {
        BasicEncoder basicEncoder = new BasicEncoder(null);
        String[] encodedTokens = basicEncoder.call(new String[] { "b", "2.3", "4.3", "aa", "ddd", "1.2", "0" });
        Assert.assertEquals(encodedTokens, new String[] { "b", "2.3", "4.3", "aa", "ddd", "1.2", "0" });
        encodedTokens = basicEncoder.call(new String[] { "c", "2.3", "4.3", "aa", "ccc", "1.2", "1" });
        Assert.assertEquals(encodedTokens, new String[] { "c", "2.3", "4.3", "aa", "ccc", "1.2", "1" });
    }

    @Test
    public void testBasicEncodingWithEmptyEncodings() {
        BasicEncoder basicEncoder = new BasicEncoder(new ArrayList<Map<String, Integer>>());
        String[] encodedTokens = basicEncoder.call(new String[] { "b", "2.3", "4.3", "aa", "ddd", "1.2", "0" });
        Assert.assertEquals(encodedTokens, new String[] { "b", "2.3", "4.3", "aa", "ddd", "1.2", "0" });
        encodedTokens = basicEncoder.call(new String[] { "c", "2.3", "4.3", "aa", "ccc", "1.2", "1" });
        Assert.assertEquals(encodedTokens, new String[] { "c", "2.3", "4.3", "aa", "ccc", "1.2", "1" });
    }

    @Test
    public void testBasicEncodingWithNullEncodingMap() {
        encodings.add(null);
        encodings.add(getEncoding(new String[] { "0", "1", "2" }));
        BasicEncoder basicEncoder = new BasicEncoder(encodings);
        String[] encodedTokens = basicEncoder.call(new String[] { "b", "2.3", "4.3", "aa", "ddd", "1.2", "0", "3.1",
                "2" });
        Assert.assertEquals(encodedTokens, new String[] { "1", "2.3", "4.3", "0", "3", "1.2", "0", "3.1", "2" });
        encodedTokens = basicEncoder.call(new String[] { "c", "2.3", "4.3", "aa", "ccc", "1.2", "1", "3.1", "1" });
        Assert.assertEquals(encodedTokens, new String[] { "2", "2.3", "4.3", "0", "2", "1.2", "1", "3.1", "1" });
        // clean up
        encodings.remove(encodings.size()-1);
        encodings.remove(encodings.size()-1);
    }
    
    @Test
    public void testBasicEncodingWithDisorderedValues() {
        encodings.add(getEncoding(new String[] { "4", "0", "2" }));
        BasicEncoder basicEncoder = new BasicEncoder(encodings);
        String[] encodedTokens = basicEncoder.call(new String[] { "b", "2.3", "4.3", "aa", "ddd", "1.2", "0", "4"});
        Assert.assertEquals(encodedTokens, new String[] { "1", "2.3", "4.3", "0", "3", "1.2", "0", "2" });
        encodedTokens = basicEncoder.call(new String[] { "c", "2.3", "4.3", "aa", "ccc", "1.2", "1", "0" });
        Assert.assertEquals(encodedTokens, new String[] { "2", "2.3", "4.3", "0", "2", "1.2", "1", "0" });
        // clean up
        encodings.remove(encodings.size()-1);
    }

    private Map<String, Integer> getEncoding(String[] uniqueVals) {
        Map<String, Integer> encoding = new HashMap<String, Integer>();
        Arrays.sort(uniqueVals);
        for (int i = 0; i < uniqueVals.length; i++) {
            encoding.put(uniqueVals[i], i);
        }
        return encoding;
    }
}
