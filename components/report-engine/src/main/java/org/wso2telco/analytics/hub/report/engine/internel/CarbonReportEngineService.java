package org.wso2telco.analytics.hub.report.engine.internel;


import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.*;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2telco.analytics.hub.report.engine.DetailReportAlert;
import org.wso2telco.analytics.hub.report.engine.ReportEngineService;
import org.wso2telco.analytics.hub.report.engine.internel.ds.ReportEngineServiceHolder;
import org.wso2telco.analytics.hub.report.engine.internel.util.CSVWriter;
import org.wso2telco.analytics.hub.report.engine.internel.util.ReportEngineServiceConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CarbonReportEngineService implements ReportEngineService {

    private static ThreadPoolExecutor threadPoolExecutor;

    public CarbonReportEngineService() {
        threadPoolExecutor = new ThreadPoolExecutor(ReportEngineServiceConstants.SERVICE_MIN_THREAD_POOL_SIZE,
                ReportEngineServiceConstants.SERVICE_MAX_THREAD_POOL_SIZE,
                ReportEngineServiceConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(ReportEngineServiceConstants.SERVICE_EXECUTOR_JOB_QUEUE_SIZE));
    }

    public void generateCSVReport(String tableName, String query, String reportName, int maxLength, String
            reportType, String columns) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);


        threadPoolExecutor.submit(new ReportEngineGenerator(tableName, query, maxLength, reportName, tenantId,
                reportType, columns));
    }
}


class ReportEngineGenerator implements Runnable {

    private static final Log log = LogFactory.getLog(ReportEngineGenerator.class);

    private String tableName;

    private String query;

    private int maxLength;

    private String reportName;

    private int tenantId;

    private String reportType;

    private String columns;

    public ReportEngineGenerator(String tableName, String query, int maxLength, String reportName, int tenantId,
                                 String reportType, String columns) {
        this.tableName = tableName;
        this.query = query;
        this.maxLength = maxLength;
        this.reportName = reportName;
        this.tenantId = tenantId;
        this.reportType = reportType;
        this.columns = columns;
    }

    @Override
    public void run() {
        try {


            int searchCount = ReportEngineServiceHolder.getAnalyticsDataService()
                    .searchCount(tenantId, tableName, query);

            int writeBufferLength = 8192;

            if (reportType.equalsIgnoreCase("transaction")) {
                //Check weather search count is greater than the max file length and split files accordingly
                if (searchCount > maxLength) {
                    for (int i = 0; i < searchCount; ) {
                        int end = i + maxLength;
                        String filepath = reportName + "-" + i + "-" + end + ".csv";
                        generate(tableName, query, filepath, tenantId, i, maxLength, writeBufferLength);
                        i = end;
                    }
                } else {
                    String filepath = reportName + ".csv";
                    generate(tableName, query, filepath, tenantId, 0, searchCount, writeBufferLength);
                }
            } else if (reportType.equalsIgnoreCase("traffic")) {
                String filepath = reportName + ".csv";
                generate(tableName, query, filepath, tenantId, 0, searchCount, writeBufferLength);
            }


        } catch (AnalyticsException e) {
            log.error("Data cannot be loaded for " + reportName + "report", e);
        }
    }

    public void generate(String tableName, String query, String filePath, int tenantId, int start,
                         int maxLength, int writeBufferLength)
            throws AnalyticsException {

        int dataCount = ReportEngineServiceHolder.getAnalyticsDataService()
                .searchCount(tenantId, tableName, query);
        List<Record> records = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        if (dataCount > 0) {
            List<SearchResultEntry> resultEntries = ReportEngineServiceHolder.getAnalyticsDataService()
                    .search(tenantId, tableName, query, start, maxLength);

            for (SearchResultEntry entry : resultEntries) {
                ids.add(entry.getId());
            }
            AnalyticsDataResponse resp = ReportEngineServiceHolder.getAnalyticsDataService()
                    .get(tenantId, tableName, 1, null, ids);

            records = AnalyticsDataServiceUtils
                    .listRecords(ReportEngineServiceHolder.getAnalyticsDataService(), resp);
            Collections.sort(records, new Comparator<Record>() {
                @Override
                public int compare(Record o1, Record o2) {
                    return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                }
            });
        }


        //------------------------------------------------------------------

        HashMap<String, String> paraMap = new HashMap<>();
        paraMap.put("col1","API");
        paraMap.put("col2","APP NAME");
        paraMap.put("col3","EVENT TYPE");
        paraMap.put("col4","OPERATOR");
        paraMap.put("col5","P.C.CODE");
        paraMap.put("col6","COUNT");
        paraMap.put("col7","TOTAL");
        paraMap.put("col8","SPCOMMISSION");
        paraMap.put("col9","SPREVSHARE");

        //------------------------------------------------------------------

        Map<String, String> dataColumns = new LinkedHashMap<>();
        List<String> columnHeads = new ArrayList<>();
        if (StringUtils.isNotBlank(columns)) {
            try {

                JSONArray d = new JSONArray(columns);
                for (int i = 0; i < d.length(); i++) {
                    JSONObject jo = d.getJSONObject(i);
                    dataColumns.put(jo.get("column").toString(), jo.get("type").toString());
                    columnHeads.add(jo.get("label").toString());
                }
            } catch (JSONException e) {
                log.error("Invalid Json", e);
            }
        }

        try {
            if (reportType.equalsIgnoreCase("traffic")) {

        //        generatePdf("eeeeeeeeeee", "/repository/conf/report1", records, paraMap);

                CSVWriter.writeTrafficCSV(records, writeBufferLength, filePath,tableName);

            } else {
                CSVWriter.writeCSV(records, writeBufferLength, filePath, dataColumns, columnHeads);
            }
        } catch (IOException e) {
            log.error("CSV file " + filePath + " cannot be created", e);
        }
    }


    String fileName = "";

    public String getUuid() {
        return uuid;
    }


    String uuid = UUID.randomUUID().toString();
    String workingDir = System.getProperty("user.dir");


    public String getFileName() {
        return fileName;
    }

    public  void generatePdf(String pdfName,String jasperFileDir,List<Record> recordList,HashMap params) {
        params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        try {
            jasperReport = JasperCompileManager.compileReport(workingDir+jasperFileDir + ".jrxml");
            jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSourceDetailReport(recordList));
            File filename = new File(workingDir+ "/repository/deployment/server/jaggeryapps/portal/reports/traffic/"+ pdfName + uuid );
            JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(filename+".pdf"));
        } catch (JRException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static JRDataSource getDataSourceDetailReport(List<Record> recordList) {

        Collection<DetailReportAlert> coll = new ArrayList<DetailReportAlert>();

        for(Record record : recordList){
            DetailReportAlert reportAlert = new DetailReportAlert();
            reportAlert.setApi(record.getValues().get("api").toString());
            reportAlert.setApplicationName(record.getValues().get("applicationName").toString());
            reportAlert.setOperatorName(record.getValues().get("operatorName").toString());
           // reportAlert.setDirection(record.getValues().get("direction").toString());
            reportAlert.setEventType(record.getValues().get("eventType").toString());
            reportAlert.setPurchaseCategoryCode(record.getValues().get("purchaseCategoryCode").toString());
            reportAlert.setSum_totalAmount(Double.valueOf(record.getValues().get("sum_totalAmount").toString()));
            reportAlert.setSpcommission(Double.valueOf(record.getValues().get("spcommission").toString()));
            reportAlert.setRevShare_sp(Double.valueOf(record.getValues().get("revShare_sp").toString()));
            coll.add(reportAlert);
        }

        return new JRBeanCollectionDataSource(coll);
    }


}