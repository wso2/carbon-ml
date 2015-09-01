/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.ml.wrangler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.wrangler.operations.*;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.HashMap;

/**
 * This class contains a wrangler operation and its parameters
 */
public class WranglerOperation {
	private static final Log logger = LogFactory.getLog(WranglerOperation.class);
	HashMap<String, String> parameters;
	String operation;
	WranglerOperation nextOperation;

	public WranglerOperation() {
		nextOperation = null;
		parameters = new HashMap<String, String>();
	}

	/**
	 * Set the next operation
	 *
	 * @param nextOperation WranglerOperation
	 */
	public void setNextOperation(WranglerOperation nextOperation) {
		this.nextOperation = nextOperation;
	}

	/**
	 * Get the next operation
	 *
	 * @return next WranglerOperation
	 */
	public WranglerOperation getNextOperation() {
		return nextOperation;
	}

	/**
	 * Get the current operation
	 *
	 * @return String current operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * Set the current operation
	 *
	 * @param operation String
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public String getParameter(String parameter) {
		return parameters.get(parameter);
	}

	public void addParameter(String param, String value) {
		if (value.matches("\".*\"")) {
			value = value.substring(1, value.length() - 1);
		}
		parameters.put(param, value);
	}

	/**
	 * Apply the current operation on a dataset
	 *
	 * @param jsc      JavaSparkContext
	 * @param data     JavaRDD on which operation is executed
	 * @param wrangler Wrangler
	 * @return JavaRDD on which operation is executed
	 */
	public JavaRDD<String[]> executeOperation(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                          Wrangler wrangler) {
		SparkOpration so;
		if (this.getOperation().equals("split")) {
			so = new SparkOperationSplit();

		} else if (this.getOperation().equals("fill")) {
			so = new SparkOperationFill();

		} else if (this.getOperation().equals("filter")) {
			so = new SparkOperationFilter();

		} else if (this.getOperation().equals("drop")) {
			so = new SparkOperationDrop();

		} else if (this.getOperation().equals("extract")) {
			so = new SparkOperationExtract();

		} else {
			so = null;
		}
		return so.execute(jsc, data, this, wrangler);
	}

	/**
	 * Log the current operation and its parameters
	 */
	public void printOperation() {
		logger.info(operation);
		for (String k : parameters.keySet()) {
			logger.info(k + " - " + parameters.get(k));
		}
	}

}
