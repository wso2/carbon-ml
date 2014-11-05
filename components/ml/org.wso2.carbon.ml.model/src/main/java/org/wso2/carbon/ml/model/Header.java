package org.wso2.carbon.ml.model;

import org.apache.spark.api.java.function.Function;

public class Header implements Function<String, Boolean> {

    private String header;

    Header(String header) {
        this.header = header;
    }

    @Override
    public Boolean call(String line) throws Exception {
        Boolean isRow = true;
        if (line.equals(this.header)) {
            isRow = false;
        }
        return isRow;
    }
}
