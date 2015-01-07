/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.decomposition.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.wso2.carbon.ml.decomposition.DecompositionService;
import org.wso2.carbon.ml.decomposition.exceptions.DecompositionException;

import java.util.ArrayList;
import java.util.List;

public class SparkDecompositionService implements DecompositionService {

    @Override
    public void fitPCA(String workflowID,JavaRDD<Vector> dataSet,
                       int numOfCompsRetain) throws DecompositionException {
        Matrix pcaTransFormedMatrix = fitPCAHelper(dataSet, numOfCompsRetain);
        SparkDecompositionUtil.saveMatrix(workflowID, pcaTransFormedMatrix);
    }

    @Override
    public JavaRDD<Vector> transformPCA(String workflowID,
                                        JavaRDD<Vector> dataSet) throws DecompositionException {

        RowMatrix dataMatrix = new RowMatrix(dataSet.rdd());
        Matrix principleComponents = SparkDecompositionUtil.loadMatrix(workflowID);
        RowMatrix projectedMatrix = dataMatrix.multiply(principleComponents);
        return projectedMatrix.rows().toJavaRDD();
    }

    @Override
    public double[][] visualizePCA(String workflowID) throws DecompositionException {
        JavaRDD<Vector> dataSet = SparkDecompositionUtil.getSamplePoints(workflowID);
        RowMatrix dataMatrix = new RowMatrix(dataSet.rdd());

        Matrix pcaTransFormedMatrix = fitPCAHelper(dataSet, 2);
        RowMatrix projectedMatrix = dataMatrix.multiply(pcaTransFormedMatrix);

        JavaRDD<Vector> pcaDataPoints = projectedMatrix.rows().toJavaRDD();
        final List<double[]> visulizationPoints = new ArrayList<double[]>();
        pcaDataPoints.foreach(new VoidFunction<Vector>() {
            @Override
            public void call(Vector vector) throws Exception {
                visulizationPoints.add(vector.toArray());

            }
        });

        return visulizationPoints.toArray(new double[visulizationPoints.size()][2]);
    }

    private Matrix fitPCAHelper(JavaRDD<Vector> dataSet, int numOfCompsRetain){

        RowMatrix dataMatrix= new RowMatrix(dataSet.rdd());
        Matrix pcaTransFormedMatrix =
            dataMatrix.computePrincipalComponents(numOfCompsRetain);
        return pcaTransFormedMatrix;
    }
}
