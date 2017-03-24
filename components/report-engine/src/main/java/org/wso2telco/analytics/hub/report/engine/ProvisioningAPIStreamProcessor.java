/*******************************************************************************
 * Copyright (c) 2016-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.wso2telco.analytics.hub.report.engine;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Stream processor for Provisioning API
 */
public class ProvisioningAPIStreamProcessor extends StreamProcessor {

    private static final String LIST_OF_PROVISIONED = "listOfProvisioned";
    private static final String LIST_OF_APPLICABLE = "listOfApplicable";

    private static final String PROVISIONING_SERVICE_LIST = "serviceList";
    private static final String PROVISIONING_SERVICE_INFO = "serviceInfo";
    private static final String PROVISIONING_SERVICE_CODE = "serviceCode";
    private static final String PROVISIONING_SERVICE_TYPE = "serviceType";
    private static final String PROVISIONING_SERVICE_DESCRIPTION = "description";
    private static final String PROVISIONING_SERVICE_CHARGE = "serviceCharge";
    private static final String PROVISIONING_CURRENCY_CODE = "currencyCode";
    private static final String PROVISIONING_ONBEHALF_OF = "onBehalfOf";
    private static final String PROVISIONING_PURCHASE_CATEGORY = "purchaseCategoryCode";
    private static final String PROVISIONING_REQUEST_IDENTIFIER = "requestIdentifier";
    private static final String PROVISIONING_RESPONSE_IDENTIFIER = "responseIdentifier";
    private static final String PROVISIONING_RESOURCE_URL = "resourceURL";

    public static final String PROVISIONING_SERVICE_INFO_LIST = "serviceInfoList";
    public static final String PROVISIONING_TIMESTAMP = "timestamp";
    public static final String PROVISIONING_TAG = "tag";
    public static final String PROVISIONING_VALUE = "value";

