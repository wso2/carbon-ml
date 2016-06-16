package org.wso2.carbon.ml.core.spark.algorithms;

import com.google.common.primitives.Doubles;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.ClassificationModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.rdd.RDD;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.Predictor;
import org.wso2.carbon.ml.core.utils.Util;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by pekasa on 01.06.16.
 */
public class Stacking implements Serializable, ClassificationModel {

    private MLModel levelOneModel;
    private List<MLModel> levelZeroModels = new ArrayList<MLModel>();

    public Stacking() {

    }

    /**
     * This method trains an Stacking ensemble model
     * @param sparkContext JavaSparkContext initialized with the application
     * @param modelId Model ID
     * @param trainData Training dataset as a JavaRDD of labeled points
     * @param baseModels   List of basemodels selected for ensembling using Stacking
     * @param paramsBaseAlgorithms Hyperparameters of Base-Algorithms
     * @param metaAlgorithm    Name of Meta-Algorithm
     * @param paramsMetaAlgorithm Hyperparameters of Meta-Algorithm
     * @param numFolds Number of folds for cross-validation
     * @param seed seed
     * @return
     */

    public void train(JavaSparkContext sparkContext,long modelId, JavaRDD<LabeledPoint> trainData, List<String> baseModels,
                      List<Map<String, String>> paramsBaseAlgorithms, String metaAlgorithm,
                      Map<String, String> paramsMetaAlgorithm,
                      Integer numFolds, Integer seed) throws NullPointerException, MLModelHandlerException,
            MLModelBuilderException {

        // Step1. train list of level0models on cross-validated data
        // Step2. get predictions of each List<?> and combine predictions to get level1 data
        // Step3. train level1model on level1 data
        // Step4. train level0models on whole data and store list of models

        Util convert = new Util();
        BaseModelsBuilder build = new BaseModelsBuilder();


        RDD<LabeledPoint> r = trainData.rdd();
        Tuple2<RDD<LabeledPoint>, RDD<LabeledPoint>>[] folds = null;

        if (numFolds > 1){
            // create a map from feature vector to some index. use this index in folds to track predicted datapoints.

            folds = MLUtils.kFold(r, numFolds, seed,
                    trainData.classTag());

        } else{
            throw new RuntimeException("not working yet: use folds > 1"); // TODO: Resolve casting problem
            //folds = (Tuple2<RDD<LabeledPoint>, RDD<LabeledPoint>>[])new Object[1];
            //folds[0] = new Tuple2<RDD<LabeledPoint>, RDD<LabeledPoint>>(r, r);
        }


        double[][] matrix = new double[(int) trainData.count()][baseModels.size()];


        int cnt = 0;
        for (String model : baseModels) {

            int idx = 0;
            // train basemodels on whole Dataset
            MLModel noCVbaseModel = build.buildBaseModels(model, trainData, paramsBaseAlgorithms.get(cnt));
            // train basemodels on cross-validated Dataset
            for (Tuple2<RDD<LabeledPoint>, RDD<LabeledPoint>> fold : folds) {
                List<String[]> dataTobePredicted = convert.LabeledpointToListStringArray(fold._2().toJavaRDD());
                MLModel baseModel = build.buildBaseModels(model, fold._1.toJavaRDD(), paramsBaseAlgorithms.get(cnt));
                Predictor predictor = new Predictor(modelId, baseModel, dataTobePredicted);
                List<Double> predictions = (List<Double>) predictor.predict();

                for (int i = 0; i < predictions.size(); i++) {
                    matrix[idx][cnt] = predictions.get(i);
                    idx++;
                }
            }
            cnt++;
            // get list of models trained on whole Dataset
            levelZeroModels.add(noCVbaseModel);
        }

        // Level-One-Dataset
        List<LabeledPoint> levelOneDataset = convert.matrixtoLabeledPoint(matrix, convert.getLabelsFolds(folds,
                (int) trainData.count()));

        JavaRDD<LabeledPoint> levelOneDistData = sparkContext.parallelize(levelOneDataset);

        // Train Meta-Algorithm  using Level-One-Dataset
        levelOneModel = build.buildBaseModels(metaAlgorithm, levelOneDistData, paramsMetaAlgorithm);


    }


    public JavaPairRDD<Double, Double> test(JavaSparkContext sparkContext, long modelId, JavaRDD<LabeledPoint> testDataset)
            throws MLModelHandlerException {

        // Predict on levelZeroTestDataset to get levelOneTestDataset
        Util convert = new Util();
        double[][] matrix = new double[(int) testDataset.count()][levelZeroModels.size()];

        List<String[]> dataTobePredicted = convert.LabeledpointToListStringArray(testDataset);
        int cnt = 0;
        for (MLModel model : levelZeroModels) {
            int idx = 0;
            Predictor predictor = new Predictor(modelId, model, dataTobePredicted);
            List<Double> predictions = (List<Double>) predictor.predict();


            for (int i = 0; i < predictions.size(); i++) {
                matrix[idx][cnt] = predictions.get(i);
                idx++;
            }
            cnt++;
        }

        List<LabeledPoint> levelOneTestDataset = convert.matrixtoLabeledPoint(matrix, convert.getLabels(testDataset));


        JavaRDD<LabeledPoint> levelOneDistTestData = sparkContext.parallelize(levelOneTestDataset);
        List<String[]> LevelOneTestDatasetList = convert.LabeledpointToListStringArray(levelOneDistTestData);
        Predictor predictor = new Predictor(modelId, levelOneModel, LevelOneTestDatasetList);

        List<Double> levelOnePredictions = (List<Double>) predictor.predict();
        List<Double> labelsList = Doubles.asList(convert.getLabels(testDataset));

        List<Tuple2<Double, Double>> list = new ArrayList<Tuple2<Double, Double>>();
        for (int j = 0; j < levelOnePredictions.size(); j++) {
            list.add(new Tuple2<Double, Double>(levelOnePredictions.get(j), labelsList.get(j)));

        }
        return sparkContext.parallelizePairs(list);

    }


    public JavaRDD<LabeledPoint> blend() {
        //TODO: CLEAN CODE: helper method for getting predictions of basemodels and combining them to get levelonedataset
        return null;
    }


    @Override
    public RDD<Object> predict(RDD<Vector> rdd) {
        //TODO: Implement
        return null;
    }

    @Override
    public double predict(Vector vector) {
        //TODO: Implement
        return 0;
    }

    @Override
    public JavaRDD<Double> predict(JavaRDD<Vector> javaRDD) {
        //TODO: Implement
        return null;
    }
}


