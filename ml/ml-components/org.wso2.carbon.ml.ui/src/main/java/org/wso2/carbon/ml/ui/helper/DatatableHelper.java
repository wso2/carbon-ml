package org.wso2.carbon.ml.ui.helper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ml.dataset.xsd.Feature;


public class DatatableHelper {

	public void populateDatatable(HttpServletResponse response,
			HttpServletRequest request, Feature[] features) throws IOException {

		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("sEcho",
				Integer.parseInt(request.getParameter("sEcho")));
		jsonResponse.put("iTotalRecords", features.length);
		jsonResponse.put("iTotalDisplayRecords", features.length);

		for (Feature feature : features) {
			JSONArray jsonArray = new JSONArray();

			// adding features
			jsonArray.put("<span class=\"feature\">" + feature.getFieldName()
					+ "</span>");

			// adding include/exclude check box
			jsonArray.put(buildInputCheckBox(feature.isInputSpecified()));

			// adding data type drop-down
			jsonArray.put(buildSectionBox(new String[] { "Categorical",
					"Numerical" }, feature.getType().getFeatureType(),
					"fieldType"));

			// adding summary statistics
			jsonArray.put("Will be added later");

			// adding impute method
			jsonArray.put(buildSectionBox(new String[] { "Drop",
					"Impute with Max" }, feature.getImputeOperation().getMethod(), "imputeMethod"));

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
			if (selectedOption.equals(ft)) {
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