    private VariableExpressionExecutor jsonVariableExecutor;
    private ConstantExpressionExecutor jsonConstantExecutorForFunction;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor processor, StreamEventCloner
            streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        {
            // Initialize the event chunk lists
            List<ComplexEventChunk<StreamEvent>> complexEventChunkList = new
                    ArrayList<ComplexEventChunk<StreamEvent>>();

            synchronized (this) {

                while (streamEventChunk.hasNext()) {
                    // Creates a new complex event chunk
                    ComplexEventChunk<StreamEvent> newComplexEventChunk = new ComplexEventChunk<StreamEvent>(true);

                    StreamEvent compressedEvent = (StreamEvent) streamEventChunk.next();
                    Object[] parameterSet = compressedEvent.getOutputData();
                    String function = jsonConstantExecutorForFunction.execute((ComplexEvent) compressedEvent)
                            .toString();
                    String jsonBody = jsonVariableExecutor.execute((ComplexEvent) compressedEvent).toString();
                    String api = parameterSet[0].toString();
                    String resourcePath = parameterSet[1].toString();
                    String method = parameterSet[2].toString();
                    long responseTime = (Long) parameterSet[3];
                    long serviceTime = (Long) parameterSet[4];
                    String serviceProvider = parameterSet[5].toString();
                    String apiPublisher = parameterSet[6].toString();
                    String applicationName = parameterSet[7].toString();
                    String operatorId = parameterSet[8].toString();
                    String responseCode = parameterSet[9].toString();
                    String msisdn = parameterSet[10].toString();
                    String direction = parameterSet[11].toString();

                    int year = (Integer) parameterSet[33];
                    int month = (Integer) parameterSet[34];
                    int day = (Integer) parameterSet[35];
                    int hour = (Integer) parameterSet[36];

                    String operatorName = parameterSet[37].toString();
                    String apiPublisherID = parameterSet[38].toString();
                    String apiID = parameterSet[39].toString();
                    String department = parameterSet[40].toString();
                    String applicationId = parameterSet[41].toString();

                    @SuppressWarnings("deprecation")
                    JSONParser parser = new JSONParser();

                    try {

                        JSONObject content = (JSONObject) parser.parse(jsonBody);
                        // Handle List applicable
                        if (function.equals(LIST_OF_APPLICABLE)) {

                            if (checkFieldAvailability(content, PROVISIONING_SERVICE_LIST)) {
                                JSONObject provisionServiceList = (JSONObject) getObject(content,
                                        PROVISIONING_SERVICE_LIST);

                                if (checkFieldAvailability(provisionServiceList, PROVISIONING_SERVICE_INFO)) {
                                    JSONArray serviceInfoList = (JSONArray) getObject(provisionServiceList,
                                            PROVISIONING_SERVICE_INFO);

                                    for (Object serviceInfo : serviceInfoList) {
                                        JSONObject serviceInfoObj = (JSONObject) serviceInfo;

                                        String serviceCode = getObject(serviceInfoObj, PROVISIONING_SERVICE_CODE)
                                                .toString();
                                        String serviceType = getObject(serviceInfoObj, PROVISIONING_SERVICE_TYPE)
                                                .toString();
                                        String description = getObject(serviceInfoObj,
                                                PROVISIONING_SERVICE_DESCRIPTION).toString();
                                        String serviceCharge = getObject(serviceInfoObj, PROVISIONING_SERVICE_CHARGE)
                                                .toString();
                                        String currencyCode = getObject(provisionServiceList,
                                                PROVISIONING_CURRENCY_CODE).toString();
                                        String onBehalfOf = getObject(provisionServiceList, PROVISIONING_ONBEHALF_OF)
                                                .toString();
                                        String purchaseCategoryCode = getObject(provisionServiceList,
                                                PROVISIONING_PURCHASE_CATEGORY).toString();
                                        String requestIdentifier = getObject(provisionServiceList,
                                                PROVISIONING_REQUEST_IDENTIFIER).toString();
                                        String responseIdentifier = getObject(provisionServiceList,
                                                PROVISIONING_RESPONSE_IDENTIFIER).toString();
                                        String resourceURL = getObject(provisionServiceList,
                                                PROVISIONING_RESOURCE_URL).toString();

                                        Object[] outputData = new Object[]{api, resourcePath, method, responseTime,
                                                serviceTime, serviceProvider, apiPublisher, applicationName, operatorId,
                                                responseCode, msisdn, direction, "ListOfApplicable", serviceCode,
                                                serviceType, description, serviceCharge, currencyCode, onBehalfOf,
                                                purchaseCategoryCode, requestIdentifier, responseIdentifier,
                                                resourceURL, "", "", "", "", "", "", "", "", 0, "",
                                                year, month, day, hour, operatorName, apiPublisherID, apiID,
                                                department, applicationId};
                                        addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, outputData);
                                    }
                                }
                            }
                        } else if (function.equals(LIST_OF_PROVISIONED)) {

                            if (checkFieldAvailability(content, PROVISIONING_SERVICE_LIST)) {

                                JSONObject serviceList = (JSONObject) getObject(content, PROVISIONING_SERVICE_LIST);
                                if (checkFieldAvailability(serviceList, PROVISIONING_SERVICE_INFO_LIST)) {

                                    JSONArray serviceInfoList = (JSONArray) getObject(serviceList,
                                            PROVISIONING_SERVICE_INFO_LIST);
                                    for (Object serviceInfo : serviceInfoList) {

                                        JSONObject serviceInfoObj = (JSONObject) serviceInfo;
                                        String serviceCode = getObject(serviceInfoObj, PROVISIONING_SERVICE_CODE)
                                                .toString();
                                        String description = getObject(serviceInfoObj,
                                                PROVISIONING_SERVICE_DESCRIPTION).toString();
                                        String timestamp = getObject(serviceInfoObj, PROVISIONING_TIMESTAMP).toString();
                                        String onBehalfOf = getObject(serviceList, PROVISIONING_ONBEHALF_OF).toString();
                                        String purchaseCategoryCode = getObject(serviceList,
                                                PROVISIONING_PURCHASE_CATEGORY)
                                                .toString();
                                        String requestIdentifier = getObject(serviceList,
                                                PROVISIONING_REQUEST_IDENTIFIER)
                                                .toString();
                                        String responseIdentifier = getObject(serviceList,
                                                PROVISIONING_RESPONSE_IDENTIFIER)
                                                .toString();
                                        String resourceURL = getObject(serviceList, PROVISIONING_RESOURCE_URL)
                                                .toString();
                                        if (checkFieldAvailability(serviceInfoObj, PROVISIONING_SERVICE_INFO)) {
                                            JSONArray serviceInfoArray = (JSONArray) getObject(serviceInfoObj,
                                                    PROVISIONING_SERVICE_INFO);
                                            for (Object serviceInfoArrayItemObj : serviceInfoArray) {
                                                JSONObject serviceInfoArrayItemJSON = (JSONObject)
                                                        serviceInfoArrayItemObj;
                                                String serviceInfoTag = getObject(serviceInfoArrayItemJSON,
                                                        PROVISIONING_TAG)
                                                        .toString();
                                                double serviceInfoValue = Double.parseDouble(getObject
                                                        (serviceInfoArrayItemJSON, PROVISIONING_VALUE).toString());
                                                Object[] outputData = new Object[]{api, resourcePath, method,
                                                        responseTime, serviceTime, serviceProvider, apiPublisher,
                                                        applicationName, operatorId,
                                                        responseCode, msisdn, direction, LIST_OF_PROVISIONED,
                                                        serviceCode, "", description, "", "", onBehalfOf,
                                                        purchaseCategoryCode, requestIdentifier, responseIdentifier,
                                                        resourceURL, "", "", "", "", "", "", timestamp,
                                                        serviceInfoTag, serviceInfoValue, "",
                                                        year, month, day, hour, operatorName, apiPublisherID, apiID,
                                                        department, applicationId};
                                                addToComplexEventChunk(complexEventPopulater, newComplexEventChunk,
                                                        outputData);
                                            }
                                        } else {

                                            Object[] outputData = new Object[]{api, resourcePath, method,
                                                    responseTime, serviceTime, serviceProvider, apiPublisher,
                                                    applicationName, operatorId,
                                                    responseCode, msisdn, direction, LIST_OF_PROVISIONED,
                                                    serviceCode, "", description, "", "", onBehalfOf,
                                                    purchaseCategoryCode, requestIdentifier, responseIdentifier,
                                                    resourceURL, "", "", "", "", "", "", timestamp, "", 0, "",
                                                    year, month, day, hour, operatorName, apiPublisherID, apiID,
                                                    department, applicationId};
                                            addToComplexEventChunk(complexEventPopulater, newComplexEventChunk,
                                                    outputData);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ParseException pex) {
                        log.error((Object) pex);
                    }
                    complexEventChunkList.add(newComplexEventChunk);
                }
            }
            if (complexEventChunkList.size() > 0) {
                for (ComplexEventChunk<StreamEvent> complexEventChunk : complexEventChunkList) {
                    nextProcessor.process(complexEventChunk);
                }
            }
        }
    }

    /**
     * Add to complex event chunk
     *
     * @param complexEventPopulater complexEventPopulater
     * @param newComplexEventChunk  newComplexEventChunk
     * @param outputData            outputData
     */
    private void addToComplexEventChunk(ComplexEventPopulater complexEventPopulater,
                                        ComplexEventChunk<StreamEvent> newComplexEventChunk, Object[] outputData) {
        StreamEvent newStreamEvent = new StreamEvent(0, 0, outputData.length);
        newStreamEvent.setOutputData(outputData);
        newStreamEvent.setTimestamp(System.currentTimeMillis());
        newComplexEventChunk.add(newStreamEvent);
    }

    /**
     * Checks the availability of the object
     *
     * @param content content
     * @param key     key
     * @return boolean
     */
    private boolean checkFieldAvailability(JSONObject content, String key) {
        return content.containsKey((Object) key);
    }

    /**
     * Get the value
     *
     * @param content content
     * @param key     key
     * @return content
     */
    private Object getObject(JSONObject content, String key) {

        Object obj = content.get((Object) key);
        if (null != obj) {
            return obj;
        } else {
            return "";
        }
    }

    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext
                                           executionPlanContext) {

        if (attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor) {
            jsonConstantExecutorForFunction = (ConstantExpressionExecutor) attributeExpressionExecutors[1];
        } else {
            throw new ExecutionPlanValidationException(
                    "This should have a constant parameter as the 2nd attribute but doesn't found a constant "
                            + attributeExpressionExecutors[1].getClass().getCanonicalName());
        }

        if (attributeExpressionExecutors[0] instanceof VariableExpressionExecutor) {
            jsonVariableExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[0];
        } else {
            throw new ExecutionPlanValidationException(
                    "This should have a variable expression as the 1st attribute but doesn't found a variable " +
                            "expression "
                            + attributeExpressionExecutors[0].getClass().getCanonicalName());
        }

        return new ArrayList<Attribute>();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] objects) {

    }
}
