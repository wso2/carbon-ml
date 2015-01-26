/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.project.mgt.dto;

/**
 * This DTO represents a machine learning workflow
 */
public class Workflow {
    private String workflowId;
    private String workflowName;

    public Workflow(String workflowId, String workflowName) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
    }

    /**
     *
     * @return ID of this workflow
     */
    public String getWorkflowId() {
        return workflowId;
    }

    /**
     *
     * @return Name of this workflow
     */
    public String getWorkflowName() {
        return workflowName;
    }
}
