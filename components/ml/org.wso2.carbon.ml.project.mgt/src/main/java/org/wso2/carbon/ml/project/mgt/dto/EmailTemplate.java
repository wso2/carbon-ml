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
import javax.xml.bind.annotation.XmlAttribute;

/**
 * DTO class for JAXB binding of email template configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTemplate {
    private String targetEpr;
    private String subject;
    private String body;
    private String redirectPath;
    private String footer;

    @XmlAttribute
    private String type;
    
    /**
     * @return Returns targetEpr of the email.
     */
    public String getTargetEpr() {
        return targetEpr;
    }

    /**
     * @param targetEpr Sets targetEpr of the email.
     */
    public void setTargetEpr(String targetEpr) {
        this.targetEpr = targetEpr;
    }

    /**
     * @return Returns subject of the email.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject Sets subject of the email.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return Returns body of the email.
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body Sets body of the email.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return Returns redirectPath of the email.
     */
    public String getRedirectPath() {
        return redirectPath;
    }

    /**
     * @param redirectPath Sets redirectPath of the email.
     */
    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    /**
     * @return Returns footer of the email.
     */
    public String getFooter() {
        return footer;
    }

    /**
     * @param footer Sets footer of the email.
     */
    public void setFooter(String footer) {
        this.footer = footer;
    }

    /**
     * @return type of the of the email notification.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
