package org.wso2.carbon.ml.model.dto;

import org.wso2.carbon.ml.model.constants.MLModelConstants;

public class MLFeature {
    private String name;
    private String type;
    private String imputeOption;
    private boolean include;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImputeOption() {
        return imputeOption;
    }

    public void setImputeOption(String imputeOption) {
        this.imputeOption = imputeOption;
    }
}
