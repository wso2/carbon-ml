package org.wso2.carbon.ml.dataset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DatasetServiceActivator implements BundleActivator {

		public void start(BundleContext context) throws Exception {
			final Log logger = LogFactory.getLog(DatasetServiceActivator.class);
			DatasetService datasetService = new DatasetService();
			context.registerService(DatasetService.class.getName(), datasetService, null);
			logger.info("Dataset Service activated.");
        }
		
		public void stop(BundleContext context) throws Exception {
	        // TODO Auto-generated method stub
        }
}

