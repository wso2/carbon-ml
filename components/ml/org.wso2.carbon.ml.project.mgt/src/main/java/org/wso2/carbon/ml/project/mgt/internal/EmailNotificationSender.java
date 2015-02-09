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

package org.wso2.carbon.ml.project.mgt.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.project.mgt.constant.ProjectMgtConstants;
import org.wso2.carbon.ml.project.mgt.dto.EmailConfigurations;
import org.wso2.carbon.ml.project.mgt.dto.EmailTemplate;
import org.wso2.carbon.ml.project.mgt.exceptions.ProjectManagementServiceException;
import org.wso2.carbon.ml.project.mgt.internal.ds.MLProjectManagementServiceValueHolder;
import org.wso2.carbon.utils.CarbonUtils;

public class EmailNotificationSender {

    private static final Log logger = LogFactory.getLog(EmailNotificationSender.class);
    private static final Pattern firstName = Pattern.compile(ProjectMgtConstants.USER_FIRST_NAME);
    
    /**
     * Send the email of given type, to the given email address.
     * 
     * @param type              Type of the email to be sent.
     * @param emailAddress      Address to which the email is sent.
     * @param redirectUrl       A redirect URL that should be sent in the email.
     * @throws                  EmailAdaptorException
     */
    void send(String type, String userName, String emailAddress, String redirectUrl) throws ProjectManagementServiceException {
        try {
            // Get the message template
            EmailTemplate emailTemplate = EmailNotificationSender.getEmailTemplate(type);

            // Set the subject of the email.
            Map<String, String> headerMap = new HashMap<String, String>();
            headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, emailTemplate.getSubject());
            
            // Set the message body.
            if (redirectUrl==null) {
                redirectUrl = emailTemplate.getRedirectPath();
            }
            OMElement payload = EmailNotificationSender.setMessageBody(userName, emailTemplate.getBody(), 
                emailTemplate.getFooter(), redirectUrl);
        
            // Create a service client using configurations in axis2.xml
            ServiceClient serviceClient;
            ConfigurationContext configContext = MLProjectManagementServiceValueHolder.getConfigurationContextService()
                    .getServerConfigContext();
            if (configContext != null) {
                serviceClient = new ServiceClient(configContext, null);
            } else {
                serviceClient = new ServiceClient();
            }
            // Set additional properties of the service client
            EmailNotificationSender.setProperties(serviceClient, headerMap, emailAddress);
            // Send the mail with the payload
            serviceClient.fireAndForget(payload);
            logger.info("Sending notification mail to " + emailAddress);
        } catch (AxisFault e) {
            throw new ProjectManagementServiceException("An error occured while sending the email: " + e.getMessage(),
                e);
        } catch (ProjectManagementServiceException e) {
            throw new ProjectManagementServiceException("An error occured while retrieving email template: "
                    + e.getMessage(), e);
        }
    }
    
    /**
     * Get the template of the given email type from ml-email-config.xml file
     * 
     * @param type  Type of the email
     * @return      Template of the given email type
     * @throws      EmailAdaptorException
     */
    private static EmailTemplate getEmailTemplate(String type) throws ProjectManagementServiceException {
        String confXml = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                ProjectMgtConstants.EMAIL_CONF_DIRECTORY + File.separator + ProjectMgtConstants.ML_EMAIL_CONF_FILE;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(EmailConfigurations.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            EmailConfigurations emailConfigurations = (EmailConfigurations)jaxbUnmarshaller.unmarshal(
                    new File(confXml));
            return emailConfigurations.getEmailTemplate(type);
        } catch (JAXBException e) {
            throw new ProjectManagementServiceException("An error occured while parsing email configurations from "
                    + confXml + " : " + e.getMessage(), e);
        }
    }
    
    /**
     * Set properties of the service client.
     * 
     * @param serviceClient     ServiceClient to set the properties.
     * @param headerMap         Header to be sending email.
     * @param receiverEmail     Sending email address.
     * @return                  ServiceClient with properties set.
     */
    private static ServiceClient setProperties(ServiceClient serviceClient, Map<String, String> headerMap,
            String receiverEmail ) {
        Options options = new Options();
        options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
        options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
        options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT, MailConstants.TRANSPORT_FORMAT_TEXT);
        options.setTo(new EndpointReference("mailto:" + receiverEmail));
        serviceClient.setOptions(options);
        return serviceClient;
    }
    
    /**
     * Format the body of the email.
     * 
     * @param body          Content that should goes to the body of the email.
     * @param footer        Content that should goes to the footer of the email.
     * @param redirectUrl   Redirecting URl to be included in the email.
     * @return              Formatted body of the email.
     */
    private static OMElement setMessageBody(String userName, String body, String footer, String redirectUrl) {
        OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER, null);
        StringBuilder contents = new StringBuilder();
        // set the user name
        body = firstName.matcher(body).replaceAll(userName);
        contents.append(body).append(System.getProperty("line.separator")).append(redirectUrl)
                .append(System.getProperty("line.separator")).append(footer);
        payload.setText(contents.toString());
        return payload;
    }
}
