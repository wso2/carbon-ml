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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SparkOpration} for Extract operation in Wrangler.
 */
public class SparkOperationExtract extends SparkOpration {

	@Override public JavaRDD<String[]> execute(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                           WranglerOperation wranglerOperation,
	                                           Wrangler wrangler) {
		int columnId = wrangler.getColumnId(wranglerOperation.getParameter("column"));
		wrangler.addColumn("extract", columnId + 1);
		String after = wranglerOperation.getParameter("after");
		String before = wranglerOperation.getParameter("before");
		String on = wranglerOperation.getParameter("on");
		String positions = wranglerOperation.getParameter("positions");
		if (after == null && before == null && on == null)
			return extractOnIndex(data, columnId, positions);
		if (after == null)
			after = "";
		if (before == null)
			before = "";
		if (on == null)
			on = "";
		return extract(data, columnId, after, before, on);
	}

	/**
	 * Apply extract operation based on a position
	 *
	 * @param data      JavaRDD on which transformations are executed
	 * @param columnId  Integer representing the column
	 * @param positions String containing the index
	 */
	private JavaRDD<String[]> extractOnIndex(JavaRDD<String[]> data, final int columnId,
	                                         final String positions) {
		return data.map(new Function<String[], String[]>() {
			@Override public String[] call(String[] row) throws Exception {
				String[] newRow = new String[row.length + 1];
				if (row[columnId] == null) {
					for (int i = 0, j = 0; i < row.length; i++, j++) {
						if (row[i] == null) {
							newRow[j] = null;
							if (i == columnId) {
								j++;
								newRow[j] = null;
							}
						} else {
							newRow[j] = row[i];
						}
					}

					return newRow;
				} else {
					for (int i = 0, j = 0; i < row.length; i++, j++) {
						if (columnId == i) {
							String val = row[i];
							String positions1 = positions.substring(1, positions.length() - 1);
							String[] positions2 = positions1.split(",");
							int p1 = Integer.parseInt(positions2[0]);
							int p2 = Integer.parseInt(positions2[1]);
							newRow[j] = val;
							if (p2 < val.length()) {
								newRow[++j] = val.substring(p1, p2);
							} else if (p1 < val.length()) {
								newRow[++j] = val.substring(p1);
							} else {
								newRow[++j] = null;
							}
						} else {
							newRow[j] = row[i];
						}
					}
					return newRow;
				}
			}
		});
	}

	/**
	 * Apply extract operation based on a regex
	 *
	 * @param data     JavaRDD on which transformations are executed
	 * @param columnId Integer representing the column
	 * @param after    String after which operation is applied
	 * @param before   String before which operation is applied
	 * @param on       String on which operation is applied
	 */
	private JavaRDD<String[]> extract(JavaRDD<String[]> data, final int columnId,
	                                  final String after, final String before, final String on) {
		System.out.println("Split - " + columnId + " " + after + " " + before + " " + on);
		return data.map(new Function<String[], String[]>() {
			@Override public String[] call(String[] row) throws Exception {
				if (row == null)
					return null;
				if (row[columnId] == null) {
					String[] list = new String[row.length + 1];
					for (int i = 0, j = 0; i < row.length; i++, j++) {
						if (i == columnId) {
							j++;
							list[j] = null;
						} else {
							list[j] = row[i];
						}
					}
					return list;
				} else {
					String[] list = new String[row.length + 1];
					for (int i = 0, j = 0; i < row.length; i++, j++) {
						if (columnId == i) {
							String val = row[i];
							Pattern pattern = Pattern.compile(after + on + before);
							Matcher matcher = pattern.matcher(val);
							if (matcher.find()) {
								list[j] = val;
								list[++j] = val.substring(matcher.start() - matcher.end());
							} else {
								list[++j] = null;
							}

						} else {
							list[j] = row[i];
						}
					}
					return list;
				}
			}
		});
	}
}
