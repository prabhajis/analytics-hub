package org.wso2telco.analytics.report.generator.extension;


import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;


public class DataExport {

    String fileName = "";

    public String getUuid() {
        return uuid;
    }


    String uuid = UUID.randomUUID().toString();
    String workingDir = System.getProperty("user.dir");


    public String getFileName() {
        return fileName;
    }


    public  void generatPdf(String pdfName,String jasperFileDir,HashMap map,HashMap params) {
        params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        try {
            jasperReport = JasperCompileManager.compileReport(workingDir+jasperFileDir + ".jrxml");
            jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSource(map));
            File filename = new File(workingDir+pdfName + uuid );
            JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(filename+".pdf"));
        } catch (JRException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public  void generatPdfDR(String pdfName,String jasperFileDir,HashMap map,HashMap params) {
        params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        try {
            jasperReport = JasperCompileManager.compileReport(workingDir+jasperFileDir + ".jrxml");
            jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSourceDetailReport(map));
            File filename = new File(workingDir+pdfName + uuid );
            JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(filename+".pdf"));
        } catch (JRException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    public void generatExcelDR(String excelName,String jasperFileDir,HashMap map,HashMap params) {
//        try {
//
//            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
//            JasperReport jasperReport =JasperCompileManager.compileReport(workingDir+jasperFileDir + ".jrxml");
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSourceDetailReport(map));
//
//            JRXlsxExporter exporter = new JRXlsxExporter();
//            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
//            exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, workingDir+excelName+uuid+".xls");
//
//            exporter.exportReport();
//
//
//
//        } catch (JRException e) {
//            e.printStackTrace();
//        }
//
//    }

    public void generatHTMLDR(String htmlName,String jasperFileDir,HashMap map,HashMap params) {

        JasperReport jasperReport = null;
        JasperPrint jasperPrint = null;
        try {
            jasperReport = JasperCompileManager.compileReport(jasperFileDir + ".jrxml");
            jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSourceDetailReport(map));

            JasperExportManager.exportReportToHtmlFile(jasperPrint, htmlName+uuid+".html");
        } catch (JRException e) {
            e.printStackTrace();
        }

    }

    public void generatExcel(String excelName,String jasperFileDir,HashMap map,HashMap params) {
        try {

            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            JasperReport jasperReport =JasperCompileManager.compileReport(workingDir+jasperFileDir + ".jrxml");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSource(map));

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, workingDir+excelName+uuid+".xls");

            exporter.exportReport();



        } catch (JRException e) {
            e.printStackTrace();
        }

    }

    public void generatExcelDR(String excelName,String jasperFileDir,HashMap map,HashMap params) {
        try {

            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            JasperReport jasperReport =JasperCompileManager.compileReport(workingDir+jasperFileDir + ".jrxml");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSourceDetailReport(map));
            File filename = new File(workingDir+excelName+uuid);
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, filename+".xls");

            exporter.exportReport();



        } catch (JRException e) {
            e.printStackTrace();
        }

    }

    private static JRDataSource getDataSource(Map<String,ArrayList> map) {


        Collection<BeanWithList> coll = new ArrayList<BeanWithList>();
        BeanWithList bean ;

        for(Map.Entry<String, ArrayList> entry : map.entrySet()){

            bean = new BeanWithList(entry.getValue(), entry.getKey());
            coll.add(bean);
        }

        return new JRBeanCollectionDataSource(coll);
    }

    private static JRDataSource getDataSourceDetailReport(Map<String,ArrayList> map) {


        Collection<DetailReportAlert> coll = new ArrayList<DetailReportAlert>();
        DetailReportAlert bean ;

        for(Map.Entry<String, ArrayList> entry : map.entrySet()){
            if(entry.getValue().size()>=12 ) {
                bean = new DetailReportAlert(entry.getKey(), entry.getValue());
                coll.add(bean);
            }
        }

        return new JRBeanCollectionDataSource(coll);
    }









}