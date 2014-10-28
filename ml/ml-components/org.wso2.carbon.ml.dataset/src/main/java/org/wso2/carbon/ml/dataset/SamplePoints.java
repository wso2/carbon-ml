package org.wso2.carbon.ml.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SamplePoints  implements Serializable{

    private static final long serialVersionUID = 1L;
	private List<List<String>> samplePoints = new ArrayList<List<String>>();
	private Map<String, Integer> headerMap;
	
	/*
	 * returns the raw-data of the sample
	 */
	protected List<List<String>> getSamplePoints() {
		return samplePoints;
	}

	/*
	 * returns the header names and their positions in the data-set
	 */
	protected Map<String, Integer> getHeader() {
		return headerMap;
	}
	
	
	protected void setSamplePoints(List<List<String>> samplePoints) {
		this.samplePoints=samplePoints;
	}
	
	protected void setHeader(Map<String, Integer> headerMap) {
		this.headerMap=headerMap;
	}
}
