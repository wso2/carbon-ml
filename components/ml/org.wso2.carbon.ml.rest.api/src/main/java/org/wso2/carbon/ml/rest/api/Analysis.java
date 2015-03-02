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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;
import org.wso2.carbon.ml.project.mgt.exceptions.MLProjectManagementServiceException;
import org.wso2.carbon.ml.rest.api.model.AnalysisModel;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/analysis")
public class Analysis extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(Analysis.class);

    /**
     * Create a new Analysis for a project.
     *  
     * @param projectId     Unique identifier of the project.
     * @param analysisName  Name for the analysis.
     * @return              AnalysisModel. HTTP 500 Internal Server Error.
     */
    @POST
    @Produces("application/json")
    public Response createAnalysis(@QueryParam("projectId") String projectId, 
                                   @QueryParam("name") String analysisName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            ProjectManagementService projectManagementService = (ProjectManagementService) carbonContext.getOSGiService(
                    ProjectManagementService.class,null);
            String analysisId = projectManagementService.createWorkflowAndSetDefaultSettings(projectId, analysisName);
            AnalysisModel analysis = new AnalysisModel(analysisId, analysisName, projectId);
            return Response.ok(analysis).build();
        } catch (MLProjectManagementServiceException e) {
            logger.error("Error occured while creating the new analysis : " + analysisName + " for project : " + 
                    projectId + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
