package org.wso2.carbon.ml.db;

public class DatabaseService {

	// TODO: read these from DB
	public String getDatasetUploadingDir() {
		return "/home/upul/temp/";
	}

	public int getDatasetInMemoryThreshold() {
		return 1024 * 1024;
	}

	public long getDatasetUploadingLimit() {
		return 1024 * 1024 * 256;
	}

}
