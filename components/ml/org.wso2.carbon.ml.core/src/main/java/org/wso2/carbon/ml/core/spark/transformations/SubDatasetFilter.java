/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.ml.core.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.regression.LabeledPoint;

import scala.Tuple2;

public class SubDatasetFilter implements Function<Tuple2<LabeledPoint, Long>, Boolean> {

    private static final long serialVersionUID = 8685683197157772862L;
    private long startIndex;
    private long endIndex;

    private SubDatasetFilter(Builder builder) {
        this.startIndex = builder.start;
        this.endIndex = builder.end;
    }

    @Override
    public Boolean call(Tuple2<LabeledPoint, Long> labeledTuple) {
        if (labeledTuple._2 >= startIndex && labeledTuple._2 <= endIndex) {
            return true;
        }
        return false;
    }

    public static class Builder {
        private long start;
        private long end;

        public Builder startIndex(long startIndex) {
            this.start = startIndex;
            return this;
        }

        public Builder endIndex(long endIndex) {
            this.end = endIndex;
            return this;
        }

        public SubDatasetFilter build() {
            return new SubDatasetFilter(this);
        }
    }
}
