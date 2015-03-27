package org.wso2.carbon.ml.commons.domain;

public class ScatterPlotPoints {
    private int tenantId;
    private String user;
    private long versionsetId;
    private String xAxisFeature;
    private String yAxisFeature;
    private String groupByFeature;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getVersionsetId() {
        return versionsetId;
    }

    public void setVersionsetId(long valueSetId) {
        this.versionsetId = valueSetId;
    }

    public String getxAxisFeature() {
        return xAxisFeature;
    }

    public void setxAxisFeature(String xAxisFeature) {
        this.xAxisFeature = xAxisFeature;
    }

    public String getyAxisFeature() {
        return yAxisFeature;
    }

    public void setyAxisFeature(String yAxisFeature) {
        this.yAxisFeature = yAxisFeature;
    }

    public String getGroupByFeature() {
        return groupByFeature;
    }

    public void setGroupByFeature(String groupByFeature) {
        this.groupByFeature = groupByFeature;
    }
}