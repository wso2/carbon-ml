package org.wso2.carbon.ml.dataset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DatasetServiceActivator implements BundleActivator {
		private static final Log logger = LogFactory.getLog(DatasetServiceActivator.class);
		
		/**
		 * Creates an instance of DatasetService OSGI service
		 */
		public void start(BundleContext context) {
			DatasetService datasetService = new DatasetService();
			context.registerService(DatasetService.class.getName(), datasetService, null);
			logger.info("Dataset Service activated.");
        }
		
		public void stop(BundleContext context) {
	        // TODO Auto-generated method stub
        }
}

