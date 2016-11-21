/*
 * Copyright (c) 2016, WSO2.Telco Inc. ((http://www.wso2telco.com)) All Rights Reserved.
 *
 * WSO2.Telco Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2telco.analytics.report.generator.extension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.export.HtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.olap4j.impl.Base64;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateHtmlReport extends StreamFunctionProcessor {

    final Map<String, String> images = new HashMap<String, String>();
    JasperReport jasperReport = null;
    Pattern pattern = Pattern.compile("[\\s|\\S]*(<body[^>]*>)([\\s|\\S]*)(<\\/body>)[\\s|\\S]*");

    @Override
    protected Object[] process(Object[] objects) {
        return new Object[0];
    }

    @Override
    protected Object[] process(Object o) {
        return new Object[0];
    }


    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors, ExecutionPlanContext executionPlanContext) {

        String jasperFilePath;

        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
            jasperFilePath = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
        } else {
            throw new ExecutionPlanValidationException("GenerateHtmlReport should have constant parameter as the 1st attribute (jasperFilePath) but found a dynamic attribute " + attributeExpressionExecutors[0].getClass().getCanonicalName());
        }
//        String jasperFilePath = "/Users/suho/wso2/support/telco/report.jrxml";
//        String paramsJson = "{\"IS_IGNORE_PAGINATION\":true,\"app\":\"colhedding2\",\"from\":\"243\",\"to\":\"2\",\"operator\":\"2sa\",\"col1\":\"900\"}";
        try {
            jasperReport = JasperCompileManager.compileReport(jasperFilePath);

        } catch (JRException e1) {
            log.error(e1.getMessage(), e1);
            throw new RuntimeException(e1.getMessage(), e1);
        }
        ArrayList attributeList = new ArrayList<Attribute>();
        attributeList.add(new Attribute("htmlBody", Attribute.Type.STRING));
        attributeList.add(new Attribute("html", Attribute.Type.STRING));
        return attributeList;
    }

    protected void processEventChunk(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        TreeMap<String, ArrayList<String>> dataMap = new TreeMap<String, ArrayList<String>>();
        ComplexEvent lastEvent = null;
        while (streamEventChunk.hasNext()) {
            lastEvent = streamEventChunk.next();

            ArrayList<String> data = new ArrayList<String>();
            int i = 3;
            while (i < this.attributeExpressionLength) {
                data.add(this.attributeExpressionExecutors[i].execute(lastEvent).toString());
                ++i;
            }
            dataMap.put(this.attributeExpressionExecutors[2].execute(lastEvent).toString(), data);

            if (lastEvent.getNext() != null) {
                streamEventChunk.remove();
            } else {
                try {

                    String paramsJson = attributeExpressionExecutors[1].execute(lastEvent).toString().replaceAll("'", "\"");
                    HashMap<String, Object> params = new ObjectMapper().readValue(paramsJson, HashMap.class);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, getDataSourceDetailReport(dataMap));

                    HtmlExporter exporterHTML = new HtmlExporter();
                    SimpleExporterInput exporterInput = new SimpleExporterInput(jasperPrint);
                    exporterHTML.setExporterInput(exporterInput);
                    HtmlReportConfiguration reportExportConfiguration = new SimpleHtmlReportConfiguration();
                    exporterHTML.setConfiguration(reportExportConfiguration);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    SimpleHtmlExporterOutput simpleHtmlExporterOutput = new SimpleHtmlExporterOutput(outputStream);
                    simpleHtmlExporterOutput.setImageHandler(new HtmlResourceHandler() {

                        public void handleResource(String id, byte[] data) {
                            System.err.println("id" + id);
                            images.put(id, "data:image/jpg;base64," + Base64.encodeBytes(data));
                        }

                        public String getResourcePath(String id) {
                            return images.get(id);
                        }
                    });
                    exporterHTML.setExporterOutput(simpleHtmlExporterOutput);

                    exporterHTML.exportReport();
                    String htmlData = new String(outputStream.toByteArray());

//                    System.out.println(htmlData);

                    Matcher matcher = pattern.matcher(htmlData);
                    if (matcher.matches()) {
                        Object[] outputData = new Object[]{matcher.group(2), htmlData};
//                        System.out.println(matcher.group(2));
                        complexEventPopulater.populateComplexEvent(lastEvent, outputData);
                    } else {
                        log.error("Message did not have HTML body");
                        streamEventChunk.remove();
                    }
                } catch (JRException e) {
                    log.error(e.getMessage(), e);
                    streamEventChunk.remove();
                } catch (JsonParseException e) {
                    log.error(e.getMessage(), e);
                    streamEventChunk.remove();
                } catch (JsonMappingException e) {
                    log.error(e.getMessage(), e);
                    streamEventChunk.remove();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    streamEventChunk.remove();
                }
            }
        }
        streamEventChunk.reset();
        if (streamEventChunk.hasNext()) {
            nextProcessor.process(streamEventChunk);
        }
    }

    public void start() {

    }


    public void stop() {

    }


    public Object[] currentState() {
        return new Object[0];
    }


    public void restoreState(Object[] objects) {

    }

    private JRDataSource getDataSourceDetailReport(TreeMap<String, ArrayList<String>> map) {

        Collection<DetailReportAlert> coll = new ArrayList<DetailReportAlert>();
        DetailReportAlert bean;
        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
            bean = new DetailReportAlert(entry.getKey(), entry.getValue());
            coll.add(bean);
        }
        return new JRBeanCollectionDataSource(coll);
    }
}
