package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.model.SparkConfigurationParser;
import org.wso2.carbon.ml.model.constants.MLModelConstants;
import org.wso2.carbon.ml.model.dto.LogisticRegressionModelSummary;
import org.wso2.carbon.ml.model.spark.transformations.Header;
import org.wso2.carbon.ml.model.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.model.spark.transformations.TokensToLabeledPoints;

import java.util.regex.Pattern;

import static org.testng.Assert.*;

public class SupervisedModelTest {

    @Test
    public void testRunSpark() throws Exception {
        String workflowJSON = "{\"responseVariable\" : \"Class\"," +
                              "\"algorithmName\" : \"LOGISTIC_REGRESSION\"," +
                              "\"datasetURL\" : \"src/test/resources/pIndiansDiabetes.csv\"," +
                              "\"algorithmType\" : \"Classification\"," +
                              "\"workflowID\" : \"00001\"," +
                              "\"modelID\" : \"00001\"," +
                              "\"modelSettingsID\" : \"00001\"," +
                              "\"Iterations\" : \"100\"," +
                              "\"Reg_Type\" : \"L1\"," +
                              "\"Reg_Parameter\" : \"0.001\"," +
                              "\"SGD_Data_Fraction\" : \"1\"," +
                              "\"trainDataFraction\" : \"0.7\"," +
                              "\"Learning_Rate\" : \"0.001\"}";
        JSONObject workflow = new JSONObject(workflowJSON);
        SparkConfigurationParser sparkConfigurationParser = new SparkConfigurationParser(
                "src/test/resources/spark-config.xml");
        SupervisedModel supervisedModel = new SupervisedModel();
        SparkConf sparkConf = sparkConfigurationParser.getSparkConf();
        supervisedModel.buildModel(workflow,sparkConf);
    }
}