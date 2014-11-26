package org.wso2.carbon.ml.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SamplePoints implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<List<String>> samplePoints = new ArrayList<List<String>>();
	private Map<String, Integer> headerMap;

	/**
	 * Returns the raw-data of the sample.
	 *
	 * @return A list of data columns of the sample
	 */
	protected List<List<String>> getSamplePoints() {
		return samplePoints;
	}

	/**
	 * Returns the header names.
	 *
	 * @return header names and their positions in the data-set as a Map
	 */
	protected Map<String, Integer> getHeader() {
		return headerMap;
	}

	/**
	 * Set the sample points.
	 *
	 * @param samplePoints A list of data columns of the sample
	 */
	protected void setSamplePoints(List<List<String>> samplePoints) {
		this.samplePoints = samplePoints;
	}

	/**
	 * Set the header of the sample points.
	 *
	 * @param headerMap header names and their positions in the data-set as a Map
	 */
	protected void setHeader(Map<String, Integer> headerMap) {
		this.headerMap = headerMap;
	}
}
