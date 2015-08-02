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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.FeatureSummary;
import org.wso2.carbon.ml.commons.domain.MLAnalysis;
import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLHyperParameter;
import org.wso2.carbon.ml.commons.domain.MLModelConfiguration;
import org.wso2.carbon.ml.commons.domain.MLModelData;
import org.wso2.carbon.ml.core.exceptions.MLAnalysisHandlerException;
import org.wso2.carbon.ml.core.impl.MLAnalysisHandler;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.rest.api.model.MLErrorBean;

/**
 * WSO2 ML Analyses API. All the operations related to Analyses are delegated from this class.
 */
@Path("/analyses")
public class AnalysisApiV10 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(AnalysisApiV10.class);
    /*
     * Analysis handler which is doing the real work.
     */
    private MLAnalysisHandler mlAnalysisHandler;

    public AnalysisApiV10() {
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
     * Create a new Analysis of a project.
     */
    @POST
    @Produces("application/json")
    public Response createAnalysis(MLAnalysis analysis) {
        if (analysis.getName() == null || analysis.getName().isEmpty() || analysis.getProjectId() == 0) {
            String msg = "Required parameters are missing: " + analysis;
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
     * Adding customized features of this analysis.
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
     * load default features as customized features of this analysis.
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
     * get summarized features of an analysis.
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
     * get filtered feature names of an analysis.
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
     * get all feature names of an analysis.
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
     * get the response variable of an analysis.
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
     * get the algorithm name of an analysis.
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
            String responseVariable = mlAnalysisHandler.getAlgorithmName(analysisId);
            return Response.ok(responseVariable).build();
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
     * get the algorithm type of an analysis.
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
     * get the train data fraction of an analysis.
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
            double responseVariable = mlAnalysisHandler.getTrainDataFraction(analysisId);
            return Response.ok(responseVariable).build();
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
     * get summarized statistics of a feature of an analysis.
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
     * Adding configurations (algorithm etc.) of this analysis.
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
     * Adding hyper parameters for the selected algorithm of this analysis.
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
     * get hyper-parameters of an analysis.
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
            List<MLHyperParameter> responseVariable = mlAnalysisHandler.getHyperParameters(analysisId, algorithmName);
            return Response.ok(responseVariable).build();
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
     * loading default configurations as configurations (algorithm etc.) of this analysis.
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
     * Get all models of a given analysis.
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
     * delete an analysis of a given id.
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
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            String msg = MLUtils.getErrorMsg(String.format(
                    "Error occurred while deleting an analysis [id] %s of tenant [id] %s and [user] %s .", analysisId,
                    tenantId, userName), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MLErrorBean(e.getMessage()))
                    .build();
        }
    }
}
