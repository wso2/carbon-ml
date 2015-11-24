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

package org.wso2.carbon.ml.core.wrangler.operations;

import org.wso2.carbon.ml.core.wrangler.Wrangler;
import org.wso2.carbon.ml.core.wrangler.WranglerOperation;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
/**
 * Implementation of {@link SparkOperation} for Drop operation in Wrangler.
 */
public class SparkOperationDrop extends SparkOperation {
	@Override public JavaRDD<String[]> execute(JavaSparkContext jsc, JavaRDD<String[]> data,
	                                           WranglerOperation wranglerOperation,
	                                           Wrangler wrangler) {
		String columnName = wranglerOperation.getParameter("column");
		int columnIndex = wrangler.removeColumn(columnName);
		return drop(data, columnIndex);
	}
	/**
	 * Apply drop operation on a column
	 *
	 * @param data      JavaRDD on which transformations are executed
	 * @param columnId  Integer representing the column
	 */
	private JavaRDD<String[]> drop(JavaRDD<String[]> data, final int columnId) {
		return data.map(new Function<String[], String[]>() {
			@Override public String[] call(String[] row) throws Exception {
				String[] newRow = new String[row.length - 1];
				for (int i = 0, j = 0; i < row.length; j++, i++) {
					if (i == columnId) {
						i++;
						if (i == row.length)
							return newRow;
					}
					newRow[j] = row[i];
				}
				return newRow;
			}
		});
	}

}
