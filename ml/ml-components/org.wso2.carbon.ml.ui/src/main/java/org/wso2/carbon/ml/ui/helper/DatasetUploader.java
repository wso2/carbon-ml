/*

 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.ui.helper;

import java.io.File;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a helper class use to upload data from the front-end to back-end data
 * store
 */
public class DatasetUploader {

	private static final Log LOGGER = LogFactory.getLog(DatasetUploader.class);
	
	private HttpServletRequest request;
	private String datasetUploadingDir;
	private int datasetInMemThreshold;
	private long datasetUploadLimit;
	
	private String datasetName;

	public DatasetUploader(HttpServletRequest request,
			String datasetUploadingDir, int datasetInMemThreshold,
			long datasetUploadLimit) {
		this.request = request;
		this.datasetUploadingDir = datasetUploadingDir;
		this.datasetInMemThreshold = datasetInMemThreshold;
		this.datasetUploadLimit = datasetUploadLimit;

	}

	public boolean doUplod() throws DatasetUploadException {

		if (ServletFileUpload.isMultipartContent(request)) {
			try {

				// This contains file item instances which keep their content
				// either in memory or disk
				DiskFileItemFactory diskFileFactory = new DiskFileItemFactory();

				// above this threshold files will be stored in disk
				diskFileFactory.setSizeThreshold(datasetInMemThreshold);

				// high-level class for handling multi-part content
				ServletFileUpload uploader = new ServletFileUpload(diskFileFactory);
				uploader.setFileSizeMax(datasetUploadLimit);

				@SuppressWarnings("unchecked")
				List<FileItem> multiparts = uploader.parseRequest(request);

				for (FileItem item : multiparts) {
					if (!item.isFormField()) {

						datasetName = new File(item.getName()).getName();
						item.write(new File(datasetUploadingDir
								+ File.separator + datasetName));
					}
				}
				// successfully uploaded
				return true;

			} catch (Exception ex) {
				String errorMessage = "Dataset uploading operation failed, error message: "
						+ ex.getMessage();
				LOGGER.error(errorMessage, ex);
				throw new DatasetUploadException(errorMessage);
			}

		} else {
			LOGGER.info("Request is not a multi-part content");
		}

		// dataset uploading operation is unsuccessful
		return false;
	}
	
	public String getDatasetName(){
		return datasetName;
	}
}
