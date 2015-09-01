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

import org.wso2.carbon.ml.wrangler.operations.*;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.HashMap;

public class WranglerOperation {
	HashMap<String, String> paramters;
	String operation;
	WranglerOperation nextOperation;

	public WranglerOperation() {
		nextOperation = null;
		paramters = new HashMap<String, String>();
	}

	public void setNextOperation(WranglerOperation nextOperation) {
		this.nextOperation = nextOperation;
	}

	public WranglerOperation getNextOperation() {
		return nextOperation;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public HashMap<String, String> getParameters() {
		return paramters;
	}

	public String getParameter(String parameter) {
		return paramters.get(parameter);
	}

	public void addParameter(String param, String value) {
		if (value.matches("\".*\"")) {
			value = value.substring(1, value.length() - 1);
		}
		paramters.put(param, value);
	}

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

	public void printOperation() {
		System.out.println(operation);
		for (String k : paramters.keySet()) {
			System.out.println(k + " - " + paramters.get(k));
		}
		System.out.println("================================");
	}

}
