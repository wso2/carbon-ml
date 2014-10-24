package org.wso2.carbon.ml.model;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
import org.apache.spark.mllib.optimization.L1Updater;
import org.apache.spark.mllib.optimization.SquaredL2Updater;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class RunLogisticRegression implements Callable<LogisticRegressionModel> {
    private String userResponse;

    RunLogisticRegression(String userResponse){
        this.userResponse = userResponse;
    }

    @Override
    public LogisticRegressionModel call() throws Exception {
        JSONObject json = new JSONObject(userResponse);
        SparkConf conf = new SparkConf().setAppName("LogisticRegression").setMaster("local[4]");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile(json.getString("datasetURL"));
        String headerRow = lines.take(1).get(0);
        String[] headerItems = headerRow.split(json.getString("columnSeparator"));
        String response = json.getString("response");
        Integer responseIndex;
        for (int i=0;i<headerItems.length;i++){
            if (response.equals(headerItems[i])){
                responseIndex = i;
                break;
            }
        }
        Header header = new Header(headerRow);
        JavaRDD<String> data = lines.filter(header);
        Pattern COMMA = Pattern.compile(json.getString("columnSeparator"));
        LineToTokens lineToTokens = new LineToTokens(COMMA);
        JavaRDD<String[]> tokens = data.map(lineToTokens);
        TokensToLabeledPoints tokensToLabeledPoints = new TokensToLabeledPoints(8);
        JavaRDD<LabeledPoint> labeledPoints = tokens.map(tokensToLabeledPoints);
        JavaRDD<LabeledPoint> trainingData = labeledPoints.sample(false,0.7,11L);
        JavaRDD<LabeledPoint> testingData = labeledPoints.subtract(trainingData);
        LogisticRegressionWithSGD lrSGD = new LogisticRegressionWithSGD(json.getDouble
                ("Learning_Rate"),json.getInt("Iterations"),json.getDouble ("Reg_Parameter"),1.0);
        if ("L1".equals(json.getString("Reg_Type"))) {
            lrSGD.optimizer().setUpdater(new L1Updater());
        } else if ("L2".equals(json.getString("Reg_Type"))) {
            lrSGD.optimizer().setUpdater(new SquaredL2Updater());
        }
        lrSGD.setIntercept(true);
        LogisticRegressionModel model = lrSGD.run(trainingData.rdd());
        sc.stop();
        return model;
    }
}
