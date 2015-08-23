/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.database.internal;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;


public class MLDataSource {
    private DataSource dataSource;

    public MLDataSource(String mlDatasourceUrl) throws SQLException {
        try {
            Context initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup(mlDatasourceUrl);

        } catch (NamingException e) {
            throw new SQLException(
                "An error occurred while obtaining the data source: " + e.getMessage(), e);
        }
    }

    public DataSource getDataSource(){
        return dataSource;
    }

}
