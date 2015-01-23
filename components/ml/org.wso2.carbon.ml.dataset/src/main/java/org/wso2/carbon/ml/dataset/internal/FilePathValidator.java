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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.dataset.internal;

import java.util.regex.Pattern;

/**
 * Class contains methods for validating file paths.
 */
public class FilePathValidator {

	private static final String FOLDER_PATH_REGEX = "([a-z]:\\/|\\/)([\\w\\/-]*)";
	private static Pattern filePathPattern = Pattern.compile(FOLDER_PATH_REGEX);

	/*
	 * private Constructor to prevent any other class from instantiating.
	 */
	private FilePathValidator() {
	}

	/**
	 * Checks whether the given path has the valid pattern.
	 *
	 * @param filePath     Path to be checked for correct pattern
	 * @return             Boolean indicating whether the given path has valid pattern
	 */
	protected static boolean isValid(String filePath) {
		return filePathPattern.matcher(filePath).matches();
	}
}