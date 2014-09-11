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
 * 
 *
 */
public class DatasetUploader {

	// TODO: read these parameters from configuration
	private static final String DATASET_UPLOADING_DIR = "/home/upul/temp";
	private static final int DATASET_IN_MEM_THRESHOLD = 1024 * 1024 * 5;
	private static final int DATASET_UPLOADING_LIMIT = 1024 * 1024 * 254;

	private static Log log = LogFactory.getLog(DatasetUploader.class);

	public boolean doUplod(HttpServletRequest request)
			throws DatasetUploadException {

		if (ServletFileUpload.isMultipartContent(request)) {
			try {

				// This contains file item instances which keep their content
				// either in memory or disk
				DiskFileItemFactory diskFileFactory = new DiskFileItemFactory();

				// above this threshold files will be stored in disk
				diskFileFactory.setSizeThreshold(DATASET_IN_MEM_THRESHOLD);

				// high-level class for handling multi-part content
				ServletFileUpload uploader = new ServletFileUpload();
				uploader.setFileSizeMax(DATASET_UPLOADING_LIMIT);

				@SuppressWarnings("unchecked")
				List<FileItem> multiparts = uploader.parseRequest(request);

				for (FileItem item : multiparts) {
					if (!item.isFormField()) {
						
						String fileName = new File(item.getName()).getName();
						item.write(new File(DATASET_UPLOADING_DIR
								+ File.separator + fileName));
					}
				}
				// successfully uploaded
				return true;

			} catch (Exception ex) {
				String errorMessage = "Dataset uploading operation failed";
				log.error(errorMessage, ex);
				throw new DatasetUploadException(errorMessage);
			}

		} else {
			log.info("Request is not a multi-part content");
		}

		// dataset uploading operation is unsuccessful
		return false;
	}
}
