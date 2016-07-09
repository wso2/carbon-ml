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
import org.wso2.carbon.ml.rest.api.MLRestAPI;
import org.wso2.carbon.ml.wrangler.MLWranglerService;
import org.wso2.carbon.ml.wrangler.WranglerService;

/**
 * WSO2 ML Wrangler API. All the operations related to Wrangler are delegated from this class.
 */
@Path("/wrangler")
public class WranglerApiV10 extends MLRestAPI {

	private static final Log logger = LogFactory.getLog(WranglerApiV10.class);

	private WranglerService wranglerService;

	public WranglerApiV10(){
		wranglerService = new MLWranglerService();
	}
	/**
	 * HTTP Options method implementation for wrangler API.
	 *
	 * @return
	 */
	@OPTIONS
	public Response options() {
		return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
	}
	/**
	 * add the javascript with wrangler operations
	 */
	@POST
	@Path("/{analysisId}/add")
	@Produces("application/json")
	@Consumes("text/plain")
	public Response addScript(@PathParam("analysisId") long analysisId,String script) {
		try{
			wranglerService.addScript(script);
			return Response.ok().build();
		}catch (Exception e){
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("/test/1")
	@Produces("application/json")
	public Response test(@PathParam("analysisId") long analysisId) {
		return Response.ok().build();
	}
}
