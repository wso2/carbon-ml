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
import org.apache.spark.api.java.function.Function;

import java.util.*;

public class SparkOperationFilter extends SparkOpration {

	@Override public JavaRDD<String[]> execute(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                           WranglerOperation wranglerOperation,
	                                           Wrangler wrangler) {
		int columnId = wrangler.getColumnId(wranglerOperation);
		switch (wranglerOperation.getParameter("conditions")) {
			case "is_null":
				String columnName = wranglerOperation.getParameter("lcol");
				columnId = wrangler.getColumnId(columnName);
				return filter(data, columnId);
			case "rowIndex":
				return filter_rowIndex(jsc, data, wranglerOperation.getParameter("indices"));
			case "eq":
				return filter(data, columnId, wranglerOperation.getParameter("value"));
			case "empty":
				return filter_empty(data);

		}
		return null;
	}

	private JavaRDD<String[]> filter_empty(JavaRDD<String[]> data) {
		return data.filter(new Function<String[], Boolean>() {
			@Override public Boolean call(String[] row) throws Exception {
				if (row == null) {
					return false;
				}
				for (int i = 0; i < row.length; i++) {
					if (row[i] == null || row[i].equals("")) {
						continue;
					}
					return true;
				}
				return false;
			}
		});
	}

	private static JavaRDD<String[]> filter_rowIndex(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                                 String indices) {
		String[] indecesList = indices.substring(1, indices.length() - 1).split(",");
		ArrayList<Integer> indecesList2 = new ArrayList<Integer>();
		for (int i = 0; i < indecesList.length; i++) {
			indecesList2.add(Integer.parseInt(indecesList[i]));
		}
		Collections.sort(indecesList2);
		List<String[]> list = data.collect();
		//System.out.println(list.size());

		for (int i = indecesList2.size() - 1; i >= 0; i--) {
			int t = indecesList2.get(i);
			list.remove(t);
		}
		//System.out.println(list.size());
		return jsc.parallelize(list);
	}

	private static JavaRDD<String[]> filter(JavaRDD<String[]> data, final int columnId,
	                                        final String value) {
		return data.filter(new Function<String[], Boolean>() {
			@Override public Boolean call(String[] row) throws Exception {
				if (row[columnId] == null)
					return true;
				if (row[columnId].equals(value)) {
					return false;
				} else {
					return true;
				}
			}
		});
	}

	private static JavaRDD<String[]> filter(JavaRDD<String[]> data, final int columnId) {
		return data.filter(new Function<String[], Boolean>() {
			@Override public Boolean call(String[] row) throws Exception {
				if (row[columnId] == null)
					return false;
				else
					return true;
			}
		});
	}

}
