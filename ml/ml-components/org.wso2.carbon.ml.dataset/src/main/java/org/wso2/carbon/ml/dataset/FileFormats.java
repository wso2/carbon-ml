package org.wso2.carbon.ml.dataset;

public class FileFormats {
	
	private final String fileFormat;
	
	private FileFormats(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public static final FileFormats CSV = new FileFormats("csv");
	public static final FileFormats TEXT = new FileFormats("txt");
	
	public String toString() {
		return fileFormat;
	}
}
