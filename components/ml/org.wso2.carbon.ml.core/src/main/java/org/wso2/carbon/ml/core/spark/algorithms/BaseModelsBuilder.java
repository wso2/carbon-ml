package org.wso2.carbon.ml.core.spark.algorithms;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.models.MLClassificationModel;
import org.wso2.carbon.ml.core.spark.models.MLDecisionTreeModel;
import org.wso2.carbon.ml.core.spark.models.MLRandomForestModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pekasa on 07.06.16.
 */
public class BaseModelsBuilder {

    /**
     * create a switch statement for methods which call sparkmllibrary libraries and return models of MLModel type
     *
     * @param algorithmName       Name of algorithm to build
     * @param trainingData        Training dataset
     * @param algorithmParameters Hyperparameters of algorithm
     */


    public MLModel buildBaseModels(MLModelConfigurationContext context, Workflow workflow, String algorithmName, JavaRDD<LabeledPoint> trainingData,
                                   Map<String, String> algorithmParameters, Boolean isMetaAlgorithm) throws MLModelBuilderException {


        try {
            Map<Integer, Integer> categoricalFeatureInfo;

            MLModel mlModel = new MLModel();
            mlModel.setAlgorithmName(algorithmName);
            mlModel.setAlgorithmClass(workflow.getAlgorithmClass());
            mlModel.setEncodings(context.getEncodings());
            mlModel.setFeatures(workflow.getIncludedFeatures());
            mlModel.setResponseVariable(workflow.getResponseVariable());
            mlModel.setNewToOldIndicesList(context.getNewToOldIndicesList());
            mlModel.setResponseIndex(context.getResponseIndex());

            if (isMetaAlgorithm) {
                categoricalFeatureInfo = getCategoricalFeatureInfo(getMetaModelEncodings());
                } else {
                categoricalFeatureInfo = getCategoricalFeatureInfo(context.getEncodings());

            }


            // build a machine learning model according to user selected algorithm

            MLConstants.SUPERVISED_ALGORITHM supervisedAlgorithm = MLConstants.SUPERVISED_ALGORITHM.valueOf(algorithmName);
            switch (supervisedAlgorithm) {

                case DECISION_TREE:
                    mlModel = buildDecisionTreeModel(trainingData, workflow,
                            mlModel, categoricalFeatureInfo, algorithmParameters);
                    break;
                case RANDOM_FOREST_CLASSIFICATION:
                    mlModel = buildRandomForestClassificationModel(trainingData, workflow,
                            mlModel, categoricalFeatureInfo, algorithmParameters);
                    break;
                case NAIVE_BAYES:
                    mlModel = buildNaiveBayesModel(trainingData, workflow, mlModel, algorithmParameters);
                    break;


                default:
                    throw new AlgorithmNameException("Incorrect algorithm name");
            }

            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building baseModel: " + e.getMessage(), e);
        }
    }


    /**
     * This method builds a decision tree model
     *
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param workflow     Machine learning workflow
     * @param mlModel      Deployable machine learning model
     * @throws MLModelBuilderException
     */
    private MLModel buildDecisionTreeModel(
            JavaRDD<LabeledPoint> trainingData, Workflow workflow, MLModel mlModel,
            Map<Integer, Integer> categoricalFeatureInfo, Map<String, String> algorithmParameters)
            throws MLModelBuilderException {
        try {
            DecisionTree decisionTree = new DecisionTree();
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData,
                    getNoOfClasses(mlModel),
                    categoricalFeatureInfo, algorithmParameters.get(MLConstants.IMPURITY),
                    Integer.parseInt(algorithmParameters.get(MLConstants.MAX_DEPTH)),
                    Integer.parseInt(algorithmParameters.get(MLConstants.MAX_BINS)));

            // remove from cache
            trainingData.unpersist();
            // add test data to cache

            mlModel.setModel(new MLDecisionTreeModel(decisionTreeModel));


            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException(
                    "An error occurred while building decision tree model: " + e.getMessage(), e);
        }

    }

    private MLModel buildRandomForestClassificationModel(JavaRDD<LabeledPoint> trainingData, Workflow workflow, MLModel mlModel,
                                                         Map<Integer, Integer> categoricalFeatureInfo, Map<String, String> algorithmParameters) throws MLModelBuilderException {
        try {

            RandomForestClassifier randomForestClassifier = new RandomForestClassifier();
            final RandomForestModel randomForestModel = randomForestClassifier.train(trainingData,
                    getNoOfClasses(mlModel),
                    categoricalFeatureInfo, Integer.parseInt(algorithmParameters.get(MLConstants.NUM_TREES)),
                    algorithmParameters.get(MLConstants.FEATURE_SUBSET_STRATEGY),
                    algorithmParameters.get(MLConstants.IMPURITY),
                    Integer.parseInt(algorithmParameters.get(MLConstants.MAX_DEPTH)),
                    Integer.parseInt(algorithmParameters.get(MLConstants.MAX_BINS)),
                    Integer.parseInt(algorithmParameters.get(MLConstants.SEED)));

            // remove from cache
            trainingData.unpersist();
            // add test data to cache

            // remove from cache

            mlModel.setModel(new MLRandomForestModel(randomForestModel));


            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building random forest classification model: "
                    + e.getMessage(), e);
        }

    }

    /**
     * This method builds a naive bayes model
     *
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param workflow     Machine learning workflow
     * @param mlModel      Deployable machine learning model
     * @throws MLModelBuilderException
     */
    private MLModel buildNaiveBayesModel(JavaRDD<LabeledPoint> trainingData, Workflow workflow, MLModel mlModel, Map<String, String> algorithmParameters) throws MLModelBuilderException {
        try {
            NaiveBayesClassifier naiveBayesClassifier = new NaiveBayesClassifier();
            NaiveBayesModel naiveBayesModel = naiveBayesClassifier.train(trainingData,
                    Double.parseDouble(algorithmParameters.get(MLConstants.LAMBDA)));

            // remove from cache
            trainingData.unpersist();
            // add test data to cache

            mlModel.setModel(new MLClassificationModel(naiveBayesModel));

            return mlModel;
        } catch (Exception e) {
            throw new MLModelBuilderException("An error occurred while building naive bayes model: " + e.getMessage(),
                    e);
        }
    }

    private Map<Integer, Integer> getCategoricalFeatureInfo(List<Map<String, Integer>> encodings) {
        Map<Integer, Integer> info = new HashMap<Integer, Integer>();
        // skip the response variable which is at last
        for (int i = 0; i < encodings.size() - 1; i++) {
            if (encodings.get(i).size() > 0) {
                info.put(i, encodings.get(i).size());
            }
        }
        return info;
    }

    private int getNoOfClasses(MLModel mlModel) {
        if (mlModel.getEncodings() == null) {
            return -1;
        }
        int responseIndex = mlModel.getEncodings().size() - 1;
        return mlModel.getEncodings().get(responseIndex) != null ? mlModel.getEncodings().get(responseIndex).size()
                : -1;

    }

    public List<Map<String, Integer>> getMetaModelEncodings(){
        List<Map<String, Integer>> encodings = new ArrayList<Map<String, Integer>>();
        return  encodings;
    }




}
