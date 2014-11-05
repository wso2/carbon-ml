package org.wso2.carbon.ml.model;

import org.json.JSONArray;
import org.json.JSONObject;

public class LogisticRegressionModelSummary {
    private JSONObject userResponse;
    private JSONArray roc;
    private double auc;

    public JSONObject getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(JSONObject userResponse) {
        this.userResponse = userResponse;
    }

    public JSONArray getRoc() {
        return roc;
    }

    public void setRoc(JSONArray roc) {
        this.roc = roc;
    }

    public double getAuc() {
        return auc;
    }

    public void setAuc(double auc) {
        this.auc = auc;
    }
}
