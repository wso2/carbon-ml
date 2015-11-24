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
import org.wso2.carbon.ml.commons.domain.Workflow;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.WranglerException;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.transformations.DiscardedRowsFilter;
import org.wso2.carbon.ml.core.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.core.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.core.wrangler.Wrangler;

/**
 * All Model Builders should extend this class.
 */
public abstract class MLModelBuilder {

    private static final Log log = LogFactory.getLog(MLModelBuilder.class);
    private MLModelConfigurationContext context = null;

    public MLModelBuilder(MLModelConfigurationContext context) {
        this.setContext(context);
    }
    
    public JavaRDD<String[]> cleanData() {
        HeaderFilter headerFilter = new HeaderFilter.Builder().init(context).build();
        LineToTokens lineToTokens = new LineToTokens.Builder().init(context).build();
        DiscardedRowsFilter discardedRowsFilter = new DiscardedRowsFilter.Builder().init(context).build();

        Workflow workflow = context.getFacts();
        String wranglerScript = workflow.getWranglerScript();

        // clean up data using wrangler
        JavaRDD<String> lines = context.getLines().cache();
        JavaRDD<String[]> inputData = lines.filter(headerFilter).map(lineToTokens).filter(discardedRowsFilter);
        JavaRDD<String[]> cleansedData;
        lines.unpersist();
        inputData.cache();
        Wrangler wrangler = new Wrangler();
        try {
            wrangler.addScript(wranglerScript);
            cleansedData = wrangler.executeOperations(context.getSparkContext(), inputData);
        } catch (WranglerException e) {
            // FIXME
            System.err.println(e);
            cleansedData = inputData;
        }
        inputData.unpersist();
        cleansedData.cache();

        return cleansedData;
    }
    
    /**
     * Build a model using the context.
     * @return build {@link MLModel}
     * @throws MLModelBuilderException if failed to build the model.
     */
    public abstract MLModel build() throws MLModelBuilderException;
    
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
