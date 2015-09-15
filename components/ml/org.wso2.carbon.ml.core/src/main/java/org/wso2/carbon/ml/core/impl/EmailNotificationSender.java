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

package org.wso2.carbon.ml.core.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.email.internal.util.EmailEventAdapterConstants;
import org.wso2.carbon.ml.core.exceptions.MLEmailNotificationSenderException;
import org.wso2.carbon.ml.core.internal.EmailTemplate;
import org.wso2.carbon.ml.core.internal.EmailTemplates;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This class is used to send email notifications.
 */
public class EmailNotificationSender {

    private static final Log logger = LogFactory.getLog(EmailNotificationSender.class);
    
    /**
     * Send email notification indicating model building has been successfully completed.
     *
     * @param emailAddress              Email address to sent the mail.
     * @param emailTemplateParameters   An array containing the values for the parameters defined in the email template.
     */
    public static void sendModelBuildingCompleteNotification(String emailAddress, String[] emailTemplateParameters) {
        try {
            send(MLConstants.MODEL_BUILDING_COMPLETE_NOTIFICATION, emailAddress,emailTemplateParameters);
        } catch (Exception e) {
            logger.error("Failed to send the model building complete notification to: " + emailAddress + " : " + 
                    e.getMessage(), e);
        }
    }

    /**
     * Send email notification indicating model building has been failed.
     *
     * @param emailAddress              Email address to sent the mail.
     * @param emailTemplateParameters   An array containing the values for the parameters defined in the email template.
     */
    public static void sendModelBuildingFailedNotification(String emailAddress, String[] emailTemplateParameters) {
        try {
            send(MLConstants.MODEL_BUILDING_FAILED_NOTIFICATION, emailAddress, emailTemplateParameters);
        } catch (Exception e) {
            logger.error("Failed to send the model building failure notification to: " + emailAddress + " : " + 
                    e.getMessage(), e);
        }
    }
    
    
    /**
     * Send the email of given type, to the given email address.
     * 
     * @param emailTemplateType         Template to be used for the email. Email templates are defined in 
     *                                  repository/conf/email/ml-email-templates.xml file.
     * @param emailAddress              Email address to sent the mail.
     * @param emailTemplateParameters   An array containing the values for the parameters defined in the email template.
     * @throws                          MLEmailNotificationSenderException
     */
    private static void send(String emailTemplateType, String emailAddress, String [] emailTemplateParameters) 
            throws MLEmailNotificationSenderException {
        try {
            if (emailAddress!=null && !emailAddress.isEmpty()) {
                // Get the message template
                EmailTemplate emailTemplate = EmailNotificationSender.getEmailTemplate(emailTemplateType);
                // Set the message body.
                String message = EmailNotificationSender.setMessageBody(emailTemplate.getBody(), 
                        emailTemplate.getFooter(), emailTemplateParameters);
            
                OutputEventAdapterService emailAdapterService = MLCoreServiceValueHolder.getInstance().getOutputEventAdapterService();
                Map<String,String> dynamicProperties = new HashMap<String,String>();
                dynamicProperties.put(EmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_ADDRESS, emailAddress);
                dynamicProperties.put(EmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_SUBJECT, emailTemplate.getSubject());
                dynamicProperties.put(EmailEventAdapterConstants.APAPTER_MESSAGE_EMAIL_TYPE, MLConstants.TEXT_PLAIN);
                emailAdapterService.publish(MLConstants.ML_EMAIL_ADAPTER, dynamicProperties, message);
                logger.info("Model building status email sent to: " + emailAddress);
            }
        } catch (MLEmailNotificationSenderException e) {
            throw new MLEmailNotificationSenderException("An error occurred while retrieving email template: "
                    + e.getMessage(), e);
        }
    }
    
    /**
     * Get the template of the given email type from ml-email-config.xml file
     * 
     * @param emailTemplateType Type of the email template
     * @return                  Template of the given email type
     * @throws                  MLEmailNotificationSenderException
     */
    private static EmailTemplate getEmailTemplate(String emailTemplateType) throws MLEmailNotificationSenderException {
        String confXml = CarbonUtils.getCarbonConfigDirPath() + File.separator +
        		MLConstants.EMAIL_CONF_DIRECTORY + File.separator + MLConstants.ML_EMAIL_TEMPLATES_FILE;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(EmailTemplates.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            EmailTemplates emailConfigurations = (EmailTemplates)jaxbUnmarshaller.unmarshal(new File(confXml));
            return emailConfigurations.getEmailTemplate(emailTemplateType);
        } catch (JAXBException e) {
            throw new MLEmailNotificationSenderException("An error occurred while parsing email configurations from "
                    + confXml + " : " + e.getMessage(), e);
        }
    }
    
    /**
     * Format the body of the email.
     * 
     * @param body                      Content that should goes to the body of the email.
     * @param footer                    Content that should goes to the footer of the email.
     * @param emailTemplateParameters   An array containing the values for the parameters defined in the email template.
     * @return                          Formatted body of the email.
     */
    private static String setMessageBody(String body, String footer, String[] emailTemplateParameters) {
        StringBuilder contents = new StringBuilder();
        // Set all the parameters in the email body.
        for (int i = 0 ; i < emailTemplateParameters.length ; i++){
            body = body.replaceAll("\\{" + (i+1) + "\\}", emailTemplateParameters[i]);
        }
        contents.append(body).append(System.getProperty("line.separator")).append(System.getProperty("line.separator"))
                .append(footer);
        return contents.toString();
    }
}
