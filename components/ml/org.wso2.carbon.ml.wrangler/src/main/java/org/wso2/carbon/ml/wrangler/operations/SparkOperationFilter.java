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

/**
 * Implementation of {@link SparkOpration} for Filter operation in Wrangler.
 */
public class SparkOperationFilter extends SparkOpration {

	@Override public JavaRDD<String[]> execute(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                           WranglerOperation wranglerOperation,
	                                           Wrangler wrangler) {
		int columnId = wrangler.getColumnId(wranglerOperation);
		String s = wranglerOperation.getParameter("conditions");
		if (s.equals("is_null")) {
			String columnName = wranglerOperation.getParameter("lcol");
			columnId = wrangler.getColumnId(columnName);
			return filterOnColumn(data, columnId);
		} else if (s.equals("rowIndex")) {
			return filterOnIndex(jsc, data, wranglerOperation.getParameter("indices"));
		} else if (s.equals("eq")) {
			return filterOnColumn(data, columnId, wranglerOperation.getParameter("value"));
		} else if (s.equals("empty")) {
			return filterEmptyRows(data);
		}
		return null;
	}

	/**
	 * Apply filter operation on empty rows
	 *
	 * @param data JavaRDD on which transformations are executed
	 */
	private JavaRDD<String[]> filterEmptyRows(JavaRDD<String[]> data) {
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

	/**
	 * Apply filter operation on a row
	 *
	 * @param jsc     JavaSparkContext
	 * @param data    JavaRDD on which transformations are executed
	 * @param indices String representing the rows to be filtered
	 */
	private JavaRDD<String[]> filterOnIndex(JavaSparkContext jsc, JavaRDD<String[]> data,
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

	/**
	 * Apply filter operation based on a column value
	 *
	 * @param data     JavaRDD on which transformations are executed
	 * @param columnId Integer representing the column
	 * @param value    String representing the value to be compared
	 */
	private JavaRDD<String[]> filterOnColumn(JavaRDD<String[]> data, final int columnId,
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

	/**
	 * Apply filter operation if a column is empty
	 *
	 * @param data     JavaRDD on which transformations are executed
	 * @param columnId Integer representing the column
	 */
	private JavaRDD<String[]> filterOnColumn(JavaRDD<String[]> data, final int columnId) {
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
