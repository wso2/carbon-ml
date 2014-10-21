/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.model;

public class Algorithm {

	private final String algorithm;
	public static final Algorithm LOGISTIC_REGRESSION = new Algorithm("LOGISTIC_REGRESSION");
	public static final Algorithm LINEAR_REGRESSION = new Algorithm("LINEAR_REGRESSION");
	public static final Algorithm RANDOM_FOREST = new Algorithm("RANDOM_FOREST");
	public static final Algorithm DECISION_TREE = new Algorithm("DECISION_TREE");
	public static final Algorithm MULTILAYER_PERCEPTRON = new Algorithm("MULTILAYER_PERCEPTRON");
	public static final Algorithm NAIVE_BAYES = new Algorithm("NAIVE_BAYES");
	public static final Algorithm K_MEANS = new Algorithm("K_MEANS");
	public static final Algorithm SVM = new Algorithm("SVM");
	public static final Algorithm MARKOV_CHAINS = new Algorithm("MARKOV_CHAINS");

	private Algorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
	public String toString() {
		return algorithm;
	}

	public String getAlgorithm() {
		return algorithm;
	}
}