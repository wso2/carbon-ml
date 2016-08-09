package org.wso2.carbon.ml.core.utils;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pekasa on 09.06.16.
 */
public class Util {


    public double[] getLabels(JavaRDD<LabeledPoint> rddata){
        List<LabeledPoint> list = rddata.collect();
        // System.out.println("LEVEL0DATASET"+list);
        double[] labels = new double[list.size()];
        int i= 0;
        for(LabeledPoint item : list ){

            labels[i] = item.label();
            i++;


        }

        return labels;
    }

    public double[] getLabelsFolds( Tuple2<RDD<LabeledPoint>, RDD<LabeledPoint>>[]  folds, int numOfDatapoints){
        double[] labels = new double[numOfDatapoints];
        int idx = 0;
        for(Tuple2<RDD<LabeledPoint>, RDD<LabeledPoint>> f : folds){
            for (LabeledPoint p : f._2().toJavaRDD().collect()){
                labels[idx] = p.label();
                idx++;
            }
        }
        return labels;
    }

    public List<LabeledPoint> matrixtoLabeledPoint(double[][] matrix, double[] labels){
        List<LabeledPoint> labeledList = new ArrayList<LabeledPoint>();
        LabeledPoint labeledRecord;

        for(int i=0; i<matrix.length; i++) {
            labeledRecord = new LabeledPoint(labels[i], Vectors.dense(matrix[i]));
            labeledList.add(labeledRecord);
        }
        return  labeledList;
    }



    public List<String[]> labeledpointToListStringArray(JavaRDD<LabeledPoint> rddata) {
        List<String[]> dataToBePredicted = new ArrayList<String[]>();
        List<LabeledPoint> list = rddata.collect();

        for(LabeledPoint item : list ){
            String[] labeledPointFeatures = new String[item.features().size()];
            double[] vector = item.features().toArray();
            for(int k= 0; k<vector.length; k++){
                labeledPointFeatures[k] = (Double.toString(vector[k]));
       }
            dataToBePredicted.add(labeledPointFeatures);

        }

        return dataToBePredicted;

    }
    public List<String[]> labeledpointToStringArray(List<LabeledPoint> list) {
        List<String[]> dataToBePredicted = new ArrayList<String[]>();

        for(LabeledPoint item : list ){
            String[] labeledPointFeatures = new String[item.features().size()];
            double[] vector = item.features().toArray();
            for(int k= 0; k<vector.length; k++){
                labeledPointFeatures[k] = (Double.toString(vector[k]));
            }
            dataToBePredicted.add(labeledPointFeatures);

        }

        return dataToBePredicted;

    }

}
