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
package org.wso2.carbon.ml.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.impl.MLDatasetProcessor;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;
import org.wso2.carbon.ml.project.mgt.exceptions.MLProjectManagementServiceException;
import org.wso2.carbon.ml.rest.api.model.AnalysisModel;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/datasets")
public class DatasetApiV10 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(DatasetApiV10.class);
    private MLDatasetProcessor datasetProcessor;
    
    public DatasetApiV10() {
        datasetProcessor = new MLDatasetProcessor();
    }

    /**
     * Create a new Analysis for a project.
     *  
     * @param projectId     Unique identifier of the project.
     * @param analysisName  Name for the analysis.
     * @return              AnalysisModel. HTTP 500 Internal Server Error.
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response uploadDataset(MLDataset dataset) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            dataset.setTenantId(tenantId);
            dataset.setUserName(userName);
            datasetProcessor.process(dataset);
            
            return Response.ok(dataset).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occured while uploading a dataset : " + dataset+ " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
