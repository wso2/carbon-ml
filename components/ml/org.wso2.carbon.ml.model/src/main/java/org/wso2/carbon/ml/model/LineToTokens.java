package org.wso2.carbon.ml.model;

import org.apache.spark.api.java.function.Function;

import java.util.regex.Pattern;

/**
 * This class transforms each line (line-by-line) into an array of String tokens
 */
public class LineToTokens implements Function<String, String[]> {

    private Pattern tokenSeparator;

    LineToTokens(Pattern pattern) {
        this.tokenSeparator = pattern;
    }

    @Override
    public String[] call(String line) throws Exception {

        return tokenSeparator.split(line);
    }
}
