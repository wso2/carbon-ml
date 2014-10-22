/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ModelService {
	
	private static final Log logger = LogFactory.getLog(ModelService.class);
    
	public JSONObject getHyperParameters(String algorithm) throws ModelServiceException{
		try {
			DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
			return handler.getHyperParameters(algorithm);
		} catch (DatabaseHandlerException ex) {
			String msg = "Error has occurred while retrieving hyper parameters";
			logger.error(msg, ex);
			throw new ModelServiceException(msg);
		}
	}
	
	public String[] getAlgorithmsByType(String algorithmType) throws ModelServiceException{
		try {
			DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
			return handler.getAlgorithms(algorithmType);
		} catch (DatabaseHandlerException ex) {
			String msg = "Error has occurred while retrieving algorithm names";
			logger.error(msg, ex);
			throw new ModelServiceException(msg);
		}
	}


    public Map<String,Double> getRecommendedAlgorithms(String algorithmType,
                                                       String userResponseJson)
            throws ModelServiceException
    {
     Map<String,Double> recommendations = new HashMap<String, Double>();
     try
     {
       DatabaseHandler handler = DatabaseHandler.getDatabaseHandler();
       String[] algorithms = handler.getAlgorithms(algorithmType);

     }
     catch(Exception e)
     {
         String msg = "An error occurred while retrieving recommended algorithms";
         logger.error(msg, e);
         throw new ModelServiceException(msg);
     }
      return recommendations;
    }


}
