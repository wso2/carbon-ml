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
 * Created by pekasa on 29.06.16.
 */
public class Bagging implements Serializable, ClassificationModel {

    private List<MLModel> levelZeroModels = new ArrayList<MLModel>();

   public void train(MLModelConfigurationContext context, JavaSparkContext sparkContext, Workflow workflow, MLModel mlModel, long modelId, JavaRDD<LabeledPoint> trainData, List<String> baseModels,
          List<Map<String, String>> paramsBaseAlgorithms, Integer seed) throws NullPointerException, MLModelHandlerException,
            MLModelBuilderException {

       BaseModelsBuilder build = new BaseModelsBuilder();
       int cnt = 0;
       for (String model : baseModels) {
           MLModel baseModel = new MLModel();
           JavaRDD<LabeledPoint> bootstrapSample = trainData.sample(true, 1.0);

           baseModel = build.buildBaseModels(context, workflow, new MLModel(), model, bootstrapSample, paramsBaseAlgorithms.get(cnt));
           cnt++;
           // get list of models trained on whole Dataset
           levelZeroModels.add(baseModel);

       }



    }
    public JavaPairRDD<Double, Double> test(JavaSparkContext sparkContext, long modelId, JavaRDD<LabeledPoint> testDataset)
            throws MLModelHandlerException {
        Util convert = new Util();
        List<Double> labelsList = Doubles.asList(convert.getLabels(testDataset));
        List<String[]> dataTobePredicted = convert.LabeledpointToListStringArray(testDataset);

        List<Double> resultPredictions = new ArrayList<Double>();
        for (String[] datapoint : dataTobePredicted) {
            List<String[]> datapointList = new ArrayList<String[]>();
            datapointList.add(datapoint);
            List<Double> datapointPredictions = new ArrayList<Double>();
            for (MLModel model : levelZeroModels) {
                List<?> predictions = new ArrayList<>();
                Predictor predictor = new Predictor(modelId, model, datapointList, 0.0,true);
                predictions = predictor.predict();
                datapointPredictions.add(Double.valueOf(predictions.get(0).toString()));
            }
            Map<Double, Integer> cardinalityMap = CollectionUtils.getCardinalityMap(datapointPredictions);
            Integer maxCardinality = Collections.max(cardinalityMap.values());
            for(final Map.Entry<Double, Integer> entry : cardinalityMap.entrySet()) {
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
