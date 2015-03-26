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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.impl.MLDatasetProcessor;
import org.wso2.carbon.ml.rest.api.model.MLDatasetBean;
import org.wso2.carbon.ml.rest.api.model.MLVersionBean;

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
    
    @OPTIONS
    public Response options() {
        return Response.ok()
                .header(HttpHeaders.ALLOW, "GET POST DELETE")
                .build();
    }

    /**
     * Upload a new data-set.
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response uploadDataset(MLDataset dataset) {
        if (dataset.getName() == null || dataset.getName().isEmpty() || dataset.getSourcePath() == null || 
                dataset.getVersion() == null || dataset.getDataSourceType() == null || dataset.getDataSourceType()
                .isEmpty() || dataset.getDataType() == null || dataset.getDataType().isEmpty()) {
            logger.error("Required parameters missing");
            return Response.status(Response.Status.BAD_REQUEST).entity("Required parameters missing").build();
        }
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            dataset.setTenantId(tenantId);
            dataset.setUserName(userName);
            datasetProcessor.process(dataset);
            return Response.ok(dataset).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occured while uploading a dataset : " + dataset + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/getalldatasets")
    @Produces("application/json")
    public Response getAllDatasets() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            List<MLDataset> datasets = datasetProcessor.getAllDatasets(tenantId, userName);
            return Response.ok(datasets).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occured while retrieving all datasets.. : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/versions")
    @Produces("application/json")
    public Response getAllDatasetVersions() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            List<MLDatasetBean> datasetBeans = new ArrayList<MLDatasetBean>();
            List<MLDataset> datasets = datasetProcessor.getAllDatasets(tenantId, userName);
            for (MLDataset mlDataset : datasets) {
                MLDatasetBean datasetBean = new MLDatasetBean();
                long datasetId = mlDataset.getId();
                datasetBean.setId(datasetId);
                datasetBean.setName(mlDataset.getName());
                datasetBean.setComments(mlDataset.getComments());
                List<MLVersionBean> versionBeans = new ArrayList<MLVersionBean>();
                List<MLDatasetVersion> versions = datasetProcessor.getAllVersionsetsOfDataset(tenantId, userName, datasetId);
                for (MLDatasetVersion mlDatasetVersion : versions) {
                    MLVersionBean versionBean = new MLVersionBean();
                    versionBean.setId(mlDatasetVersion.getId());
                    versionBean.setVersion(mlDatasetVersion.getVersion());
                    versionBeans.add(versionBean);
                }
                datasetBean.setVersions(versionBeans);
                datasetBeans.add(datasetBean);
            }
            return Response.ok(datasetBeans).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occured while retrieving all dataset versions : " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/{datasetId}")
    @Produces("application/json")
    public Response getDataset(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            MLDataset dataset = datasetProcessor.getDataset(tenantId, userName, datasetId);
            if (dataset == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(dataset).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occured while retrieving all datasets.. : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/{datasetId}/versions")
    @Produces("application/json")
    public Response getAllVersionsets(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            List<MLDatasetVersion> versionsets = datasetProcessor.getAllDatasetVersions(tenantId, userName, datasetId);
            return Response.ok(versionsets).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occured while retrieving all versions of dataset : " + datasetId+ " : " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/versions/{versionsetId}")
    @Produces("application/json")
    public Response getVersionset(@PathParam("versionsetId") long versionsetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            MLDatasetVersion versionset = datasetProcessor.getVersionset(tenantId, userName, versionsetId);
            if (versionset == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(versionset).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occured while retrieving dataset versions: " + versionsetId + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
