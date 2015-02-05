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

package org.wso2.carbon.ml.project.mgt.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.wso2.carbon.ml.project.mgt.constant.ProjectMgtConstants;
import java.util.List;

/**
 * DTO class for JAXB binding of email template configuration.
 */
@XmlRootElement(name = ProjectMgtConstants.CONFIGURATIONS)
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailConfigurations {
    
    @XmlElement(name = ProjectMgtConstants.CONFIGURATION)
    private List<EmailTemplate> emailConfigurations = null;

    /**
     * @return Returns a list of email templates.
     */
    public List<EmailTemplate> getEmailConfigurations() {
        return emailConfigurations;
    }

    /**
     * @param algorithms Sets email templates.
     */
    public void setEmailTemplates(List<EmailTemplate> emailTemplate) {
        this.emailConfigurations = emailTemplate;
    }
    
    /**
     * Returns the template of a email of given type.
     * 
     * @param type  Type of the email
     * @return      Email template of the given type
     */
    public EmailTemplate getEmailTemplate(String type) {
        for (EmailTemplate emailTemplate : emailConfigurations) {
            if (type.equalsIgnoreCase(emailTemplate.getType())) {
                return emailTemplate;
            }
        }
        return null;
    }
}
