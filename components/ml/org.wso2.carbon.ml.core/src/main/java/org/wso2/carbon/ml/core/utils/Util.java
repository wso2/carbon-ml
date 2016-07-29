package org.wso2.carbon.ml.core.utils;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Integer;


import static org.apache.spark.mllib.linalg.Vectors.dense;

/**
 * Created by pekasa on 09.06.16.
 */
public class Util {





    public double[][] arrayListdoubleArray(ArrayList<ArrayList<Double>> matrix, int numOfModels, int trainDataSetSize){
        double[][] level1Dataset = new double[matrix.size()][matrix.get(0).size()];

        for(int i= 0; i<matrix.size(); i++){
            ArrayList<Double> row = matrix.get(i);
            double[] copy = new double[row.size()];
            for(int j = 0; j< copy.length; j++){
                copy[j] = row.get(j);
            }
            level1Dataset[i] = copy;

        }

        return level1Dataset ;
    }

    public Vector listToVetor(List<?> list){
        List<Double> listDouble = (List<Double>) list;

        double[] doubleArray = new double[list.size()];
        for(int i= 0; i < listDouble.size(); i++){
            doubleArray[i] = listDouble.get(i);

        }
        Vector vector = dense(doubleArray);

        return vector;
    }

    public double[] listTodoubleArray(List<?> list){
        List<Double> listDouble = (List<Double>) list;

        double[] doubleArray = new double[list.size()];
        for(int i= 0; i < listDouble.size(); i++){
            doubleArray[i] = listDouble.get(i);

        }

        return doubleArray;
    }

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



    public List<String[]> labeledpointToListStringArray(List<Map<String, Integer>> encodings, JavaRDD<LabeledPoint> rddata) {
        List<String[]> dataToBePredicted = new ArrayList<String[]>();
        List<LabeledPoint> list = rddata.collect();
        List<Map<Integer, String>> reversedEncodingsList = reverse(encodings);


        for(LabeledPoint item : list ){
            String[] labeledPointFeatures;
            double[] vector = item.features().toArray();
            labeledPointFeatures = decoder(vector, reversedEncodingsList);


            dataToBePredicted.add(labeledPointFeatures);

        }

        return dataToBePredicted;

    }

    public List<Map<Integer, String>> reverse(List<Map<String, Integer>> encodings){
        List<Map<Integer, String>> reversedEncodingsList  = new ArrayList<>();

        for(Map<String, Integer> map : encodings){

            Map<Integer, String> reversedEncoding = new HashMap<>();
            for(Map.Entry<String, Integer> entry : map.entrySet()){
                reversedEncoding.put(entry.getValue(), entry.getKey());
            }
            reversedEncodingsList.add(reversedEncoding);

        }
        return reversedEncodingsList;

    }

    public String[] decoder(double[] tokens, List<Map<Integer, String>> reversedEncodingsList) {
        String[] stringTokens = new String[tokens.length];
        if (reversedEncodingsList.isEmpty() || reversedEncodingsList == null) {

            for(int k = 0; k< tokens.length; k++){

                stringTokens[k] = Double.toString(tokens[k]);
            }
            return stringTokens;
        }
        for (int i = 0; i < tokens.length; i++) {
            if (reversedEncodingsList.size() <= i) {
                continue;
            }
            Map<Integer, String> encoding = reversedEncodingsList.get(i);
            if (encoding != null && !encoding.isEmpty()) {
                // if we found an unknown string, we encode it from 0th mapping.
                String code = encoding.get((int)tokens[i]) == null ? encoding.values().iterator().next()
                        : encoding.get((int)tokens[i]);
                // replace the value with the encoded value
                stringTokens[i] = code;
            }
            else{
                stringTokens[i] = Double.toString(tokens[i]);
            }
        }
        return stringTokens;
    }


    // Method to map feature vector to datapoints

    public Map<Vector, Integer> featureToIntegerMap(JavaRDD<LabeledPoint>trainDataSet){


        List<LabeledPoint> list = trainDataSet.collect();
        Map<Vector,Integer> featureVectorMap = new HashMap<Vector, Integer>();
        Integer mapValue = 0;
        for(LabeledPoint item : list ) {
            featureVectorMap.put(item.features(), mapValue);
            mapValue = mapValue.intValue() +1;

        }
        return featureVectorMap ;

    }

}
