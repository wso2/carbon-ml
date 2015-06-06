/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.core.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;

/**
 * This class converts Spark SQL Rows into a CSV/TSV string.
 */
public class RowsToLines implements Function<Row, String> {

    private static final long serialVersionUID = -5025419727399292773L;
    private String columnSeparator;

    public RowsToLines(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }

    @Override
    public String call(Row row) {
        StringBuilder sb = new StringBuilder();
        if (row.length() <= 0) {
            return "";
        }
        for (int i = 0; i < row.length(); i++) {
            sb.append(row.get(i) + columnSeparator);
        }
        return sb.substring(0, sb.length() - 1);
    }

}
