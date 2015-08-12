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

package org.wso2.carbon.ml.core.spark.summary;

import org.apache.spark.rdd.RDD;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.ModelSummary;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;

public class RecommendationModelSummary implements ModelSummary, Serializable {

	private static final long serialVersionUID = 5557613175001838756L;
	private String algorithm;
	private String[] features;
	private int rank;
	private RDD<Tuple2<Object, double[]>> userFeatures;
	private RDD<Tuple2<Object, double[]>> productFeatures;

	@Override
	public String getModelSummaryType() {
		return MLConstants.RECOMMENDATION_MODEL_SUMMARY;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public RDD<Tuple2<Object, double[]>> getUserFeatures() {
		return userFeatures;
	}

	public void setUserFeatures(RDD<Tuple2<Object, double[]>> userFeatures) {
		this.userFeatures = userFeatures;
	}

	public RDD<Tuple2<Object, double[]>> getProductFeatures() {
		return productFeatures;
	}

	public void setProductFeatures(RDD<Tuple2<Object, double[]>> productFeatures) {
		this.productFeatures = productFeatures;
	}

	/**
	 * @param features Array of names of the features
	 */
	public void setFeatures(String[] features) {
		if (features == null) {
			this.features = new String[0];
		} else {
			this.features = Arrays.copyOf(features, features.length);
		}
	}

	@Override
	public String[] getFeatures() {
		return features;
	}
}
