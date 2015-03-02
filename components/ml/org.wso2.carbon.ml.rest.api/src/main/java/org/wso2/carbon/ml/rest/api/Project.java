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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.dataset.DatasetService;
import org.wso2.carbon.ml.dataset.exceptions.DatasetServiceException;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;
import org.wso2.carbon.ml.project.mgt.exceptions.MLProjectManagementServiceException;
import org.wso2.carbon.ml.rest.api.model.ProjectModel;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/")
public class Project extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(Project.class);

    /**
     * Create a project with a dataset.
     * 
     * @param projectName   Name of the project. Should be Unique for a Tenant.
     * @param datasetUri    URI of the dataset.
     * @param description   Project description
     * @param JWTToken      JWT Token
     * @return              ProjectModel. HTTP 500 Internal Server Error. HTTP 401 Unauthorized.
     */
    @SuppressWarnings("deprecation")
    @Path("/projects")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProject(@QueryParam("name") String projectName, 
                                  @QueryParam("datasetUri") String datasetUri,
                                  @QueryParam("desciption") String description,
                                  @HeaderParam("X-JWT-Assertion") String JWTToken) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String projectID = carbonContext.getTenantId() + "." + projectName;
        try {
            DatasetService datasetService = (DatasetService) carbonContext.getOSGiService(DatasetService.class, null);
            ProjectManagementService projectManagementService = (ProjectManagementService) carbonContext.getOSGiService(
                    ProjectManagementService.class,null);
            File file = new File(datasetUri);
            // Upload the file
            datasetService.uploadDataset(new FileInputStream(file), file.getName(), projectID);
            // Create a project
            projectManagementService.createProject(projectID, projectName, description);
            // Calculate Summary Stats
            datasetService.calculateSummaryStatistics(file.getName(), projectID, projectID);
            ProjectModel project = new ProjectModel(projectID, projectName, description);
            return Response.ok(project).build();
        } catch (FileNotFoundException e) {
            logger.error("Couldn't find the dataset: " + datasetUri + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (DatasetServiceException e) {
            logger.error("Error occured while uploading the dataset : " + datasetUri + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IOException e) {
            logger.error("Error occured while reading the dataset : " + datasetUri + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (MLProjectManagementServiceException e) {
            logger.error("Error occured while creating project : " + projectID + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /**
     * Retrieve details (Name, Description, DatasetUri) of a project.
     * 
     * @param projectId     Unique Identifier of the project to be retrieved
     * @return              ProjectModel. HTTP 500 Internal Server Error.
     */
    @GET
    @Path("/projects/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProject(@PathParam("id") String projectId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            ProjectManagementService projectManagementService = (ProjectManagementService) carbonContext.getOSGiService(
                    ProjectManagementService.class, null);
            String projectDetails[] = projectManagementService.getProject(projectId);
            ProjectModel project = new ProjectModel(projectId, projectDetails[0], projectDetails[1]);
            return Response.ok(project).build();
        } catch (MLProjectManagementServiceException e) {
            logger.error("Error occured while retrieving details of project : " + projectId + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /**
     * Retrieve details (Name, Description, DatssetUri) of all the projects for a tenant.
     * 
     * @param projectId     Unique Identifier of the project to be retrieved
     * @return              ProjectModel. HTTP 500 Internal Server Error.
     */
    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProjects() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            ProjectManagementService projectManagementService = (ProjectManagementService) carbonContext.getOSGiService(
                    ProjectManagementService.class, null);
            List<org.wso2.carbon.ml.project.mgt.dto.Project> projects = projectManagementService.getAllProjects(
                    String.valueOf(carbonContext.getTenantId()));
            ProjectModel []projectModel = new ProjectModel[projects.size()];
            for(int i=0 ; i< projects.size() ; i++){
                projectModel[i] = new ProjectModel(projects.get(i).getProjectId(), projects.get(i).getProjectName(), 
                                                   projects.get(i).getProjectDescription());
            }
            return Response.ok(projectModel).build();
        } catch (MLProjectManagementServiceException e) {
            logger.error("Error occured while retrieving the projects : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /**
     * Delete a project.
     * 
     * @param projectId     Unique Identifier of the project to be deleted
     * @return              Deleted ProjectModel. HTTP 500 Internal Server Error.
     */
    @DELETE
    @Path("/projects/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProject(@PathParam("id") String projectId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            ProjectManagementService projectManagementService = (ProjectManagementService) carbonContext.getOSGiService(
                    ProjectManagementService.class, null);
            String projectDetails[] = projectManagementService.getProject(projectId);
            projectManagementService.deleteProject(projectId);
            ProjectModel project = new ProjectModel(projectId, projectDetails[0], projectDetails[1]);
            return Response.ok(project).build();
        } catch (MLProjectManagementServiceException e) {
            logger.error("Error occured while deleting project : " + projectId + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
