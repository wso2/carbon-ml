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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ml.dataset.xsd.Feature;


public class DatatableHelper {

	public void populateDatatable(HttpServletResponse response,
			HttpServletRequest request, Feature[] features, int datasetSize)
			throws IOException {

		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("sEcho",
				Integer.parseInt(request.getParameter("sEcho")));
		// TODO: remove hard coded values
		jsonResponse.put("iTotalRecords", datasetSize);
		jsonResponse.put("iTotalDisplayRecords", datasetSize);

		for (Feature feature : features) {
			JSONArray jsonArray = new JSONArray();

			// adding features
			jsonArray.put("<span class=\"feature\">" + feature.getFieldName()
					+ "</span>");			

			// adding include/exclude check box
			jsonArray.put(buildInputCheckBox(feature.isInputSpecified()));

			// adding data type drop-down
			jsonArray.put(buildSectionBox(new String[] { "CATEGORICAL",
					"NUMERICAL" }, feature.getType().getFeatureName(),
					"fieldType"));
			
			// adding summary statistics
			jsonArray.put(feature.getSummaryStats());

			// adding impute method
			jsonArray
					.put(buildSectionBox(new String[] { "DISCARD",
							"REPLACE_WTH_MEAN", "REGRESSION_IMPUTATION" },
							feature.getImputeOperation().getImputeOptionName(),
							"imputeMethod"));

			// create a JSON array with above HTML elements
			jsonResponse.append("aaData", jsonArray);
		}
		response.resetBuffer();
		response.reset();
		response.setContentType("application/Json");
		response.getWriter().print(jsonResponse.toString().trim());
	}

	/**
	 * This private helper method is used to build the "input" check boxes of
	 * the data-table
	 * 
	 * @param value
	 *            : indicates whether the check box is selected or not
	 * @return HTML code for rendering a check box
	 */
	private String buildInputCheckBox(boolean value) {
		String checkboxControl = "<input type=\"checkbox\" "
				+ "class=\"includeFeature\" value=\"includeFeature\"";
		if (value) {
			checkboxControl += " checked />";
		} else {
			checkboxControl += "/>";
		}

		return checkboxControl;

	}

	/**
	 * This private method is used by the
	 * {@link #populateDatatable(HttpServletResponse, HttpServletRequest, Feature[])}
	 * method build selected boxes.
	 * 
	 * @param types
	 *            :
	 * @param selectedOption
	 *            : already selected option
	 * @param cssClass
	 *            : css class assigned to this selection buttons.
	 * @return: HTML code for rendering this selection boxes
	 */
	private String buildSectionBox(String[] types, String selectedOption,
			String cssClass) {
		StringBuilder selection = new StringBuilder();
		selection.append("<select class=\"" + cssClass + "\">");
		for (String ft : types) {
			if (selectedOption.equalsIgnoreCase(ft)) {
				selection.append("<option selected value=\"" + ft.toString()
						+ "\">" + ft.toString() + "</option>");
			} else {
				selection.append("<option value=\"" + ft.toString() + "\">"
						+ ft.toString() + "</option>");
			}

		}
		selection.append("</select>");
		return selection.toString();
	}
}
