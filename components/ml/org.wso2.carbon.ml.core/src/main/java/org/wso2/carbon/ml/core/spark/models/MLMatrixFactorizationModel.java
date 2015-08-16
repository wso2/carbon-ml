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

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Wraps Spark's {@link MatrixFactorizationModel}
 */
public class MLMatrixFactorizationModel implements Externalizable {

	private static final long serialVersionUID = 186767859324000308L;
	private MatrixFactorizationModel model;

	public MLMatrixFactorizationModel() {

	}

	public MLMatrixFactorizationModel(MatrixFactorizationModel model) {
		this.model = model;
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(model);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		model = (MatrixFactorizationModel) in.readObject();
	}

	public MatrixFactorizationModel getModel() {
		return model;
	}

	public void setModel(MatrixFactorizationModel model) {
		this.model = model;
	}
}
