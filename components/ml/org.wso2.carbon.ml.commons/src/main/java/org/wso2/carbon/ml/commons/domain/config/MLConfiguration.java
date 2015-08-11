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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.util.List;

/**
 * DTO class for JAXB binding of machine-learner.xml
 */
@XmlRootElement(name = "MachineLearner")
@XmlAccessorType(XmlAccessType.FIELD)
public class MLConfiguration {

    @XmlElement(name = "DataSourceName")
    private String datasourceName;

    @XmlElement(name = "HdfsURL")
    private String hdfsUrl;

    @XmlElement(name = "EmailNotificationEndpoint")
    private String emailNotificationEndpoint;

    @XmlElement(name = "ModelRegistryLocation")
    private String modelRegistryLocation;

    @XmlElementWrapper(name = "Properties")
    @XmlElement(name = "Property")
    private List<MLProperty> properties;

    @XmlElementWrapper(name = "Algorithms")
    @XmlElement(name = "Algorithm")
    private List<MLAlgorithm> mlAlgorithms;

    @XmlElement(name = "SummaryStatisticsSettings")
    private SummaryStatisticsSettings summaryStatisticsSettings;

    @XmlElement(name = "ModelStorage")
    private Storage modelStorage;
    
    @XmlElement(name = "DatasetStorage")
    private Storage datasetStorage;

    public Storage getModelStorage() {
        if (modelStorage == null) {
            modelStorage = new Storage();
            modelStorage.setStorageType("file");
            File f = new File(System.getProperty("carbon.home") + File.separator + "models");
            try {
                if (f.mkdir()) {
                    modelStorage.setStorageDirectory(f.getAbsolutePath());
                } else {
                    modelStorage.setStorageDirectory(System.getProperty("carbon.home") + File.separator + "tmp");
                }
            } catch (Exception e) {
                // can't create the directory, use an existing one.
                modelStorage.setStorageDirectory(System.getProperty("carbon.home") + File.separator + "tmp");
            }
        }
        return modelStorage;
    }

    public void setModelStorage(Storage modelStorage) {
        this.modelStorage = modelStorage;
    }
    
    public Storage getDatasetStorage() {
        if (datasetStorage == null) {
            datasetStorage = new Storage();
            datasetStorage.setStorageType("file");
            File f = new File(System.getProperty("carbon.home") + File.separator + "datasets");
            try {
                if (f.mkdir()) {
                    datasetStorage.setStorageDirectory(f.getAbsolutePath());
                } else {
                    datasetStorage.setStorageDirectory(System.getProperty("carbon.home") + File.separator + "tmp");
                }
            } catch (Exception e) {
                // can't create the directory, use an existing one.
                datasetStorage.setStorageDirectory(System.getProperty("carbon.home") + File.separator + "tmp");
            }
        }
        return datasetStorage;
    }
    
    public void setDatasetStorage(Storage datasetStorage) {
        this.datasetStorage = datasetStorage;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public List<MLAlgorithm> getMlAlgorithms() {
        return mlAlgorithms;
    }

    public void setMlAlgorithms(List<MLAlgorithm> mlAlgorithms) {
        this.mlAlgorithms = mlAlgorithms;
    }

    public SummaryStatisticsSettings getSummaryStatisticsSettings() {
        return summaryStatisticsSettings;
    }

    public void setSummaryStatisticsSettings(SummaryStatisticsSettings summaryStatisticsSettings) {
        this.summaryStatisticsSettings = summaryStatisticsSettings;
    }

    public List<MLProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MLProperty> properties) {
        this.properties = properties;
    }

    public String getHdfsUrl() {
        return hdfsUrl;
    }

    public void setHdfsUrl(String hdfsUrl) {
        this.hdfsUrl = hdfsUrl;
    }

    public String getEmailNotificationEndpoint() {
        return emailNotificationEndpoint;
    }

    public String getModelRegistryLocation() {
        return modelRegistryLocation;
    }

    public void setModelRegistryLocation(String modelRegistryLocation) {
        this.modelRegistryLocation = modelRegistryLocation;
    }

    public void setEmailNotificationEndpoint(String emailNotificationEndpoint) {
        this.emailNotificationEndpoint = emailNotificationEndpoint;
    }
}
