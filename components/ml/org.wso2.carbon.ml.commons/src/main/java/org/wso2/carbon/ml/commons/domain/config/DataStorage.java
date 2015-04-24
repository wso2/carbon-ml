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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.commons.domain.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

/**
 * DTO class for JAXB binding of ModelStorage
 */
public class DataStorage implements Serializable {

    private static final long serialVersionUID = 1320087479354297195L;
    private String fileLocation;
    private String hdfsLocation;
    
    @XmlElement(name = "FileLocation")
    public String getFileLocation() {
        return fileLocation;
    }
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
    @XmlElement(name = "StorageDirectory")
    public String getHdfsLocation() {
        return hdfsLocation;
    }
    public void setHdfsLocation(String hdfsLocation) {
        this.hdfsLocation = hdfsLocation;
    }
    
    

}
