/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.ml.core.spark.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkContext;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Wraps Spark's {@link MatrixFactorizationModel}
 */
public class MLMatrixFactorizationModel implements Externalizable {

	private static final long serialVersionUID = 186767859324000308L;
	private static final Log log = LogFactory.getLog(MLMatrixFactorizationModel.class);

	private String outPath;
	private MatrixFactorizationModel model;

	public MLMatrixFactorizationModel(MatrixFactorizationModel model) {
		this.model = model;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		try {
			saveModel();
		} catch (MLModelBuilderException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			this.model = retrieveModel();
		} catch (MLModelBuilderException e) {
			log.error(e.getMessage(), e);
		}
	}

	public MatrixFactorizationModel getModel() {
		return model;
	}

	public void setModel(MatrixFactorizationModel model) {
		this.model = model;
	}

	public String getOutPath() {
		return outPath;
	}

	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}

	private void saveModel() throws MLModelBuilderException {
		if(model == null) {
			throw new MLModelBuilderException("Error when persisting model. MatrixFactorizationModel is null.");
		}

		if(outPath == null) {
			throw new MLModelBuilderException("Error when persisting model. Out Path cannot be null.");
		}

		model.save(MLCoreServiceValueHolder.getInstance().getSparkContext().sc(), outPath);
	}

	private MatrixFactorizationModel retrieveModel() throws MLModelBuilderException {
		if(outPath == null) {
			throw new MLModelBuilderException("Error when retrieving model. Out Path cannot be null.");
		}

		return MatrixFactorizationModel.load(MLCoreServiceValueHolder.getInstance().getSparkContext().sc(), outPath);
	}
}
