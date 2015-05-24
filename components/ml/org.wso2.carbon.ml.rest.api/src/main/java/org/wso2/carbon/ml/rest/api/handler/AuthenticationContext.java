package org.wso2.carbon.ml.rest.api.handler;

import org.wso2.carbon.context.PrivilegedCarbonContext;

public class AuthenticationContext {

    public static boolean isAthenticated() {
        // If the user-name, tenant Domain and tenant ID is not set in carbon context, conclude as not authenticated.
        // This is done to authenticate each request.
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        return (carbonContext.getUsername()!= null && carbonContext.getTenantDomain() != null && carbonContext.getTenantId() != 0);
    }
}
