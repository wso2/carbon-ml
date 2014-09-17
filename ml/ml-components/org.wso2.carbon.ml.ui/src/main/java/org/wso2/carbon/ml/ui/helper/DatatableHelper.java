package org.wso2.carbon.ml.ui.helper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ml.db.xsd.Feature;

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
			jsonArray.put(buildDataTypeSectionBox(new String[] { "Categorical", "Numerical" },
					feature.getType().getFeatureType()));

			// adding summary statistics
			jsonArray.put("Will be added later");

			// adding impute method
			jsonArray.put(buildImputeSectionBox(new String[] { "Drop", "Impute with Max" },
					feature.getImputeOperation().getImputeOperation()));

			// create a JSON array with above HTML elements
			jsonResponse.append("aaData", jsonArray);
		}
		response.resetBuffer();
		response.reset();		
		response.setContentType("application/Json");		
		response.getWriter().print(jsonResponse.toString().trim());
	}

	//TODO: 
	private String buildInputCheckBox(boolean value) {
		String control = "<input type=\"checkbox\" "
				+ "class=\"includeFeature\" value=\"includeFeature\"";
		if (value) {
			control += " checked />";
		} else {
			control += "/>";
		}

		return control;

	}

	// TODO: replace these two with a parameterized method
	private String buildDataTypeSectionBox(String[] types, String selected) {
		StringBuilder selection = new StringBuilder();
		selection.append("<select class=\"fieldType\">");
		for (String ft : types) {
			if (ft.toString().equals(selected)) {
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

	//TODO:
	private String buildImputeSectionBox(String[] types, String selected) {
		StringBuilder selection = new StringBuilder();
		selection.append("<select class=\"imputeMethod\">");
		for (String ft : types) {
			if (ft.toString().equals(selected)) {
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
