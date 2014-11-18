package org.wso2.carbon.ml.model;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

public class LineToTokensTest {

    @Test
    public void testCall() throws Exception {
        SparkConf conf = new SparkConf().setAppName("testLineToTokens").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("src/test/resources/pIndiansDiabetes.csv");
        Pattern pattern = Pattern.compile(",");
        LineToTokens lineToTokens = new LineToTokens(pattern);
        JavaRDD<String[]> tokens = lines.map(lineToTokens);
        Assert.assertEquals(lines.count(),tokens.count(),"Line count doesn't match the tokens " +
                                                         "count");
        sc.stop();
    }
}