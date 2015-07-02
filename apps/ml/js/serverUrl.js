/*
Displaying ML Jaggery App URL
*/
var carbon = require('carbon');
var configurationContextService = carbon.server.osgiService('org.wso2.carbon.utils.ConfigurationContextService');
var configCtx = configurationContextService.getServerConfigContext();
var log = new Log(); 
log.info("WSO2 Machine Learner Console URL : "+configCtx.getProperty("ml.url"));
