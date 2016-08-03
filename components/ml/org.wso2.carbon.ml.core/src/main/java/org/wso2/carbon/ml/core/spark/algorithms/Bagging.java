package org.wso2.carbon.ml.core.spark.algorithms;

import com.google.common.primitives.Doubles;
import org.apache.commons.collections.CollectionUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.ClassificationModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.Predictor;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.utils.Util;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Misgana on 29.06.16.
 */


public class Bagging implements Serializable, ClassificationModel {

    private static final long serialVersionUID = 690837690937129469L;
    private List<MLModel> baseModelsList = new ArrayList<MLModel>();

    /**
     * This method trains an Stacking ensemble model
     * @param context MLModelConfigurationContext initialized with the application
     * @param modelId Model ID
     * @param trainingData Training data-set as a JavaRDD of labeled points
     * @param baseModels   List of base-learners selected for ensemble
     * @param paramsBaseAlgorithms Hyper-parameters of Base-learners
     * @param seed seed
     * @return
     * @throws MLModelBuilderException
     */

   public void train(MLModelConfigurationContext context, Workflow workflow, long modelId, JavaRDD<LabeledPoint> trainingData, List<String> baseModels,
          List<Map<String, String>> paramsBaseAlgorithms, Integer seed) throws NullPointerException,
            MLModelBuilderException {

       BaseModelsBuilder build = new BaseModelsBuilder();

       int cnt = 0;
       for (String model : baseModels) {
           MLModel baseModel = new MLModel();
           JavaRDD<LabeledPoint> bootstrapSample = trainingData.sample(true, 1.0, seed);

           baseModel = build.buildBaseModels(context, workflow, model, bootstrapSample, paramsBaseAlgorithms.get(cnt),
                   false);
           cnt++;
           // get list of models trained on whole Dataset
           baseModelsList.add(baseModel);

       }



    }

    /**
     * This method applies bagging for ensembling predictions of multiple models using majority vote
     * @param sparkContext JavaSparkContext initialized with the application
     * @param modelId Model ID
     * @param testingData Testing dataset as a JavaRDD of labeled points
     * @return JavaPairRDD of predicted labels and actual labels
     *@throws MLModelHandlerException
     */

    public JavaPairRDD<Double, Double> test(JavaSparkContext sparkContext, long modelId, JavaRDD<LabeledPoint> testingData)
            throws MLModelHandlerException {
        Util convert = new Util();
        List<Double> labelsList = Doubles.asList(convert.getLabels(testingData));
        List<String[]> dataTobePredicted = convert.labeledpointToListStringArray(testingData);

        List<Double> resultPredictions = new ArrayList<Double>();
        for (String[] datapoint : dataTobePredicted) {
            List<String[]> datapointList = new ArrayList<String[]>();
            datapointList.add(datapoint);
            List<Double> datapointPredictions = new ArrayList<Double>();
            for (MLModel model : baseModelsList) {
                List<?> predictions = new ArrayList<>();
                Predictor predictor = new Predictor(modelId, model, datapointList, 0.0,true, true);
                predictions = predictor.predict();
                datapointPredictions.add(Double.valueOf(predictions.get(0).toString()));
            }
            //Map to store number of occurences of a prediction to be used for voting
            Map<Double, Integer> cardinalityMap = CollectionUtils.getCardinalityMap(datapointPredictions);
            Integer maxCardinality = Collections.max(cardinalityMap.values());
            for(final Map.Entry<Double, Integer> entry : cardinalityMap.entrySet()) {
                // return the most voted class
                if (maxCardinality == entry.getValue()) {
                    resultPredictions.add(entry.getKey());
                    break;
                }
            }
        }

        List<Tuple2<Double, Double>> list = new ArrayList<Tuple2<Double, Double>>();
        for (int j = 0; j < resultPredictions.size(); j++) {
            list.add(new Tuple2<Double, Double>(resultPredictions.get(j), labelsList.get(j)));

        }
        return sparkContext.parallelizePairs(list);

    }

    @Override
    public RDD<Object> predict(RDD<Vector> rdd) {
        return null;
    }

    @Override
    public double predict(Vector vector) {
        return 0;
    }

    @Override
    public JavaRDD<Double> predict(JavaRDD<Vector> javaRDD) {
        return null;
    }
}
