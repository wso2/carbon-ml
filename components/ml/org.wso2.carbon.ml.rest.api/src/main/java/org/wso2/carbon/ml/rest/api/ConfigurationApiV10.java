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

import org.apache.http.HttpHeaders;
import org.wso2.carbon.ml.commons.domain.config.MLAlgorithm;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/configs")
public class ConfigurationApiV10 extends MLRestAPI {

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
        return Response.status(Response.Status.NOT_FOUND)
                .entity("No algorithm found with the name: " + algorithmName).build();
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
        return Response.status(Response.Status.NOT_FOUND)
                .entity("No algorithm found with the name: " + algorithmName).build();
    }

}
