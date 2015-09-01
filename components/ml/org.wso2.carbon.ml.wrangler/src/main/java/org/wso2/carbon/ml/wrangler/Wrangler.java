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
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.wso2.carbon.ml.wrangler.exceptions.WranglerException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This class contains all the wrangler operations and relevant data
 */
public class Wrangler {
	private static final Log logger = LogFactory.getLog(Wrangler.class);

	private WranglerOperation currentOperation;
	private WranglerOperation previousOperation;
	private ArrayList<String> columns;
	private ArrayList<Boolean> columnsSplit;
	private ArrayList<Boolean> columnsExtract;
	private WranglerOperation firstOperation;

	public Wrangler() {
		columns = new ArrayList<String>();
		columnsSplit = new ArrayList<Boolean>();
		columnsExtract = new ArrayList<Boolean>();
		currentOperation = null;
		previousOperation = null;
	}
	/**
	 * Add a new Wrangler script from a file
	 *
	 * @param scriptPath String file path
	 */
	public void addFileScript(JavaRDD<String[]> data, String scriptPath) {
		Path script = Paths.get(scriptPath);
		Scanner scanner = null;
		try {
			scanner = new Scanner(script);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		boolean flag = false;

		addNewOperation();

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			flag = parseLine(line, flag, currentOperation);
		}
	}
	/**
	 * Add a new Wrangler script
	 *
	 * @param script String
	 * @throws WranglerException
	 */
	public void addScript(String script) throws WranglerException{
		try {
			boolean flag = false;
			addNewOperation();
			String[] lines = script.split("\n");
			for (String line : lines) {
				flag = parseLine(line, flag, currentOperation);
			}
		}catch (Exception e){
			throw new WranglerException("Error while parsing the script"+e.getMessage(),e);
		}
	}

	/**
	 * Add a new Wrangler Operation
	 */
	public void addNewOperation() {
		previousOperation = currentOperation;
		currentOperation = new WranglerOperation();
		if (previousOperation == null) {
			firstOperation = currentOperation;
		} else {
			previousOperation.setNextOperation(currentOperation);
		}
	}

	/**
	 * Execute all the wrangler operations on a dataset
	 *
	 * @param jsc  JavaSparkContext
	 * @param data JavaRDD on which transformations are executed
	 * @return JavaRDD on which transformations are executed
	 */
	public JavaRDD<String[]> executeOperations(JavaSparkContext jsc, JavaRDD<String[]> data) {
		WranglerOperation nextOperation = this.firstOperation;
		while (nextOperation.nextOperation != null) {
			data = nextOperation.executeOperation(jsc, data, this);
			nextOperation = nextOperation.getNextOperation();
		}
		return data;
	}

	/**
	 * Parse a single line in the input script
	 *
	 * @param line              String representing the line to be passed
	 * @param flag              Boolean to identify a new operation
	 * @param wranglerOperation WranglerOperation defined by current line in the script
	 * @return flag value
	 */
	private boolean parseLine(String line, boolean flag, WranglerOperation wranglerOperation) {
		Pattern pattern;
		Matcher matcher;

		line = line.trim();
		if (line.equals(")")) {
			if (flag) {
				this.addNewOperation();
			}
			return false;
		}

		if (line.startsWith("w.add(")) {
			pattern = Pattern.compile("\\.[a-z_]+\\(");
			matcher = pattern.matcher(line);
			matcher.find();
			matcher.find();
			String operation = matcher.group();
			operation = operation.substring(1, operation.length() - 1);
			wranglerOperation.setOperation(operation);
			flag = true;
			line = line.substring(9);
		}
		if (line.matches(".*dw\\.[a-zA-Z_]+\\(.*")) {
			String l1 = line.substring(1, line.indexOf('('));
			pattern = Pattern.compile("\\.[a-zA-Z_]+\\(");
			matcher = pattern.matcher(line);
			matcher.find();
			matcher.find();
			String operation = matcher.group();
			operation = operation.substring(1, operation.length() - 1);
			wranglerOperation.addParameter(l1, operation);

			line = line.replaceAll(".*dw\\.[a-zA-Z_]+\\(", "");

		}

		if (flag) {
			pattern = Pattern.compile("\\.[a-zA-Z_]+\\(");
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				String param = matcher.group();
				param = param.substring(1, param.length() - 1);
				line = line.substring(matcher.end());
				String value = line.substring(matcher.groupCount(), line.length() - 1);
				if (param.equals("column")) {
					if (value.length() > 4)
						value = value.substring(2, value.length() - 2);
				}
				if (!value.equals("undefined")) {
					wranglerOperation.addParameter(param, value);
				}
			}

		}
		return flag;
	}

	/**
	 * Initiate the columns for a dataset
	 *
	 * @param numberOfColumns int
	 */
	public void initColumns(int numberOfColumns) {
		if (numberOfColumns == 1) {
			columns.add(0, "data");
			return;
		}
		columns.add(0, "split");
		columnsSplit.add(0, true);
		for (int i = 1; i < numberOfColumns; i++) {
			columns.add(i, "split" + i);
			columnsSplit.add(i, true);
		}
	}
	/**
	 * Log the columns
	 */
	public void printColumns() {
		for (String s : columns) {
			logger.info("- " + s);
		}
	}

	/**
	 * Get the column index
	 *
	 * @param columnName String
	 * @return column index
	 */
	public int getColumnId(String columnName) {
		return columns.indexOf(columnName);
	}

	/**
	 * Remove a column by name
	 *
	 * @param columnName String
	 * @return index of the column removed
	 */
	public int removeColumn(String columnName) {
		int id = columns.indexOf(columnName);
		columns.remove(columnName);
		if (columnName.contains("split")) {
			if (columnName.equals("split")) {
				columnsSplit.set(0, false);
			} else {
				int t = Integer.parseInt(columnName.replace("split", ""));
				if (t < columnsSplit.size()) {
					columnsSplit.set(t, false);
				}
			}
		}
		return id;
	}

	/**
	 * Add a column
	 *
	 * @param columnName  String
	 * @param columnIndex int
	 */
	public void addColumn(String columnName, int columnIndex) {
		if (columnName.equals("split")) {
			boolean first = false;
			for (int i = 0; i < columnsSplit.size(); i++) {
				if (!columnsSplit.get(i)) {
					columnsSplit.remove(i);
					columnsSplit.add(i, true);
					if (columnIndex == 0) {
						columns.add(0, "split");
					} else {
						columns.add(columnIndex, "split" + i);
					}
					if (!first)
						first = true;
					else {
						return;
					}
				}
			}
			columns.add(columnIndex, "split" + columnsSplit.size());
			columnsSplit.add(true);
			if (!first) {
				columns.add(columnIndex + 1, "split" + columnsSplit.size());
				columnsSplit.add(true);
			}
		} else {
			columns.add(columnIndex, columnName);
		}
	}

	/**
	 * Get the column index of a specific operation
	 *
	 * @param wranglerOperation WranglerOperation
	 * @return column index
	 */
	public int getColumnId(WranglerOperation wranglerOperation) {
		String columnName = wranglerOperation.getParameter("column");
		return columns.indexOf(columnName);
	}
}
