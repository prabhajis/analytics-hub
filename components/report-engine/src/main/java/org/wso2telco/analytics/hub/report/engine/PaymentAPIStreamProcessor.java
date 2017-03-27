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

/*
 * Stream processor for PAYMENT API
 */
public class PaymentAPIStreamProcessor extends StreamProcessor {

    private static final String LIST_CHARGE_OPERATIONS = "listChargeOperations";

    private static final String PAYMENT_TRANSACTION_LIST = "paymentTransactionList";
    private static final String AMOUNT_TRANSACTION = "amountTransaction";
    private static final String END_USER_ID = "endUserId";
    private static final String REFERENCE_CODE = "referenceCode";
    private static final String SERVER_REFERENCE_CODE = "serverReferenceCode";
    private static final String RESOURCE_URL = "resourceURL";
    private static final String TRANSACTION_OPERATION_STATUS = "transactionOperationStatus";

    private static final String PAYMENT_AMOUNT = "paymentAmount";
    private static final String CHARGING_INFORMATION = "chargingInformation";
    private static final String AMOUNT = "amount";
    private static final String CURRENCY = "currency";
    private static final String DESCRIPTION = "description";

    private VariableExpressionExecutor jsonVariableExecutor;
    private ConstantExpressionExecutor jsonConstantExecutorForFunction;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor processor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        {

            // Initialize the event chunk lists
            List<ComplexEventChunk<StreamEvent>> complexEventChunkList = new ArrayList<ComplexEventChunk<StreamEvent>>();

            synchronized (this) {

                while (streamEventChunk.hasNext()) {

                    // Creates a new complex event chunk
                    ComplexEventChunk<StreamEvent> newComplexEventChunk = new ComplexEventChunk<StreamEvent>(true);

                    StreamEvent compressedEvent = (StreamEvent) streamEventChunk.next();
                    Object[] parameterSet = compressedEvent.getOutputData();
                    String function = jsonConstantExecutorForFunction.execute((ComplexEvent) compressedEvent).toString();
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

                    int year = (Integer) parameterSet[27];
                    int month = (Integer) parameterSet[28];
                    int day = (Integer) parameterSet[29];
                    int hour = (Integer) parameterSet[30];

                    String operatorName = parameterSet[31].toString();
                    String apiPublisherID = parameterSet[32].toString();
                    String apiID = parameterSet[33].toString();
                    String department = parameterSet[34].toString();
                    String applicationId = parameterSet[35].toString();


                    @SuppressWarnings("deprecation")
                    JSONParser parser = new JSONParser();

                    try {

                        JSONObject content = (JSONObject) parser.parse(jsonBody);

                        // Handle List Charge Operations
                        if (function.equals(LIST_CHARGE_OPERATIONS)) {

                            if (checkFieldAvailability(content, PAYMENT_TRANSACTION_LIST)) {
                                JSONObject paymentTransactionList = (JSONObject) getObject(content, PAYMENT_TRANSACTION_LIST);

                                if (checkFieldAvailability(paymentTransactionList, AMOUNT_TRANSACTION)) {
                                    JSONArray amountTransactionsList = (JSONArray) getObject(paymentTransactionList, AMOUNT_TRANSACTION);

                                    for (Object amountTransaction : amountTransactionsList) {
                                        JSONObject amountTransactionObj = (JSONObject) amountTransaction;

                                        String resourceURL = null;
                                        String currency = null;
                                        String description = null;
                                        double amount = 0;

                                        if (checkFieldAvailability(amountTransactionObj, RESOURCE_URL)) {
                                            resourceURL = getObject(amountTransactionObj, RESOURCE_URL).toString();
                                        }

                                        String endUserId = getObject(amountTransactionObj, END_USER_ID).toString();
                                        String referenceCode = getObject(amountTransactionObj, REFERENCE_CODE).toString();
                                        String serverReferenceCode = getObject(amountTransactionObj, SERVER_REFERENCE_CODE).toString();
                                        String transactionOperationStatus = getObject(amountTransactionObj, TRANSACTION_OPERATION_STATUS).toString();

                                        if (checkFieldAvailability(amountTransactionObj, PAYMENT_AMOUNT)) {
                                            JSONObject paymentAmount = (JSONObject) getObject(amountTransactionObj, PAYMENT_AMOUNT);

                                            if (checkFieldAvailability(paymentAmount, CHARGING_INFORMATION)) {
                                                JSONObject chargingInformation = (JSONObject) getObject(paymentAmount, CHARGING_INFORMATION);

                                                if (checkFieldAvailability(chargingInformation, CURRENCY)) {
                                                    currency = getObject(chargingInformation, CURRENCY).toString();
                                                }
                                                if (checkFieldAvailability(chargingInformation, DESCRIPTION)) {
                                                    description = getObject(chargingInformation, DESCRIPTION).toString();
                                                }
                                                if (checkFieldAvailability(chargingInformation, AMOUNT)) {
                                                    amount = Double.parseDouble(getObject(chargingInformation, AMOUNT).toString());
                                                }
                                            }
                                        }

                                        Object[] outputData = new Object[]{api, resourcePath, method, responseTime, serviceTime, serviceProvider, apiPublisher, applicationName, operatorId,
                                                responseCode, msisdn, direction, "", 0, "", "", 0, "", amount, currency,
                                                description, serverReferenceCode, "", transactionOperationStatus, referenceCode, endUserId, resourceURL,
                                                year, month, day, hour, operatorName, apiPublisherID, apiID, department, applicationId};

                                        addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, outputData);
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
        return content.get((Object) key);
    }

    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

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
                    "This should have a variable expression as the 1st attribute but doesn't found a variable expression "
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
