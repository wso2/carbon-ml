package org.wso2.carbon.ml.model;

import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.testng.annotations.Test;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunLogisticRegressionTest {

    @Test
    public void testLogisticRegression() throws Exception {
        String jsonString= "{\"response\" : \"Cover_Type\"," +
                           "\"algoName\" : \"LOGISTIC_REGRESSION\"," +
                           "\"datasetURL\" : \"src/test/resources/pIndiansDiabetes.csv\"," +
                           "\"columnSeparator\" : \",\"," +
                           "\"Iterations\" : \"100\"," +
                           "\"Reg_Type\" : \"L1\"," +
                           "\"Reg_Parameter\" : \"0.001\"," +
                           "\"Learning_Rate\" : \"0.001\"}";
        RunLogisticRegression runLogisticRegression = new RunLogisticRegression(jsonString);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletionService<LogisticRegressionModel> completionService = new
                ExecutorCompletionService<LogisticRegressionModel>(executorService);
        completionService.submit(runLogisticRegression);
        LogisticRegressionModel model = completionService.take().get();
        System.out.println(model.intercept());

    }

}