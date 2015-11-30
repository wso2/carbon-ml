/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.ml.core.spark.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.rdd.RDD;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

import scala.Tuple2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * Wraps Spark's {@link MatrixFactorizationModel}
 */
public class MLMatrixFactorizationModel implements Externalizable {

    private static final long serialVersionUID = 186767859324000308L;
    private static final Log log = LogFactory.getLog(MLMatrixFactorizationModel.class);

    private MatrixFactorizationModel model;

    public MLMatrixFactorizationModel() {

    }

    public MLMatrixFactorizationModel(MatrixFactorizationModel model) {
        this.model = model;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // can't save the whole MatrixFactorizationModel, hence saving relevant attributes separately.
        out.writeInt(model.rank());
        out.writeObject(model.userFeatures().toJavaRDD().collect());
        out.writeObject(model.productFeatures().toJavaRDD().collect());

        if (log.isDebugEnabled()) {
            log.debug("Rank, user features and product features of MatrixFactorizationModel were serialized "
                    + "successfully.");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int rank = in.readInt();
        List<Tuple2<Object, double[]>> userFeaturesList = (List<Tuple2<Object, double[]>>) in.readObject();
        List<Tuple2<Object, double[]>> productFeaturesList = (List<Tuple2<Object, double[]>>) in.readObject();

        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        RDD<Tuple2<Object, double[]>> userFeatures = valueHolder.getSparkContext().parallelize(userFeaturesList).rdd();
        RDD<Tuple2<Object, double[]>> productFeatures = valueHolder.getSparkContext().parallelize(productFeaturesList)
                .rdd();
        model = new MatrixFactorizationModel(rank, userFeatures, productFeatures);

        if (log.isDebugEnabled()) {
            log.debug("Rank, user features and product features were de-serialized successfully and loaded "
                    + "MatrixFactorizationModel.");
        }
    }

    public MatrixFactorizationModel getModel() {
        return model;
    }

    public void setModel(MatrixFactorizationModel model) {
        this.model = model;
    }

}
