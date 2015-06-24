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
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.config.MLAlgorithm;
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

    @GET
    @Path("/algorithms")
    @Produces("application/json")
    public Response getAllAlgorithms() {
        List<MLAlgorithm> algos = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        return Response.ok(algos).build();
    }

    @GET
    @Path("/algorithms/{algorithmName}")
    @Produces("application/json")
    public Response getAlgorithm(@PathParam("algorithmName") String algorithmName) {
        if (algorithmName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cannot find the Algorithm name from the URI.")
                    .build();
        }
        List<MLAlgorithm> algos = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        for (MLAlgorithm mlAlgorithm : algos) {
            if (algorithmName.equals(mlAlgorithm.getName())) {
                return Response.ok(mlAlgorithm).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("No algorithm found with the name: " + algorithmName)
                .build();
    }

    @GET
    @Path("/algorithms/{algorithmName}/hyperParams")
    @Produces("application/json")
    public Response getHyperParamsOfAlgorithm(@PathParam("algorithmName") String algorithmName) {
        if (algorithmName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cannot find the Algorithm name from the URI.")
                    .build();
        }
        List<MLAlgorithm> algos = MLCoreServiceValueHolder.getInstance().getAlgorithms();
        for (MLAlgorithm mlAlgorithm : algos) {
            if (algorithmName.equals(mlAlgorithm.getName())) {
                return Response.ok(mlAlgorithm.getParameters()).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("No algorithm found with the name: " + algorithmName)
                .build();
    }

    @GET
    @Path("/das/tables")
    @Produces("application/json")
    public Response getDASTables() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        AnalyticsDataService analyticsDataService = (AnalyticsDataService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(AnalyticsDataService.class, null);
        if (analyticsDataService == null) {
            String msg = String
                    .format("Error occurred while retrieving DAS tables of tenant [id] %s . Cause: AnalyticsDataService is null.",
                            tenantId);
            logger.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        List<String> tableNames;
        try {
            tableNames = analyticsDataService.listTables(tenantId);
        } catch (AnalyticsException e) {
            String msg = MLUtils.getErrorMsg(
                    String.format("Error occurred while retrieving DAS tables of tenant [id] %s .", tenantId), e);
            logger.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();

        }
        return Response.ok(tableNames).build();
    }

}
