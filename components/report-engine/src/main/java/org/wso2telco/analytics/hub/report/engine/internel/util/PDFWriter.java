package org.wso2telco.analytics.hub.report.engine.internel.util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2telco.analytics.hub.report.engine.DetailReportAlert;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
public class PDFWriter {

    private static final Log log = LogFactory.getLog(PDFWriter.class);

    String fileName = "";
    static String  workingDir = System.getProperty("user.dir");

    public String getFileName() {
        return fileName;
    }

    public static void generatePdf(String pdfName, String jasperFileDir, List<Record> recordList, HashMap params) {

        params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        JasperPrint jasperPrint = null;
        try {
            File reportFile = new File(workingDir + jasperFileDir + ".jasper");   //north bound
            jasperPrint = JasperFillManager.fillReport(reportFile.getPath(), params, getDataSourceDetailReport
                    (recordList));
            File filename = new File(workingDir + "/" + pdfName);
            filename.getParentFile().mkdirs();
            JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(filename + ".pdf"));
            File tmpFile = new File(workingDir + "/" + pdfName + ".wte");
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        } catch (JRException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static JRDataSource getDataSourceDetailReport(List<Record> recordList) {

        Collection<DetailReportAlert> coll = new ArrayList<DetailReportAlert>();

    //    year month direction api apiID  applicationName applicationId serviceProvider  serviceProviderId   operatorName operatorId  operation category  subcategory totalCount totalAmount
        // totalOpCommision  totalSpCommision  totalHbCommision

        for (Record record : recordList) {
            DetailReportAlert reportAlert = new DetailReportAlert();
            reportAlert.setApi(getValue(record.getValues().get("api")));
            reportAlert.setApplicationName(getValue(record.getValues().get("applicationName")));
            reportAlert.setEventType(getValue(record.getValues().get("operation")));
            reportAlert.setSubscriber(getValue(record.getValues().get("serviceProvider")));
            reportAlert.setOperatorName(getValue(record.getValues().get("operatorName")));
            reportAlert.setHubshare(Double.parseDouble(record.getValues().get("totalHbCommision").toString()));
            reportAlert.setSpshare(Double.parseDouble(record.getValues().get("totalAmount").toString()));
            reportAlert.setOperatorshare(record.getValues().get("totalOpCommision") != null ? Double.parseDouble
                    (getValue(record.getValues().get("totalOpCommision"))) : null);
            reportAlert.setTax(Double.parseDouble(record.getValues().get("totalTaxAmount").toString()));
            reportAlert.setTotalamount(Double.parseDouble(record.getValues().get("totalAmount").toString()));

            coll.add(reportAlert);
        }

        return new JRBeanCollectionDataSource(coll, false);
    }

    private static String getValue(Object val) {

        if (val != null) {
            return val.toString();
        } else {
            return "";
        }

    }
    private static JRDataSource getCurrentMonthDataSourceReport(Collection<DetailReportAlert> reportCollection)
    {

        return  new JRBeanCollectionDataSource(reportCollection,false);
    }

    public static void generateKillBillPdf(String pdfName, String jasperFileDir,List<Record> list,HashMap params)
    {
        params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        JasperPrint jasperPrint = null;
        try {
            File reportFile = new File(workingDir + jasperFileDir + ".jasper");   //north bound
            String year = (String) params.get("R_YEAR");
            String month = (String) params.get("R_MONTH");
            Formatter monthFormat = new Formatter();
            Calendar calendar = Calendar.getInstance();
            String currentMonth = monthFormat.format("%tB", calendar).toString();
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            String currentYearValue = Integer.toString(currentYear);
            if(currentYearValue.equals(year) && currentMonth.equals(month))
            {
                jasperPrint = JasperFillManager.fillReport(reportFile.getPath(), params);
            }

            File filename = new File(workingDir + "/" + pdfName);
            filename.getParentFile().mkdirs();
            JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(filename + ".pdf"));
        } catch (JRException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void generatePdf(String pdfName, String jasperFileDir,Collection<DetailReportAlert> collection, HashMap<String,Object> params)
    {
        params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        JasperPrint jasperPrint = null;
        try {
            File reportFile = new File(workingDir + jasperFileDir + ".jasper");   //north bound

                jasperPrint = JasperFillManager.fillReport(reportFile.getPath(), params, getCurrentMonthDataSourceReport
                        (collection));
            File filename = new File(workingDir + "/" + pdfName);
            filename.getParentFile().mkdirs();
            JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(filename + ".pdf"));
        } catch (JRException e) {
            log.error("An error occurred while converting to PDF"+e);
        } catch (FileNotFoundException e) {
            log.error("File cannot be found"+e);
        }
    }

}
