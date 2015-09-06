/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.core.interfaces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.wso2.carbon.ml.commons.domain.MLModel;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;

/**
 * All Model Builders should extend this class.
 */
public abstract class MLModelBuilder {

    private static final Log log = LogFactory.getLog(MLModelBuilder.class);
    private MLModelConfigurationContext context = null;

    public MLModelBuilder(MLModelConfigurationContext context) {
        this.setContext(context);
    }
    
    /**
     * Pre-process the dataset.
     * @return pre-processed JavaRDD
     * @throws MLModelBuilderException
     */
    public abstract JavaRDD<?> preProcess() throws MLModelBuilderException;
    
    /**
     * Build a model using the context.
     * @return build {@link MLModel}
     * @throws MLModelBuilderException if failed to build the model.
     */
    public abstract MLModel build(JavaRDD<?> dataset) throws MLModelBuilderException;
    
    public void handleIgnoreException(String msg, Exception e) {
        log.error(msg, e);
    }

    public MLModelConfigurationContext getContext() {
        return context;
    }

    public void setContext(MLModelConfigurationContext context) {
        this.context = context;
    }

}
