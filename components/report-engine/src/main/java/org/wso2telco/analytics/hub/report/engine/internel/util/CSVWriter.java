/*
 *  Copyright (c) 2016 WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2telco.analytics.hub.report.engine.internel.util;

import org.wso2.carbon.analytics.datasource.commons.Record;

import java.io.*;
import java.util.List;

public class CSVWriter {

    public static void write(List<Record> records, int bufSize, String filePath) throws IOException {

        File file = new File(filePath);
        file.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(file, true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);


        for (Record record: records) {
            StringBuilder sb = new StringBuilder();
            sb.append(record.getValues().get("api"));
            sb.append(',');
            sb.append(record.getValues().get("msisdn"));
            sb.append(',');
            sb.append(record.getValues().get("responseTime"));
            sb.append(',');
            sb.append(record.getValues().get("serviceProvider"));
            sb.append(',');
            sb.append(record.getValues().get("applicationName"));
            sb.append(',');
            sb.append(record.getValues().get("operatorId"));
            sb.append(',');
            sb.append(record.getValues().get("responseCode"));

            sb.append(System.getProperty("line.separator"));

            bufferedWriter.write(sb.toString());
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
