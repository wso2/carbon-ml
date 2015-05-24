package org.wso2.carbon.ml.rest.api.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.rest.api.RestAPIConstants;

public class SessionBasedAuthenticationHandler implements RequestHandler {

    private static final Log logger = LogFactory.getLog(MLBasicAuthenticationHandler.class);
    
    @Override
    public Response handleRequest(Message message, ClassResourceInfo classResourceInfo) {
        // if the request is already authenticated, then skip this handler and continue
        if (AuthenticationContext.isAthenticated()) {
            return null;
        }
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) message.get(RestAPIConstants.HTTP_REQUEST_HEADER);
        HttpSession httpSession = httpServletRequest.getSession();

        try {
            if (httpSession != null) {
                String userName = (String) httpSession.getAttribute("userName");
                String tenantDomain = (String) httpSession.getAttribute("tenantDomain");
                if (userName != null && tenantDomain != null && httpSession.getAttribute("tenantId") != null) {
                    //if the userName and tenantDomain is present in the session, conclude this as an 
                    //authenticated session.
                    // set the correct tenant info for downstream code.
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    carbonContext.setTenantDomain(tenantDomain);
                    carbonContext.setTenantId((Integer)httpSession.getAttribute("tenantId"));
                    carbonContext.setUsername(userName);
                    return null;
                }
            }
            logger.error("Request is not Authenticated.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity("Request is not" +
                " Authenticated.").build();
        } catch (Exception e) {
            logger.error("Error occured while authenticating the request.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity("Error " +
                "occured while authenticating the request.").build();
        }
    }
}
