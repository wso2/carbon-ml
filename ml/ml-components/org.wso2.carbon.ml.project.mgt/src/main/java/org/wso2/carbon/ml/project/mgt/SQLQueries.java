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
package org.wso2.carbon.ml.project.mgt;

public class SQLQueries {
	
	public static final String CREATE_PROJECT = "INSERT INTO ML_PROJECT(ID,NAME,DESCRIPTION,CREATED_TIME) VALUES(?,?,?,CURRENT_TIMESTAMP())";
    
    public static final String DELETE_PROJECT = "DELETE FROM ML_PROJECT WHERE ID=?";
    
    public static final String ADD_USER_TO_PROJECT = "INSERT INTO ML_USER_PROJECTS(USERNAME,PROJECT) VALUES(?,?)";
    
    public static final String GET_USER_PROJECTS = "SELECT ID,NAME,CREATED_TIME FROM ML_PROJECT WHERE ID IN (SELECT PROJECT FROM ML_USER_PROJECTS WHERE USERNAME=?)";

    public static final String GET_DATASET_ID = "SELECT ID FROM ML_DATASET WHERE PROJECT=?";
    
    //private Constructor to prevent class from instantiating.
	private SQLQueries() {
	  }
}
