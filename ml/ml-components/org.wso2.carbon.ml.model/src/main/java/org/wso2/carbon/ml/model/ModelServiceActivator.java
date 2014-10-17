package org.wso2.carbon.ml.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ModelServiceActivator implements BundleActivator {
		private static final Log logger = LogFactory.getLog(ModelServiceActivator.class);

    /**
     * Creates an instance of ModelService OSGi service
     * @param context OSGi bundle's execution context
     */
		public void start(BundleContext context) {
			ModelService modelService = new ModelService();
			context.registerService(ModelService.class.getName(), modelService, null);
			logger.info("Model Service activated.");
        }
		
		public void stop(BundleContext context) {
	        //
        }
}

