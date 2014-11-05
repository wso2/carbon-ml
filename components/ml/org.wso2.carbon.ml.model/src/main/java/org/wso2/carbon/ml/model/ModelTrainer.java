package org.wso2.carbon.ml.model;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.Tuple2;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class ModelTrainer implements Callable<String> {

    private String userResponse;

    ModelTrainer(String userResponse){
        this.userResponse = userResponse;
    }

    @Override
    public String call() throws Exception {
        JSONObject userResponse = new JSONObject(this.userResponse);
        if (Algorithm.LOGISTIC_REGRESSION.toString().equals(userResponse.getString("algoName")))
        {
            SparkConf conf = new SparkConf().setAppName(Algorithm.LOGISTIC_REGRESSION.toString()).setMaster
                    ("local[4]");
            JavaSparkContext sc = new JavaSparkContext(conf);
            JavaRDD<String> lines = sc.textFile(userResponse.getString("datasetURL"));
            String headerRow = lines.take(1).get(0);
            String[] headerItems = headerRow.split(userResponse.getString("columnSeparator"));
            String response = userResponse.getString("response");
            Integer responseIndex;
            for (int i=0;i<headerItems.length;i++){
                if (response.equals(headerItems[i])){
                    responseIndex = i;
                    break;
                }
            }
            Header header = new Header(headerRow);
            JavaRDD<String> data = lines.filter(header);
            Pattern pattern = Pattern.compile(userResponse.getString("columnSeparator"));
            LineToTokens lineToTokens = new LineToTokens(pattern);
            JavaRDD<String[]> tokens = data.map(lineToTokens);
            TokensToLabeledPoints tokensToLabeledPoints = new TokensToLabeledPoints(8);
            JavaRDD<LabeledPoint> labeledPoints = tokens.map(tokensToLabeledPoints);
            JavaRDD<LabeledPoint> trainingData = labeledPoints.sample(false,
                                                                      userResponse.getDouble
                                                                              ("trainDataRatio"),
                                                                      11L);
            JavaRDD<LabeledPoint> testingData = labeledPoints.subtract(trainingData);
            LogisticRegression logisticRegression = new LogisticRegression();
            LogisticRegressionModel model = logisticRegression.trainWithSGD(trainingData,
                                                                    userResponse.getDouble("Learning_Rate"),
                                                                    userResponse.getInt("Iterations"),
                                                                    userResponse.getString("Reg_Type"),
                                                                    userResponse.getDouble("Reg_Parameter"),
                                                                    userResponse.getDouble("SGD_Data_Fraction"));
            model.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = logisticRegression.test(model,
                                                                                      testingData);
            BinaryClassificationMetrics metrics = logisticRegression.getEvaluationMetrics
                    (scoresAndLabels);
            LogisticRegressionModelSummary logisticRegressionModelSummary = new
                    LogisticRegressionModelSummary();
            logisticRegressionModelSummary.setUserResponse(userResponse);
            logisticRegressionModelSummary.setAuc(metrics.areaUnderROC());
            List<Tuple2<Object,Object>> rocData = metrics.roc().toJavaRDD().collect();
            JSONArray rocPoints = new JSONArray();
            for (int i=0; i<rocData.size();i+=1000)
            {
                JSONArray point = new JSONArray();
                point.put(rocData.get(i)._1());
                point.put(rocData.get(i)._2());
                rocPoints.put(point);
            }
            logisticRegressionModelSummary.setRoc(rocPoints);
            sc.stop();
        }
        return null;
    }
}
