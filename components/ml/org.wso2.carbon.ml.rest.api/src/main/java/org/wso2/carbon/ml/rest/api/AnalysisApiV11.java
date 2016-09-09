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

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.core.exceptions.MLAnalysisHandlerException;
import org.wso2.carbon.ml.core.impl.MLAnalysisHandler;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.rest.api.model.MLAnalysisConfigsBean;
import org.wso2.carbon.ml.rest.api.model.MLErrorBean;

/**
 * WSO2 ML Analyses API. All the operations related to analyses are delegated from this class.
 */
@Path("/analyses")
public class AnalysisApiV11 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(AnalysisApiV11.class);
    /*
     * Analysis handler which is doing the real work.
     */
    private MLAnalysisHandler mlAnalysisHandler;

    public AnalysisApiV11() {
        mlAnalysisHandler = new MLAnalysisHandler();
    }

    /**
     * HTTP Options method implementation for analysis API.
     *
     * @return
     */
    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }

    /**
     * Create a new analysis of a project.
     * @param analysis {@link MLAnalysis} object
     */
    @POST
    @Produces("application/json")
    public Response createAnalysis(MLAnalysis analysis) {
        String analysisName = analysis.getName();
        if (analysisName == null || analysisName.isEmpty() || analysis.getProjectId() == 0) {
            String msg = "Analysis name or project Id is missing: " + analysis;
            logger.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(new MLErrorBean(msg)).build();
        }
        if (!MLUtils.isValidName(analysisName)) {
            String msg = "analysis name: " + analysisName + " contains invalid characters.";
            logger.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(new MLErrorBean(msg)).build();
        }
        
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            analysis.setTenantId(tenantId);
            analysis.setUserName(userName);
            mlAnalysisHandler.createAnalysis(analysis);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while creating an [analysis] %s of tenant [id] %s and [user] %s .", analysis,
                    tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Adding customized features of an analysis.
     * @param analysisId Unique id of the analysis
     * @param customizedFeatures {@link List} of {@link MLCustomizedFeature} objects
     */
    @POST
    @Path("/{analysisId}/features")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addCustomizedFeatures(@PathParam("analysisId") long analysisId,
            List<MLCustomizedFeature> customizedFeatures) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addCustomizedFeatures(analysisId, customizedFeatures, tenantId, userName);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while adding customized features for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Set default features as customized features of an analysis.
     * @param analysisId Unique id of the analysis
     * @param customizedValues {@link MLCustomizedFeature} object
     */
    @POST
    @Path("/{analysisId}/features/defaults")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addDefaultsIntoCustomizedFeatures(@PathParam("analysisId") long analysisId,
            MLCustomizedFeature customizedValues) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            customizedValues.setTenantId(tenantId);
            customizedValues.setUserName(userName);
            customizedValues.setLastModifiedUser(userName);
            mlAnalysisHandler.addDefaultsIntoCustomizedFeatures(analysisId, customizedValues);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while adding default features into customized features for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get summarized features of an analysis.
     * @param analysisId Unique id of the analysis
     * @param limit Number of features need to retrieve, from the starting index
     * @param offset Starting index
     * @return JSON array of {@link FeatureSummary} objects
     */
    @GET
    @Path("/{analysisId}/summarizedFeatures")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getSummarizedFeatures(@PathParam("analysisId") long analysisId, @QueryParam("limit") int limit,
            @QueryParam("offset") int offset) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {

            List<FeatureSummary> features = mlAnalysisHandler.getSummarizedFeatures(tenantId, userName, analysisId,
                    limit, offset);
            return Response.ok(features).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving summarized features for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get customized features of an analysis.
     * @param analysisId Unique id of the analysis
     * @param limit Number of features need to retrieve, from the starting index
     * @param offset Starting index
     * @return JSON array of {@link MLCustomizedFeature} objects
     */
    @GET
    @Path("/{analysisId}/customizedFeatures")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getCustomizedFeatures(@PathParam("analysisId") long analysisId, @QueryParam("limit") int limit,
                                          @QueryParam("offset") int offset) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLCustomizedFeature> customizedFeatures = mlAnalysisHandler.getCustomizedFeatures(tenantId, userName, analysisId,
                    limit, offset);
            return Response.ok(customizedFeatures).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving customized features for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get configurations of an analysis.
     * @param analysisId Unique id of the analysis
     * @param limit Number of features included in the analysis configuration
     * @param offset Starting index of the features
     * @return JSON array of {@link MLAnalysisConfigsBean} objects
     */
    @GET
    @Path("/{analysisId}/configs")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getConfigs(@PathParam("analysisId") long analysisId, @QueryParam("limit") int limit,
                               @QueryParam("offset") int offset) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLAnalysisConfigsBean mlAnalysisConfigsBean = new MLAnalysisConfigsBean();
            mlAnalysisConfigsBean.setId(analysisId);
            mlAnalysisConfigsBean.setCustomizedFeatures(mlAnalysisHandler.getCustomizedFeatures(tenantId, userName, analysisId,
                    limit, offset));
            mlAnalysisConfigsBean.setAlgorithmName(mlAnalysisHandler.getAlgorithmName(analysisId));
            mlAnalysisConfigsBean.setResponseVariable(mlAnalysisHandler.getResponseVariable(analysisId));
            mlAnalysisConfigsBean.setTrainDataFraction(mlAnalysisHandler.getTrainDataFraction(analysisId));
            mlAnalysisConfigsBean.setNormalLabels(mlAnalysisHandler.getNormalLabels(analysisId));
            mlAnalysisConfigsBean.setNormalization(Boolean.parseBoolean(mlAnalysisHandler.getNormalization(analysisId)));
            mlAnalysisConfigsBean.setNewNormalLabel(mlAnalysisHandler.getNewNormalLabel(analysisId));
            mlAnalysisConfigsBean.setNewAnomalyLabel(mlAnalysisHandler.getNewAnomalyLabel(analysisId));
            mlAnalysisConfigsBean.setUserVariable(mlAnalysisHandler.getUserVariable(analysisId));
            mlAnalysisConfigsBean.setProductVariable(mlAnalysisHandler.getProductVariable(analysisId));
            mlAnalysisConfigsBean.setRatingVariable(mlAnalysisHandler.getRatingVariable(analysisId));
            mlAnalysisConfigsBean.setObservations(mlAnalysisHandler.getObservations(analysisId));
            mlAnalysisConfigsBean.setHyperParameters(mlAnalysisHandler.getHyperParameters(analysisId, mlAnalysisHandler.getAlgorithmName(analysisId)));
            return Response.ok(mlAnalysisConfigsBean).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving configurations for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get filtered feature names of an analysis.
     * @param analysisId Unique id of the analysis
     * @param featureType Feature type need to retrieve (Categorical or Numerical)
     * @return JSON array of feature names
     */
    @GET
    @Path("/{analysisId}/filteredFeatures")
    @Produces("application/json")
    public Response getfilteredFeatures(@PathParam("analysisId") String analysisId,
            @QueryParam("featureType") String featureType) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {

            List<String> features = mlAnalysisHandler.getFeatureNames(analysisId, featureType);
            return Response.ok(features).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving filtered feature names for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all feature names of an analysis.
     * @param analysisId Unique id of the analysis
     * @return JSON array of feature names
     */
    @GET
    @Path("/{analysisId}/features")
    @Produces("application/json")
    public Response getAllFeatures(@PathParam("analysisId") String analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {

            List<String> features = mlAnalysisHandler.getFeatureNames(analysisId);
            return Response.ok(features).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving all feature names for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the response variable of an analysis.
     * @param analysisId Unique id of the analysis
     * @return Response variable name
     */
    @GET
    @Path("/{analysisId}/responseVariables")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getResponseVariable(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String responseVariable = mlAnalysisHandler.getResponseVariable(analysisId);
            return Response.ok(responseVariable).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving response variable for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the algorithm name of an analysis.
     * @param analysisId Unique id of the analysis
     * @return Algorithm name
     */
    @GET
    @Path("/{analysisId}/algorithmName")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getAlgorithmName(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String algorithmName = mlAnalysisHandler.getAlgorithmName(analysisId);
            return Response.ok(algorithmName).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving algorithm name for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the algorithm type of an analysis.
     * @param analysisId Unique id of the analysis
     * @return Algorithm type
     */
    @GET
    @Path("/{analysisId}/algorithmType")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getAlgorithmType(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String responseVariable = mlAnalysisHandler.getAlgorithmType(analysisId);
            return Response.ok(responseVariable).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving algorithm type for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the normal labels of an analysis.
     * @param analysisId Unique id of the analysis
     * @return Normal Labels
     */
    @GET
    @Path("/{analysisId}/normalLabels")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getNormalLabels(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String normalLabels = mlAnalysisHandler.getNormalLabels(analysisId);
            return Response.ok(normalLabels).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving normal labels for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the normalization option of an analysis.
     * @param analysisId Unique id of the analysis
     * @return Normalization option
     */
    @GET
    @Path("/{analysisId}/normalization")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getNormalization(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String normalLabels = mlAnalysisHandler.getNormalization(analysisId);
            return Response.ok(normalLabels).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving data normalization selection for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the new normal label of an analysis.
     * @param analysisId Unique id of the analysis
     * @return New Normal Label
     */
    @GET
    @Path("/{analysisId}/newNormalLabel")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getNewNormalLabel(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String normalLabels = mlAnalysisHandler.getNewNormalLabel(analysisId);
            return Response.ok(normalLabels).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving data new normal label for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the new anomaly labels of an analysis.
     * @param analysisId Unique id of the analysis
     * @return New Anomaly Label
     */
    @GET
    @Path("/{analysisId}/newAnomalyLabel")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getNewAnomalyLabel(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String normalLabels = mlAnalysisHandler.getNewAnomalyLabel(analysisId);
            return Response.ok(normalLabels).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving new anomaly label for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get the train data fraction of an analysis.
     * @param analysisId Unique id of the analysis
     * @return Train data fraction
     */
    @GET
    @Path("/{analysisId}/trainDataFraction")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getTrainDataFraction(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            double trainDataFraction = mlAnalysisHandler.getTrainDataFraction(analysisId);
            return Response.ok(trainDataFraction).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving train data fraction for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get summary statistics of a feature of an analysis.
     * @param analysisId Unique id of the analysis
     * @param featureName Name of the feature
     * @return Summary statistics of the feature
     */
    @GET
    @Path("/{analysisId}/stats")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getSummaryStatistics(@PathParam("analysisId") long analysisId,
            @QueryParam("feature") String featureName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {

            if (featureName == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new MLErrorBean("feature query param was not set.")).build();
            }

            String summary = mlAnalysisHandler.getSummaryStats(tenantId, userName, analysisId, featureName);
            return Response.ok(summary).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving summarized stats of feature [name] %s for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    featureName, analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Add configurations of an analysis.
     * @param analysisId Unique id of the analysis
     * @param modelConfigs {@link List} of {@link MLModelConfiguration} objects
     */
    @POST
    @Path("/{analysisId}/configurations")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addModelConfiguration(@PathParam("analysisId") long analysisId,
            List<MLModelConfiguration> modelConfigs) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addModelConfigurations(analysisId, modelConfigs);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while adding model configurations for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Add hyper-parameters for the selected algorithm of an analysis.
     * @param analysisId Unique id of the analysis
     * @param hyperParameters {@link List} of {@link MLHyperParameter} objects
     * @param algorithmName Algorithm name
     */
    @POST
    @Path("/{analysisId}/hyperParams")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addHyperParameters(@PathParam("analysisId") long analysisId,
            List<MLHyperParameter> hyperParameters, @QueryParam("algorithmName") String algorithmName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addHyperParameters(analysisId, hyperParameters, algorithmName);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while adding hyper parameters for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get hyper-parameter of an analysis.
     * @param analysisId Unique id of the analysis
     * @param algorithmName Algorithm name
     * @return JSON array of {@link MLHyperParameter} objects
     */
    @GET
    @Path("/{analysisId}/hyperParameters")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getHyperParameters(@PathParam("analysisId") long analysisId,
            @QueryParam("algorithmName") String algorithmName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLHyperParameter> hyperParameters = mlAnalysisHandler.getHyperParameters(analysisId, algorithmName);
            return Response.ok(hyperParameters).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving hyper parameters of algorithm [name] %s for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    algorithmName, analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Load default configurations as configurations of an analysis.
     * @param analysisId Unique id of the analysis
     */
    @POST
    @Path("/{analysisId}/hyperParams/defaults")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addDefaultsIntoHyperParameters(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addDefaultsIntoHyperParameters(analysisId);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while loading default hyper parameters for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieve all analyses.
     * @return JSON array of {@link MLAnalysis} objects
     */
    @GET
    @Produces("application/json")
    public Response getAllAnalyses() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLAnalysis> analyses = mlAnalysisHandler.getAnalyses(tenantId, userName);
            return Response.ok(analyses).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while retrieving all analyses of tenant [id] %s and [user] %s .", tenantId,
                    userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all models of an analysis.
     * @param analysisId Unique id of the analysis
     * @return JSON array of {@link MLModelData} objects
     */
    @GET
    @Path("/{analysisId}/models")
    @Produces("application/json")
    public Response getAllModelsOfAnalysis(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLModelData> models = mlAnalysisHandler.getAllModelsOfAnalysis(tenantId, userName, analysisId);
            return Response.ok(models).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while retrieving all models of analysis [id] %s of tenant [id] %s and [user] %s .",
                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * Delete an analysis of a given ID.
     * @param analysisId Unique id of the analysis
     */
    @DELETE
    @Path("/{analysisId}")
    @Produces("application/json")
    public Response deleteAnalysis(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.deleteAnalysis(tenantId, userName, analysisId);
            auditLog.info(String.format("User [name] %s of tenant [id] %s deleted an analysis [id] %s ", userName,
                    tenantId, analysisId));
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while deleting an analysis [id] %s of tenant [id] %s and [user] %s .", analysisId,
                    tenantId, userName), e);
            logger.error(msg, e);
            auditLog.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }

    /**
     * get the user variable of an analysis.
     */
    @GET
    @Path("/{analysisId}/userVariable")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getUserVariable(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String userVariable = mlAnalysisHandler.getUserVariable(analysisId);
            return Response.ok(userVariable).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving user variable for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                           .build();
        }
    }

    /**
     * get the product variable of an analysis.
     */
    @GET
    @Path("/{analysisId}/productVariable")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getProductVariable(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String productVariable = mlAnalysisHandler.getProductVariable(analysisId);
            return Response.ok(productVariable).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving product variable for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                           .build();
        }
    }

    /**
     * get the rating variable of an analysis.
     */
    @GET
    @Path("/{analysisId}/ratingVariable")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getRatingVariable(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            String ratingVariable = mlAnalysisHandler.getRatingVariable(analysisId);
            return Response.ok(ratingVariable).build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils
                    .getErrorMsg(
                            String.format(
                                    "Error occurred while retrieving rating variable for the analysis [id] %s of tenant [id] %s and [user] %s .",
                                    analysisId, tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                           .build();
        }
    }

	/**
	 * get the observation list fraction of an analysis.
	 */
	@GET
	@Path("/{analysisId}/observationList")
	@Produces("application/json")
	@Consumes("application/json")
	public Response getObservationList(@PathParam("analysisId") long analysisId) {
		PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		int tenantId = carbonContext.getTenantId();
		String userName = carbonContext.getUsername();
		try {
			String observations = mlAnalysisHandler.getObservations(analysisId);
			return Response.ok(observations).build();
		} catch (MLAnalysisHandlerException e) {
			String msg = MLUtils
					.getErrorMsg(
							String.format(
									"Error occurred while retrieving observations for the analysis [id] %s of tenant [id] %s and [user] %s .",
									analysisId, tenantId, userName), e);
			logger.error(msg, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
			               .build();
		}
	}




}
