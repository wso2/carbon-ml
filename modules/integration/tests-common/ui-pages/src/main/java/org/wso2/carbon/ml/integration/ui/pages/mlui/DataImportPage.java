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

package org.wso2.carbon.ml.integration.ui.pages.mlui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;

import java.io.File;

public class DataImportPage extends MLUIPage {
    private static final Log logger = LogFactory.getLog(DataImportPage.class);

    /**
     * Creates a data import page
     *
     * @param driver    Instance of the web driver
     * @throws          MLUIPageCreationException 
     */
    public DataImportPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }

    /**
     * Import data.
     * 
     * @param datasetUrl    Url of the dataset
     * @param project       Name of the project
     * @param desciption    Description of the project
     * @param workflow      Name of the workflow
     * @return Dataset      Summary Page
     * @throws              InvalidPageException 
     */
    public DatasetSummaryPage importData(File dataFile, String project, String desciption,
            String workflow) throws InvalidPageException {
        try {
            WebElement projectName = driver.findElement(By.id(mlUIElementMapper.getElement("project.name")));
            WebElement projectDescription = driver.findElement(By.id(mlUIElementMapper
                    .getElement("project.description")));
            WebElement workflowName = driver.findElement(By.id(mlUIElementMapper.getElement("workflow.name")));
            WebElement fileElement = driver.findElement(By.id(mlUIElementMapper.getElement("file.element")));
            projectName.clear();
            projectDescription.clear();
            workflowName.clear();
            projectName.sendKeys(project);
            projectDescription.sendKeys(desciption);
            workflowName.sendKeys(workflow);
            fileElement.sendKeys(dataFile.getPath());
            driver.findElement(By.xpath(mlUIElementMapper.getElement("import.button"))).click();
            return new DatasetSummaryPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Error occure while creating the data summary page: ", e);
        }
    }
}