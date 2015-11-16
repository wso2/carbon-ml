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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.ClusterPoint;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.commons.domain.ScatterPlotPoints;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.exceptions.MLInputValidationException;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.MLDatasetProcessor;
import org.wso2.carbon.ml.core.impl.MLModelHandler;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.rest.api.model.MLDatasetBean;
import org.wso2.carbon.ml.rest.api.model.MLErrorBean;
import org.wso2.carbon.ml.rest.api.model.MLVersionBean;

/**
 * This class defines the ML dataset API.
 */
@Path("/datasets")
public class DatasetApiV10 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(DatasetApiV10.class);
    /*
     * Delegates all the dataset related operations.
     */
    private MLDatasetProcessor datasetProcessor;
    private MLModelHandler mlModelHandler;

    public DatasetApiV10() {
        datasetProcessor = new MLDatasetProcessor();
        mlModelHandler = new MLModelHandler();
    }

    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }

    /**
     * Upload a new dataset.
     *
     * @param datasetName Name of the dataset
     * @param version Version of the dataset
     * @param description Description of the dataset
     * @param sourceType Storage type of the source of the dataset (file/HDFS/WSO2 DAS)
     * @param destination Storage type of the server side copy of the dataset (file/HDFS)
     * @param sourcePath Absolute URI of the dataset if source type is DAS
     * @param dataFormat Format of the dataset
     * @param containsHeader Header availability of the dataset
     * @param inputStream Input stream of the dataset
     * @return JSON of {@link org.wso2.carbon.ml.commons.domain.MLDataset} object
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDataset(@Multipart("datasetName") String datasetName, @Multipart("version") String version,
            @Multipart("description") String description, @Multipart("sourceType") String sourceType,
            @Multipart("destination") String destination, @Multipart("sourcePath") String sourcePath,
            @Multipart("dataFormat") String dataFormat, @Multipart("containsHeader") boolean containsHeader,
            @Multipart("file") InputStream inputStream) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        MLDataset dataset = new MLDataset();
        try {
            // validate input parameters
            if (sourceType == null || sourceType.isEmpty()) {
                String msg = "Required parameters are missing.";
                logger.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(new MLErrorBean(msg)).build();
            }

            dataset.setName(datasetName);
            dataset.setVersion(version);
            dataset.setSourcePath(sourcePath);
            dataset.setDataSourceType(sourceType);
            dataset.setComments(description);
            dataset.setDataTargetType(destination);
            dataset.setDataType(dataFormat);
            dataset.setTenantId(tenantId);
            dataset.setUserName(userName);
            dataset.setContainsHeader(containsHeader);

            datasetProcessor.process(dataset, inputStream);
            return Response.ok(dataset).build();
        } catch (MLInputValidationException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while uploading a [dataset] %s of tenant [id] %s and [user] %s .", dataset,
                    tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
                    .entity(new MLErrorBean(e.getMessage())).build();

        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while uploading a [dataset] %s of tenant [id] %s and [user] %s .", dataset,
                    tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all datasets of this tenant and user.
     *
     * @param status Status of the dataset
     * @return JSON array of {@link org.wso2.carbon.ml.rest.api.model.MLDatasetBean} objects
     */
    @GET
    @Produces("application/json")
    public Response getAllDatasets(@QueryParam("status") String status) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLDatasetBean> datasetBeans = new ArrayList<MLDatasetBean>();
            List<MLDataset> datasets = datasetProcessor.getAllDatasets(tenantId, userName);
            for (MLDataset dataset : datasets) {
                MLDatasetBean datasetBean = new MLDatasetBean();
                datasetBean.setId(dataset.getId());
                datasetBean.setName(dataset.getName());
                datasetBean.setComments(dataset.getComments());
                datasetBean.setStatus(dataset.getStatus());
                if(status != null) {
                    if(status.equals(datasetBean.getStatus())) {
                        datasetBeans.add(datasetBean);
                    }
                }
                else {
                    datasetBeans.add(datasetBean);
                }
            }
            return Response.ok(datasetBeans).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while retrieving all datasets of tenant [id] %s and [user] %s .", tenantId,
                    userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all datasets with their versions.
     *
     * @return JSON array of {@link org.wso2.carbon.ml.rest.api.model.MLDatasetBean} objects
     */
    @GET
    @Path("/versions")
    @Produces("application/json")
    public Response getAllDatasetVersions() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLDatasetBean> datasetBeans = new ArrayList<MLDatasetBean>();
            List<MLDataset> datasets = datasetProcessor.getAllDatasets(tenantId, userName);
            for (MLDataset mlDataset : datasets) {
                MLDatasetBean datasetBean = new MLDatasetBean();
                long datasetId = mlDataset.getId();
                datasetBean.setId(datasetId);
                datasetBean.setName(mlDataset.getName());
                datasetBean.setComments(mlDataset.getComments());
                datasetBean.setStatus(mlDataset.getStatus());
                List<MLVersionBean> versionBeans = new ArrayList<MLVersionBean>();
                List<MLDatasetVersion> versions = datasetProcessor.getAllVersionsetsOfDataset(tenantId, userName,
                        datasetId);
                for (MLDatasetVersion mlDatasetVersion : versions) {
                    MLVersionBean versionBean = new MLVersionBean();
                    versionBean.setId(mlDatasetVersion.getId());
                    versionBean.setVersion(mlDatasetVersion.getVersion());
                    versionBean.setStatus(mlDatasetVersion.getStatus());
                    versionBeans.add(versionBean);
                }
                datasetBean.setVersions(versionBeans);
                datasetBeans.add(datasetBean);
            }
            return Response.ok(datasetBeans).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while retrieving all dataset versions of tenant [id] %s and [user] %s .", tenantId,
                    userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the dataset of a given dataset ID.
     *
     * @param datasetId ID of the dataset
     * @return JSON of {@link org.wso2.carbon.ml.commons.domain.MLDataset} object
     */
    @GET
    @Path("/{datasetId}")
    @Produces("application/json")
    public Response getDataset(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLDataset dataset = datasetProcessor.getDataset(tenantId, userName, datasetId);
            if (dataset == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(dataset).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while retrieving the dataset with the [id] %s of tenant [id] %s and [user] %s .",
                    datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the status of a dataset.
     *
     * @param datasetId ID of the dataset
     * @return JSON string of the dataset status
     */
    @GET
    @Path("/{datasetId}/status")
    @Produces("application/json")
    public Response getDatasetStatus(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLDataset dataset = datasetProcessor.getDataset(tenantId, userName, datasetId);
            if (dataset == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            // create a JSON like string to be parsed
            String responseString = "{\"status\":\"" + dataset.getStatus() + "\"}";
            return Response.ok(responseString).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while retrieving the dataset status with the [id] %s of tenant [id] %s and [user] %s .",
                    datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all versions of a dataset.
     *
     * @param datasetId ID of the dataset
     * @return JSON array of {@link org.wso2.carbon.ml.commons.domain.MLDatasetVersion} objects
     */
    @GET
    @Path("/{datasetId}/versions")
    @Produces("application/json")
    public Response getAllVersionsets(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLDatasetVersion> versionsets = datasetProcessor.getAllDatasetVersions(tenantId, userName, datasetId);
            return Response.ok(versionsets).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving all versions of a dataset with the [id] %s of tenant [id] %s and [user] %s .",
                                    datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get dataset version for a given dataset ID and version.
     *
     * @param datasetId ID of the dataset
     * @param version Version of the dataset
     * @return JSON of {@link org.wso2.carbon.ml.commons.domain.MLDatasetVersion} object
     */
    @GET
    @Path("/{datasetId}/versions/{version}")
    @Produces("application/json")
    public Response getVersionset(@PathParam("datasetId") long datasetId, @PathParam("version") String version) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLDatasetVersion versionSet = datasetProcessor.getVersionSetWithVersion(tenantId, userName, datasetId,
                    version);
            if (versionSet == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(versionSet).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving the version set with [version] %s of a dataset with the [id] %s of tenant [id] %s and [user] %s .",
                                    version, datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get dataset version for a given dataset version ID.
     *
     * @param versionsetId ID of the dataset version
     * @return JSON of {@link org.wso2.carbon.ml.commons.domain.MLDatasetVersion} object
     */
    @GET
    @Path("/versions/{versionsetId}")
    @Produces("application/json")
    public Response getVersionset(@PathParam("versionsetId") long versionsetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLDatasetVersion versionset = datasetProcessor.getVersionset(tenantId, userName, versionsetId);
            if (versionset == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(versionset).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving the version set with the [id] %s of tenant [id] %s and [user] %s .",
                                    versionsetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Get sample points of a dataset version.
     *
     * @param versionsetId ID of the dataset version
     * @return JSON of {@link org.wso2.carbon.ml.commons.domain.SamplePoints} object
     */
    @GET
    @Path("/versions/{versionsetId}/sample")
    @Produces("application/json")
    public Response getSamplePoints(@PathParam("versionsetId") long versionsetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            SamplePoints samplePoints = datasetProcessor.getSamplePoints(tenantId, userName, versionsetId);
            if (samplePoints == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(samplePoints).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving the sample set for the version set [id] %s of tenant [id] %s and [user] %s .",
                                    versionsetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get scatter plot points of the latest version of a dataset.
     *
     * @param datasetId ID of the dataset
     * @param scatterPlotPoints Scatter plot features
     * @return JSON array of scatter plot points
     */
    @POST
    @Path("/{datasetId}/scatter")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getScatterPlotPointsOfLatestVersion(@PathParam("datasetId") long datasetId,
            ScatterPlotPoints scatterPlotPoints) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            scatterPlotPoints.setTenantId(tenantId);
            scatterPlotPoints.setUser(userName);
            List<Object> points = datasetProcessor.getScatterPlotPointsOfLatestVersion(datasetId, scatterPlotPoints);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving scatter plot points for latest version of dataset [id] %s of tenant [id] %s and [user] %s .",
                                    datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get scatter plot points of a dataset version.
     *
     * @param versionsetId ID of the dataset version
     * @param scatterPlotPoints Scatter plot features
     * @return JSON array of scatter plot points
     */
    @POST
    @Path("/versions/{versionsetId}/scatter")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getScatterPlotPoints(@PathParam("versionsetId") long versionsetId,
            ScatterPlotPoints scatterPlotPoints) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            scatterPlotPoints.setTenantId(tenantId);
            scatterPlotPoints.setUser(userName);
            scatterPlotPoints.setVersionsetId(versionsetId);
            List<Object> points = datasetProcessor.getScatterPlotPoints(scatterPlotPoints);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving scatter plot points of dataset version [id] %s of tenant [id] %s and [user] %s .",
                                    versionsetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get chart sample points of the latest version of a dataset.
     *
     * @param datasetId ID of the dataset
     * @param featureListString List of features (single string containing comma separated features)
     * @return JSON array of chart sample points
     */
    @GET
    @Path("/{datasetId}/charts")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getChartSamplePointsOfLatestVersion(@PathParam("datasetId") long datasetId,
            @QueryParam("features") String featureListString) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<Object> points = datasetProcessor.getChartSamplePointsOfLatestVersion(tenantId, userName, datasetId,
                    featureListString);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving chart sample points for latest version of dataset [id] %s of tenant [id] %s and [user] %s .",
                                    datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get chart sample points of a dataset version.
     *
     * @param versionsetId ID of the dataset version
     * @param featureListString List of features (single string containing comma separated features)
     * @return JSON array of chart sample points
     */
    @GET
    @Path("/versions/{versionsetId}/charts")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getChartSamplePoints(@PathParam("versionsetId") long versionsetId,
            @QueryParam("features") String featureListString) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<Object> points = datasetProcessor.getChartSamplePoints(tenantId, userName, versionsetId,
                    featureListString);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving chart sample points of dataset version [id] %s of tenant [id] %s and [user] %s .",
                                    versionsetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get cluster points of a dataset version.
     *
     * @param datasetId ID of the dataset
     * @param featureListString List of features (single string containing comma separated features)
     * @param noOfClusters No of clusters
     * @return JSON array of {@link org.wso2.carbon.ml.commons.domain.ClusterPoint} objects
     */
    @GET
    @Path("/{datasetId}/cluster")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getClusterPoints(@PathParam("datasetId") long datasetId,
            @QueryParam("features") String featureListString, @QueryParam("noOfClusters") int noOfClusters) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<ClusterPoint> points = mlModelHandler.getClusterPoints(tenantId, userName, datasetId,
                    featureListString, noOfClusters);
            return Response.ok(points).build();
        } catch (MLMalformedDatasetException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving cluster points with [features] %s and [number of clusters] %s of dataset [id] %s of tenant [id] %s and [user] %s .",
                                    featureListString, noOfClusters, datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(new MLErrorBean(e.getMessage())).build();
        } catch (MLModelHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving cluster points with [features] %s and [number of clusters] %s of dataset [id] %s of tenant [id] %s and [user] %s .",
                                    featureListString, noOfClusters, datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get filtered feature names of a dataset.
     *
     * @param datasetId ID of the dataset
     * @param featureType Feature type
     * @return JSON array of features
     */
    @GET
    @Path("/{datasetId}/filteredFeatures")
    @Produces("application/json")
    public Response getFilteredFeatures(@PathParam("datasetId") long datasetId,
            @QueryParam("featureType") String featureType) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {

            List<String> features = datasetProcessor.getFeatureNames(datasetId, featureType);
            return Response.ok(features).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving feature names with [type] %s for the dataset [id] %s of tenant [id] %s and [user] %s .",
                                    featureType, datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get summary statistics of a dataset feature.
     *
     * @param datasetId ID of the dataset
     * @param featureName Name of the feature
     * @return JSON of summary string
     */
    @GET
    @Path("/{datasetId}/stats")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getSummaryStatistics(@PathParam("datasetId") long datasetId,
            @QueryParam("feature") String featureName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {

            String summary = datasetProcessor.getSummaryStats(datasetId, featureName);
            return Response.ok(summary).build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving summarized stats of feature [name] %s for the dataset [id] %s of tenant [id] %s and [user] %s .",
                                    featureName, datasetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Delete the dataset for a given dataset ID.
     *
     * @param datasetId ID of the dataset
     */
    @DELETE
    @Path("/{datasetId}")
    @Produces("application/json")
    public Response deleteDataset(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            datasetProcessor.deleteDataset(tenantId, userName, datasetId);
            return Response.ok().build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while deleting dataset [id] %s of tenant [id] %s and [user] %s .", datasetId,
                    tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Delete the dataset version for a given dataset version ID.
     *
     * @param versionsetId ID of the dataset version
     */
    @DELETE
    @Path("/versions/{versionsetId}")
    @Produces("application/json")
    public Response deleteDatasetVersion(@PathParam("versionsetId") long versionsetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            datasetProcessor.deleteDatasetVersion(tenantId, userName, versionsetId);
            return Response.ok().build();
        } catch (MLDataProcessingException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while deleting dataset version [id] %s of tenant [id] %s and [user] %s .",
                    versionsetId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }
}
