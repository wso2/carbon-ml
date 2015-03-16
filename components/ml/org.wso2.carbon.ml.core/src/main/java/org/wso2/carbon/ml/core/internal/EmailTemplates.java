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

package org.wso2.carbon.ml.core.internal;

import org.wso2.carbon.ml.commons.constants.MLConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * DTO class for JAXB binding of email templates.
 */
@XmlRootElement(name = MLConstants.EMAIL_TEMPLATES)
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTemplates {
    
    @XmlElement(name = MLConstants.TEMPLATE)
    private List<EmailTemplate> emailTemplates = null;

    /**
     * @return Returns a list of email templates.
     */
    public List<EmailTemplate> getEmailTemplates() {
        return emailTemplates;
    }

    /**
     * @param algorithms Sets email templates.
     */
    public void setEmailTemplates(List<EmailTemplate> emailTemplate) {
        this.emailTemplates = emailTemplate;
    }
    
    /**
     * Returns the template of a email of given type.
     * 
     * @param type  Type of the email
     * @return      Email template of the given type
     */
    public EmailTemplate getEmailTemplate(String type) {
        for (EmailTemplate emailTemplate : emailTemplates) {
            if (type.equalsIgnoreCase(emailTemplate.getType())) {
                return emailTemplate;
            }
        }
        return null;
    }
}
