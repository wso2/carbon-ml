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

package org.wso2.carbon.ml.wrangler.operations;

import org.wso2.carbon.ml.wrangler.Wrangler;
import org.wso2.carbon.ml.wrangler.WranglerOperation;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;

public abstract class SparkOpration implements Serializable {
	String name;

	public abstract JavaRDD<String[]> execute(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                          WranglerOperation wranglerOperation,
	                                          Wrangler wrangler);
}
