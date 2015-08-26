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

import java.util.List;

public class SparkOperationFill extends SparkOpration {

	@Override public JavaRDD<String[]> execute(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                           WranglerOperation wranglerOperation,
	                                           Wrangler wrangler) {
		int columnIndex = wrangler.getColumnId(wranglerOperation);
		return fillColumn(jsc, data, columnIndex, wranglerOperation.getParameter("direction"));
	}

	private static JavaRDD<String[]> fillColumn(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                            final int columnIndex, final String direction) {
		if (direction.equals("left") || direction.equals("right")) {
			return data.map(new Function<String[], String[]>() {
				@Override public String[] call(String[] r) throws Exception {
					if (r[columnIndex] == null) {
						String[] rowElements = new String[r.length];
						if (direction.equals("left")) {
							rowElements[0] = r[0];
							for (int i = 1; i < r.length; i++) {
								if (r[i] == null) {
									if (i == columnIndex) {
										rowElements[i] = rowElements[i - 1];
									} else {
										rowElements[i] = null;
									}
								} else {
									rowElements[i] = r[i];
								}
							}

						} else if (direction.equals("right")) {
							rowElements[r.length - 1] = r[r.length - 1];
							for (int i = r.length - 2; i >= 0; i--) {
								if (r[i] == null) {
									if (i == columnIndex) {
										rowElements[i] = rowElements[i + 1];
									} else {
										rowElements[i] = null;
									}
								} else {
									rowElements[i] = r[i];
								}
							}
						}
						return rowElements;
					} else {
						return r;
					}
				}
			});

		} else if (direction.equals("down")) {
			List<String[]> rows = data.collect();
			for (int i = 1; i < rows.size(); i++) {
				String[] r = rows.get(i);
				if (r[columnIndex] == null) {
					String[] rowElements = new String[r.length];
					for (int j = 0; j < r.length; j++) {
						if (j == columnIndex) {
							rowElements[j] = rows.get(i - 1)[columnIndex];
						} else {
							rowElements[j] = r[j];
						}
					}
					rows.remove(i);
					rows.add(i, rowElements);
				}
			}
			return jsc.parallelize(rows);
		} else if (direction.equals("up")) {
			List<String[]> rows = data.collect();
			for (int i = rows.size() - 2; i >= 0; i--) {
				String[] r = rows.get(i);
				if (r[columnIndex] == null) {
					String[] rowElements = new String[r.length];
					for (int j = 0; j < r.length; j++) {
						if (j == columnIndex) {
							rowElements[j] = rows.get(i + 1)[columnIndex];
						} else {
							rowElements[j] = r[j];
						}
					}
					rows.remove(i);
					rows.add(i, rowElements);
				}
			}
			return jsc.parallelize(rows);
		}
		return null;
	}

//	private static JavaRDD<Row> fillRow(JavaSparkContext jsc, JavaRDD<Row> data, int rowIndex,
//	                                    String direction) {
//		List<Row> list = data.collect();
//		Row r = list.get(rowIndex);
//		String[] rowElements = new String[r.length()];
//		if (direction.equals("left")) {
//			if (r.isNullAt(0)) {
//				rowElements[0] = null;
//			} else {
//				rowElements[0] = r.getString(0);
//			}
//			for (int i = 1; i < r.length(); i++) {
//				if (r.isNullAt(i)) {
//					rowElements[i] = rowElements[i - 1];
//				} else {
//					rowElements[i] = r.getString(i);
//				}
//			}
//		} else if (direction.equals("right")) {
//			if (r.isNullAt(r.length() - 1)) {
//				rowElements[r.length() - 1] = null;
//			} else {
//				rowElements[r.length() - 1] = r.getString(r.length() - 1);
//			}
//			for (int i = r.length() - 2; i >= 0; i--) {
//				if (r.isNullAt(i)) {
//					rowElements[i] = rowElements[i + 1];
//				} else {
//					rowElements[i] = r.getString(i);
//				}
//			}
//		} else if (direction.equals("above")) {
//			if (rowIndex == 0)
//				return data;
//			for (int i = 0; i < r.length(); i++) {
//				if (r.isNullAt(i)) {
//					rowElements[i] = null;
//				} else {
//					rowElements[i] = r.get(i).toString();
//				}
//			}
//		}
//
//		list.remove(rowIndex);
//		list.add(rowIndex, Row.create(rowElements));
//		return jsc.parallelize(list);
//
//	}

}
