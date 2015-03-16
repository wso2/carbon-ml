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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLHyperParameter;
import org.wso2.carbon.ml.commons.domain.MLModelConfiguration;
import org.wso2.carbon.ml.commons.domain.MLModelNew;
import org.wso2.carbon.ml.commons.domain.MLStorage;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
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
    public Response createModel(MLModelNew model) {
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
    @Path("/{modelId}/features")
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
                    "Error occured while adding customized features for the model [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{modelId}/features/defaults")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addDefaultsIntoCustomizedFeatures(@PathParam("modelId") long modelId, MLCustomizedFeature customizedValues) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            customizedValues.setTenantId(tenantId);
            customizedValues.setUserName(userName);
            customizedValues.setLastModifiedUser(userName);
            
            mlModelHandler.addDefaultsIntoCustomizedFeatures(modelId, customizedValues);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding customized features for the model [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{modelId}/configurations")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addModelConfiguration(@PathParam("modelId") long modelId, List<MLModelConfiguration> modelConfigs) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlModelHandler.addModelConfigurations(modelId, modelConfigs);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding model configurations for the model [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{modelId}/hyperParams")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addHyperParameters(@PathParam("modelId") long modelId, List<MLHyperParameter> hyperParameters) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlModelHandler.addHyperParameters(modelId, hyperParameters);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding hyper parameters for the model [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{modelId}/hyperParams/defaults")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addDefaultsIntoHyperParameters(@PathParam("modelId") long modelId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlModelHandler.addDefaultsIntoHyperParameters(modelId);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding hyper parameters for the model [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{modelId}/storages")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addStorage(@PathParam("modelId") long modelId, MLStorage storage) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlModelHandler.addStorage(modelId, storage);
            return Response.ok().build();
        } catch (MLModelHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding storage for the model [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{modelId}")
    @Produces("application/json")
    @Consumes("application/json")
    public Response buildModel(@PathParam("modelId") long modelId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlModelHandler.buildModel(tenantId, userName, modelId);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error(String.format(
                    "Error occured while building the model [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    modelId, tenantId, userName, e.getMessage()), e);
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
