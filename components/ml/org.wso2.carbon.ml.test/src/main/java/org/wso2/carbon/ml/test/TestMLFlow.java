/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.test;

import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.SPARK_CONFIG_XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.wso2.carbon.ml.commons.domain.HyperParameter;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.dataset.DatasetService;
import org.wso2.carbon.ml.dataset.exceptions.DatasetServiceException;
import org.wso2.carbon.ml.model.ModelService;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.exceptions.SparkConfigurationParserException;
import org.wso2.carbon.ml.model.internal.MLModelUtils;
import org.wso2.carbon.ml.model.internal.dto.ConfusionMatrix;
import org.wso2.carbon.ml.model.internal.dto.ModelSettings;
import org.wso2.carbon.ml.model.spark.transformations.DoubleArrayToVector;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;
import org.wso2.carbon.ml.project.mgt.exceptions.ProjectManagementServiceException;
import org.wso2.carbon.ml.test.internal.ds.MLTestServiceValueHolder;
import org.apache.spark.mllib.linalg.Vector;

/**
 * Demonstrating end-to-end flow of ML using LOGISTIC REGRESSION.
 */
public class TestMLFlow {
    private static final Log log = LogFactory.getLog(TestMLFlow.class);
    private static final String PROJECT_NAME = "mltest";
    private static final double TRAINING_DATA_FRACTION = 0.7;
    private static final double[] PREDICTING_DATA_SET = new double[] { 5.1, 3.5, 1.4, 0.2 };

    public void testMLFlow() {
        log.info("**********************************************************");
        log.info("Demonstrating end-to-end flow of ML using LOGISTIC REGRESSION.");
        log.info("**********************************************************");
        String datasetPath = System.getProperty("datasetPath");
        if (datasetPath == null) {
            log.info("Please start the ML server with -DdatasetPath=pathToFile and provide the fully qualified file path.");
            return;
        }
        String responseVar = System.getProperty("responseVar");
        if (responseVar == null) {
            log.info("Please start the ML server with -DresponseVar=responseVariable and provide the response variable of the data-set.");
            return;
        }
        String projectName = System.getProperty("projectName");
        if (projectName == null) {
            projectName = PROJECT_NAME;
        }
        DatasetService datasetService = MLTestServiceValueHolder.getDatasetService();
        ProjectManagementService projectMgtService = MLTestServiceValueHolder.getProjectMgtService();
        ModelService modelService = MLTestServiceValueHolder.getModelService();

        File file = new File(datasetPath);
        FileInputStream fis = null;

        try {
            log.info("**********************************************************");
            log.info("********** LogisticRegressionModel Generation  ***********");
            log.info("**********************************************************");
            fis = new FileInputStream(file);
            // create project
            projectMgtService.createProject(projectName, "mltest", "ml test");
            log.info("Created a project : " + projectName);
            projectMgtService.addTenantToProject("-1234", projectName);
            log.info("Add Super Tenant to project : " + projectName);
            // upload dataset
            String datasetURL = datasetService.uploadDataset(fis, file.getName(), projectName);
            log.info("Uploaded the data-set : " + datasetURL);
            datasetService.calculateSummaryStatistics(file.getName(), projectName, projectName);
            log.info("Calculated summary stats of data-set: " + file.getName());
            // create a work-flow
            projectMgtService.createNewWorkflow(projectName, projectName, projectName, projectName);
            log.info("Created a work-flow: " + projectName);
            // set deg
            projectMgtService.setDefaultFeatureSettings(projectName, projectName);
            log.info("Default feature settings are set for project: " + projectName);

            ModelSettings settings = new ModelSettings();
            settings.setWorkflowID(projectName);
            settings.setAlgorithmName("LOGISTIC_REGRESSION");
            settings.setAlgorithmType("Classification");
            settings.setDatasetURL(datasetURL);
            settings.setModelSettingsID(projectName);
            settings.setResponse(responseVar);
            settings.setTrainDataFraction(TRAINING_DATA_FRACTION);
            List<HyperParameter> hyperParams = modelService.getHyperParameters("LOGISTIC_REGRESSION");
            settings.setHyperParameters(hyperParams);

            modelService.insertModelSettings(settings);
            log.info("Inserted model settings: " + settings);

            modelService.buildModel(projectName, projectName);
            log.info("Build model request is submitted for work-flow: " + projectName);

            long t = System.currentTimeMillis();
            while (!modelService.isExecutionStarted(projectName)) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                }
            }
            log.info(String.format("Model execution started in %s milliseconds.", System.currentTimeMillis() - t));
            t = System.currentTimeMillis();
            while (!modelService.isExecutionCompleted(projectName)) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                }
            }
            log.info(String.format("Model execution completed in %s milliseconds.", System.currentTimeMillis() - t));
            ConfusionMatrix confusionMatrix = modelService.getConfusionMatrix(projectName, 0.5);
            log.info("Confusion matrix is generated for the model");
            if (confusionMatrix != null) {
                log.info(confusionMatrix);
            } else {
                log.info("Confusion matrix is null for work-flow id: " + projectName);
            }

            log.info("**********************************************************");
            log.info("*********************    Prediction    *******************");
            log.info("**********************************************************");
            // deserializing
            MLModel mlModel = modelService.getModel(projectName);
            log.info(String.format("Model of project %s is deserialized.", projectName));

            LogisticRegressionModel logisticModel = (LogisticRegressionModel) mlModel.getModel();
            // create a new spark configuration
            SparkConf sparkConf = MLModelUtils.getSparkConf(SPARK_CONFIG_XML);
            sparkConf.setAppName(projectName);
            // create a new java spark context
            JavaSparkContext sc = new JavaSparkContext(sparkConf);
            // 5.1,3.5,1.4,0.2
            List<double[]> data = new ArrayList<double[]>();
            data.add(PREDICTING_DATA_SET);
            JavaRDD<double[]> dataRDD = sc.parallelize(data);
            JavaRDD<Vector> dataVector = dataRDD.map(new DoubleArrayToVector());
            JavaRDD<Double> predictedData = logisticModel.predict(dataVector);
            List<Double> predictedDataList = predictedData.collect();
            for (Double double1 : predictedDataList) {
                log.info("Prediction: " + double1);
            }
        } catch (IOException e) {
            log.error(e);
        } catch (DatasetServiceException e) {
            log.error(e);
        } catch (ProjectManagementServiceException e) {
            log.error(e);
        } catch (ModelServiceException e) {
            log.error(e);
        } catch (SparkConfigurationParserException e) {
            log.error(e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

}
