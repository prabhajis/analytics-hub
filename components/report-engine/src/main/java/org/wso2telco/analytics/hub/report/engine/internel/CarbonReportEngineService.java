package org.wso2telco.analytics.hub.report.engine.internel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2telco.analytics.hub.report.engine.ReportEngineService;
import org.wso2telco.analytics.hub.report.engine.internel.ds.ReportEngineServiceHolder;
import org.wso2telco.analytics.hub.report.engine.internel.util.CSVWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CarbonReportEngineService implements ReportEngineService {

    private static final Log log = LogFactory.getLog(CarbonReportEngineService.class);

    public void generateCSVReport(String tableName, String query, String reportName, int maxLength) {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

            int searchCount =  ReportEngineServiceHolder.getAnalyticsDataService()
                                                .searchCount(tenantId, tableName, query);

            int writeBufferLength = 8192;

            //Check weather search count is greater than the max file length and split files accordingly
            if (searchCount > maxLength) {
                for (int i = 0; i < searchCount; ) {
                    int end = i + maxLength;
                    String filepath = reportName +"-" + ".csv";
                    generateCSV(tableName, query, filepath, tenantId, i, end, writeBufferLength);
                    i = end;
                }
            } else {
                String filepath = reportName + ".csv";
                generateCSV(tableName, query, filepath, tenantId, 0, searchCount, writeBufferLength);
            }


        } catch (AnalyticsException e) {
            log.error("Data cannot be loaded for " + reportName + "report", e);
        }


    }

    public void generateCSV(String tableName, String query, String filePath, int tenantId, int start,
                            int maxLength, int writeBufferLength)
            throws AnalyticsException{

        List<SearchResultEntry> resultEntries = ReportEngineServiceHolder.getAnalyticsDataService()
                .search(tenantId, tableName, query, start, maxLength);

        List<String> ids = new ArrayList<>();
        for (SearchResultEntry entry : resultEntries) {
            ids.add(entry.getId());
        }
        AnalyticsDataResponse resp = ReportEngineServiceHolder.getAnalyticsDataService()
                .get(tenantId, tableName, 1, null, ids);

        List<Record> records = AnalyticsDataServiceUtils
                .listRecords(ReportEngineServiceHolder.getAnalyticsDataService(), resp);

        try {
            CSVWriter.write(records, writeBufferLength, filePath);
        } catch (IOException e) {
            log.error("CSV file " + filePath + " cannot be created", e);
        }
    }
}
