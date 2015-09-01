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

/**
 * Implementation of {@link SparkOpration} for Fill operation in Wrangler.
 */
public class SparkOperationFill extends SparkOpration {

	@Override public JavaRDD<String[]> execute(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                           WranglerOperation wranglerOperation,
	                                           Wrangler wrangler) {
		int columnIndex = wrangler.getColumnId(wranglerOperation);
		String direction = wranglerOperation.getParameter("direction");
		return fillColumn(jsc, data, columnIndex, direction);
	}

	/**
	 * Apply fill operation on a column
	 *
	 * @param jsc       JavaSparkContext
	 * @param data      JavaRDD on which transformations are executed
	 * @param columnId  Integer representing the column
	 * @param direction String representing the direction on the fill operation
	 */
	private JavaRDD<String[]> fillColumn(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                     final int columnId, final String direction) {
		if (direction.equals("left") || direction.equals("right")) {
			return data.map(new Function<String[], String[]>() {
				@Override public String[] call(String[] r) throws Exception {
					if (r[columnId] == null) {
						String[] rowElements = new String[r.length];
						if (direction.equals("left")) {
							rowElements[0] = r[0];
							for (int i = 1; i < r.length; i++) {
								if (r[i] == null) {
									if (i == columnId) {
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
									if (i == columnId) {
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
				if (r[columnId] == null) {
					String[] rowElements = new String[r.length];
					for (int j = 0; j < r.length; j++) {
						if (j == columnId) {
							rowElements[j] = rows.get(i - 1)[columnId];
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
				if (r[columnId] == null) {
					String[] rowElements = new String[r.length];
					for (int j = 0; j < r.length; j++) {
						if (j == columnId) {
							rowElements[j] = rows.get(i + 1)[columnId];
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

	/**
	 * Apply fill operation on a row
	 *
	 * @param jsc       JavaSparkContext
	 * @param data      JavaRDD on which transformations are executed
	 * @param rowIndex  Integer representing the row
	 * @param direction String representing the direction on the fill operation
	 */
	private JavaRDD<String[]> fillRow(JavaSparkContext jsc, JavaRDD<String[]> data, int rowIndex,
	                                  String direction) {
		List<String[]> list = data.collect();
		String[] row = list.get(rowIndex);
		String[] newRow = new String[row.length];
		if (direction.equals("left")) {
			newRow[0] = row[0];
			for (int i = 1; i < row.length; i++) {
				newRow[i] = row[i - 1];
			}
		} else if (direction.equals("right")) {
			newRow[row.length - 1] = row[row.length - 1];
			for (int i = row.length - 2; i >= 0; i--) {
				newRow[i] = row[i + 1];
			}
		} else if (direction.equals("above")) {
			if (rowIndex == 0) {
				return data;
			} else {
				String[] row2 = list.get(rowIndex - 1);
				for (int i = 0; i < row.length; i++) {
					if (row[i] == null) {
						newRow[i] = row2[i];
					} else {
						newRow[i] = row[i];
					}
				}
			}
		} else if (direction.equals("below")) {
			if (rowIndex == list.size() - 1) {
				return data;
			} else {
				String[] row2 = list.get(rowIndex + 1);
				for (int i = 0; i < row.length; i++) {
					if (row[i] == null) {
						newRow[i] = row2[i];
					} else {
						newRow[i] = row[i];
					}
				}
			}
		}
		list.remove(rowIndex);
		list.add(rowIndex, newRow);
		return jsc.parallelize(list);
	}

}
