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
public class LocationAPIStreamProcessor extends StreamProcessor {

    // private static final String GET_LOCATION = "listLocationInformation";
    private static final String TERMINAL_LOCATION_LIST = "terminalLocationList";
    private static final String TERMINAL_LOCATION = "terminalLocation";
    private static final String ADDRESS = "address";
    private static final String CURRENT_LOCATION = "currentLocation";
    private static final String ACCURACY = "accuracy";
    private static final String ALTITUDE = "altitude";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String TIME_STAMP = "timestamp";
    private static final String LOCATION_RETRIEVAL_STATUS = "locationRetrievalStatus";
    private VariableExpressionExecutor jsonVariableExecutor;
    private ConstantExpressionExecutor jsonConstantExecutorForFunction;


    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor processor, StreamEventCloner
            streamEventCloner, ComplexEventPopulater complexEventPopulater) {


        List<ComplexEventChunk<StreamEvent>> complexEventChunkLists = new ArrayList<ComplexEventChunk<StreamEvent>>();
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

                int year = (Integer) parameterSet[21];
                int month = (Integer) parameterSet[22];
                int day = (Integer) parameterSet[23];
                int hour = (Integer) parameterSet[24];

                String operatorName = parameterSet[25].toString();
                String apiPublisherID = parameterSet[26].toString();
                String apiID = parameterSet[27].toString();
                String department = parameterSet[28].toString();
                String applicationId = parameterSet[29].toString();

                @SuppressWarnings("deprecation")
                JSONParser parser = new JSONParser();
                try {
                    JSONObject content = (JSONObject) parser.parse(jsonBody);
                    String address = null;
                    double accuracy = 0;
                    String altitude = null;
                    String latitude = null;
                    String longitude = null;
                    String timestamp = null;
                    String locationRetrievalStatus = null;

                    if (checkFieldAvailability(content, TERMINAL_LOCATION_LIST)) {

                        JSONObject terminalLocationList = (JSONObject) getObject(content, TERMINAL_LOCATION_LIST);

                        if (checkFieldAvailability(terminalLocationList, TERMINAL_LOCATION)) {
                            JSONArray terminalLocation = (JSONArray) getObject(terminalLocationList, TERMINAL_LOCATION);

                            for (Object terminalLocationVal : terminalLocation) {
                                JSONObject terminalLocationObj = (JSONObject) terminalLocationVal;
                                address = getObject(terminalLocationObj, ADDRESS).toString();
                                if (checkFieldAvailability(terminalLocationObj, CURRENT_LOCATION)) {
                                    JSONObject currentLocation = (JSONObject) getObject(terminalLocationObj,
                                            CURRENT_LOCATION);

                                    if (checkFieldAvailability(currentLocation, ACCURACY)) {
                                        accuracy = Double.parseDouble(getObject(currentLocation, ACCURACY).toString());
                                    }
                                    if (checkFieldAvailability(currentLocation, ALTITUDE)) {
                                        altitude = getObject(currentLocation, ALTITUDE).toString();
                                    }
                                    if (checkFieldAvailability(currentLocation, LATITUDE)) {
                                        latitude = getObject(currentLocation, LATITUDE).toString();
                                    }
                                    if (checkFieldAvailability(currentLocation, LONGITUDE)) {
                                        longitude = getObject(currentLocation, LONGITUDE).toString();
                                    }
                                    if (checkFieldAvailability(currentLocation, TIME_STAMP)) {
                                        timestamp = getObject(currentLocation, TIME_STAMP).toString();
                                    }

                                }

                                locationRetrievalStatus = getObject(terminalLocationObj, LOCATION_RETRIEVAL_STATUS).toString();

                            }
                        }
                    }
                    Object[] outputData = new Object[]{api, resourcePath, method, responseTime, serviceTime,
                            serviceProvider, apiPublisher, applicationName, operatorId,
                            responseCode, msisdn, direction, "", address, accuracy, altitude, latitude, longitude,
                            timestamp, locationRetrievalStatus, serviceProvider,
                            year, month, day, hour, operatorName, apiPublisherID, apiID, department, applicationId};
                    addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, outputData);

                } catch (ParseException ex) {
                    log.error(ex);
                }
                complexEventChunkLists.add(newComplexEventChunk);
            }
        }
        if (complexEventChunkLists.size() > 0) {
            for (ComplexEventChunk<StreamEvent> complexEventChunk : complexEventChunkLists) {
                nextProcessor.process(complexEventChunk);
            }
        }
    }

    private void addToComplexEventChunk(ComplexEventPopulater complexEventPopulater, ComplexEventChunk<StreamEvent>
            newComplexEventChunk, Object[] outputData) {

        StreamEvent newStreamEvent = new StreamEvent(0, 0, outputData.length);
        newStreamEvent.setOutputData(outputData);
        newStreamEvent.setTimestamp(System.currentTimeMillis());
        newComplexEventChunk.add(newStreamEvent);
    }


    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {

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


    private boolean checkFieldAvailability(JSONObject jsonContent, String key) {
        return jsonContent.containsKey(key);
    }

    private Object getObject(JSONObject content, String key) {
        return content.get((Object) key);
    }


}