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

import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
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
import org.wso2telco.analytics.report.generator.extension.internal.GenerateReportHolder;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsTableSearch extends StreamFunctionProcessor {

    int tenantId = -1;
    private String tableName;
    private List<Attribute> attributes = new ArrayList<Attribute>();

    @Override
    protected Object[] process(Object[] objects) {
        return new Object[0];
    }

    @Override
    protected Object[] process(Object o) {
        return new Object[0];
    }


    //tableName, query, attributes, returnCount (long)
    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors, ExecutionPlanContext executionPlanContext) {
        String attributeStrings;
        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
            tableName = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
        } else {
            throw new ExecutionPlanValidationException("GenerateHtmlReport should have constant parameter as the 1st attribute (tableName) but found a dynamic attribute " + attributeExpressionExecutors[0].getClass().getCanonicalName());
        }
        if (attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor) {
            attributeStrings = ((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue().toString().replaceAll("'", "\"");
        } else {
            throw new ExecutionPlanValidationException("GenerateHtmlReport should have constant parameter as the 3rd attribute (attributes) but found a dynamic attribute " + attributeExpressionExecutors[2].getClass().getCanonicalName());
        }
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String[] attributeArray = attributeStrings.split(",");
        for (String attributeString : attributeArray) {
            String[] nameType = attributeString.trim().replaceAll("  ", " ").split(" ");
            if (Attribute.Type.STRING.toString().equalsIgnoreCase(nameType[1])) {
                attributes.add(new Attribute(nameType[0], Attribute.Type.STRING));
            } else if (Attribute.Type.INT.toString().equalsIgnoreCase(nameType[1])) {
                attributes.add(new Attribute(nameType[0], Attribute.Type.INT));

            } else if (Attribute.Type.FLOAT.toString().equalsIgnoreCase(nameType[1])) {
                attributes.add(new Attribute(nameType[0], Attribute.Type.FLOAT));

            } else if (Attribute.Type.LONG.toString().equalsIgnoreCase(nameType[1])) {
                attributes.add(new Attribute(nameType[0], Attribute.Type.LONG));

            } else if (Attribute.Type.BOOL.toString().equalsIgnoreCase(nameType[1])) {
                attributes.add(new Attribute(nameType[0], Attribute.Type.BOOL));

            } else if (Attribute.Type.DOUBLE.toString().equalsIgnoreCase(nameType[1])) {
                attributes.add(new Attribute(nameType[0], Attribute.Type.DOUBLE));

            } else {
                attributes.add(new Attribute(nameType[0], Attribute.Type.OBJECT));

            }

        }
        return attributes;
    }

    protected void processEventChunk(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        List<ComplexEventChunk> complexEventChunkList = new ArrayList<ComplexEventChunk>();
        synchronized (this) {
            StreamEvent lastEvent = null;
            while (streamEventChunk.hasNext()) {
                lastEvent = streamEventChunk.next();


                try {
                    List<SearchResultEntry> resultEntries = GenerateReportHolder.getAnalyticsDataService().search(tenantId, tableName, attributeExpressionExecutors[1].execute(lastEvent).toString(), 0, (Integer) attributeExpressionExecutors[3].execute(lastEvent));
                    List<String> ids = new ArrayList<String>();
                    for (SearchResultEntry entry : resultEntries) {
                        ids.add(entry.getId());
                    }
                    AnalyticsDataResponse resp = GenerateReportHolder.getAnalyticsDataService().get(tenantId, tableName, 1, null, ids);
                    List<Record> records = AnalyticsDataServiceUtils.listRecords(GenerateReportHolder.getAnalyticsDataService(), resp);

                    if (records.size() > 0) {
                        ComplexEventChunk<StreamEvent> newComplexEventChunk = new ComplexEventChunk<StreamEvent>(true);
                        complexEventChunkList.add(newComplexEventChunk);
                        for (Record record : records) {
                            StreamEvent newStreamEvent = streamEventCloner




                                    .copyStreamEvent(lastEvent);
                            complexEventPopulater.populateComplexEvent(newStreamEvent, recordToObject(attributes, record));
                            newComplexEventChunk.add(newStreamEvent);
                        }
                    }

                } catch (AnalyticsIndexException e) {
                    log.error(e.getMessage(), e);
                } catch (AnalyticsException e) {
                    e.printStackTrace();
                }

            }
        }
        if (complexEventChunkList.size() > 0) {
            for (ComplexEventChunk complexEventChunk : complexEventChunkList) {
                nextProcessor.process(complexEventChunk);
            }
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

    public static Object[] recordToObject(List<Attribute> attrs, Record record) {
        Object[] data = new Object[attrs.size()];
        Attribute attr;
        for (int i = 0; i < attrs.size(); i++) {
            attr = attrs.get(i);
            data[i] = record.getValue(attr.getName());
        }
        return data;
    }
}
