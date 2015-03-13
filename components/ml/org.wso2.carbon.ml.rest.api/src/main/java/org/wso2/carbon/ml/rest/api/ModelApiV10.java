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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.impl.MLModelHandler;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/models")
public class ModelApiV10 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(ModelApiV10.class);
    private MLModelHandler mlModelHandler;

    public ModelApiV10() {
        mlModelHandler = new MLModelHandler();
    }

    /**
     * Create a new Model.
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response createProject(MLModelNew model) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            model.setTenantId(tenantId);
            model.setUserName(userName);
            mlModelHandler.createModel(model);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error("Error occured while creating a model : " + model + " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/features/{modelId}")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addCustomizedFeatures(@PathParam("modelId") long modelId, List<MLCustomizedFeature> customizedFeatures) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlModelHandler.addCustomizedFeatures(modelId, customizedFeatures);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding customized features for the model [name] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{modelName}")
    @Produces("application/json")
    public Response getModel(@PathParam("modelName") String modelName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLModelNew model = mlModelHandler.getModel(tenantId, userName, modelName);
            if (model == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(model).build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while retrieving a model [name] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelName, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Produces("application/json")
    public Response getAllModels() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLModelNew> models = mlModelHandler.getAllModels(tenantId, userName);
            return Response.ok(models).build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while retrieving all models of tenant [id] %s and [user] %s . Cause: %s",
                    tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @DELETE
    @Path("/{modelName}")
    @Produces("application/json")
    public Response deleteModel(@PathParam("modelName") String modelName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlModelHandler.deleteModel(tenantId, userName, modelName);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while deleting a model [name] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelName, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
