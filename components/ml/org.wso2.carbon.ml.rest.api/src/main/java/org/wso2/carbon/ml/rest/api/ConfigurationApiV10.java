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

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.config.MLAlgorithm;
import org.wso2.carbon.ml.commons.domain.config.SummaryStatisticsSettings;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/configs")
public class ConfigurationApiV10 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(ConfigurationApiV10.class);

    public ConfigurationApiV10() {
    }

    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET").build();
    }

    /**
     * Get all supported algorithms.
     *
     * @return JSON array of {@link org.wso2.carbon.ml.commons.domain.config.MLAlgorithm} objects
     */
    @GET
    @Path("/algorithms")
    @Produces("application/json")
    public Response getAllAlgorithms() {
        List<MLAlgorithm> mlAlgorithms = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        return Response.ok(mlAlgorithms).build();
    }

    /**
     * Get {@link org.wso2.carbon.ml.commons.domain.config.MLAlgorithm} object by algorithm name.
     *
     * @param algorithmName Name of the algorithm
     * @return JSON of {@link org.wso2.carbon.ml.commons.domain.config.MLAlgorithm} object
     */
    @GET
    @Path("/algorithms/{algorithmName}")
    @Produces("application/json")
    public Response getAlgorithm(@PathParam("algorithmName") String algorithmName) {
        if (algorithmName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cannot find the Algorithm name from the URI.")
                    .build();
        }
        List<MLAlgorithm> mlAlgorithms = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        for (MLAlgorithm mlAlgorithm : mlAlgorithms) {
            if (algorithmName.equals(mlAlgorithm.getName())) {
                return Response.ok(mlAlgorithm).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("No algorithm found with the name: " + algorithmName)
                .build();
    }

    /**
     * Get hyper-parameters of an algorithm.
     *
     * @param algorithmName Name of the algorithm
     * @return JSON array of {@link org.wso2.carbon.ml.commons.domain.MLHyperParameter} objects
     */
    @GET
    @Path("/algorithms/{algorithmName}/hyperParams")
    @Produces("application/json")
    public Response getHyperParamsOfAlgorithm(@PathParam("algorithmName") String algorithmName) {
        if (algorithmName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cannot find the Algorithm name from the URI.")
                    .build();
        }
        List<MLAlgorithm> mlAlgorithms = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        for (MLAlgorithm mlAlgorithm : mlAlgorithms) {
            if (algorithmName.equals(mlAlgorithm.getName())) {
                return Response.ok(mlAlgorithm.getParameters()).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("No algorithm found with the name: " + algorithmName)
                .build();
    }

    /**
     * Get available WSO2 DAS tables.
     *
     * @return JSON array of table names
     */
    @GET
    @Path("/das/tables")
    @Produces("application/json")
    public Response getDASTables() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        AnalyticsDataAPI analyticsDataApi = (AnalyticsDataAPI) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(AnalyticsDataAPI.class, null);
        if (analyticsDataApi == null) {
            String msg = String
                    .format("Error occurred while retrieving DAS tables of tenant [id] %s . Cause: AnalyticsDataAPI is null.",
                            tenantId);
            logger.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        List<String> tableNames;
        try {
            tableNames = analyticsDataApi.listTables(tenantId);
        } catch (AnalyticsException e) {
            String msg = MLUtils.getErrorMsg(
                    String.format("Error occurred while retrieving DAS tables of tenant [id] %s .", tenantId), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();

        }
        return Response.ok(tableNames).build();
    }

    /**
     * Get summary statistics settings.
     *
     * @return JSON of {@link org.wso2.carbon.ml.commons.domain.config.SummaryStatisticsSettings} object
     */
    @GET
    @Path("/summaryStatSettings")
    @Produces("application/json")
    public Response getSummaryStatSettings() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        SummaryStatisticsSettings summaryStatisticsSettings = MLCoreServiceValueHolder.getInstance().getSummaryStatSettings();
        if (summaryStatisticsSettings == null) {
            String msg = String
                    .format("Error occurred while retrieving summary statistics settings of tenant [id] %s.",
                            tenantId);
            logger.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.ok(summaryStatisticsSettings).build();
    }
    /**
     * Get PMML availability of an algorithm.
     *
     */
    @GET
    @Path("/pmml/{algorithmName}")
    @Produces("application/json")
    public Response getPMMLAvailability(@PathParam("algorithmName") String algorithmName) {
        if (algorithmName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cannot find the Algorithm name from the URI.")
                    .build();
        }
        List<MLAlgorithm> mlAlgorithms = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        for (MLAlgorithm mlAlgorithm : mlAlgorithms) {
            if (algorithmName.equals(mlAlgorithm.getName()) && mlAlgorithm.getPMMLExportable()) {
                return Response.ok().build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("PMML downoald not supported for : " + algorithmName)
                .build();
    }

}
