package org.wso2.carbon.ml.model;

import org.json.JSONArray;

public class LogisticRegressionModelSummary {
    private JSONArray roc;
    private double auc;

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
