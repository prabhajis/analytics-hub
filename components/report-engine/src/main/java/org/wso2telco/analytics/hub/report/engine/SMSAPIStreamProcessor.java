/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
 * Stream processor for SMS API
 */
public class SMSAPIStreamProcessor extends StreamProcessor {

	private static final String OUTBOUND_SMS_MESSAGE_REQUEST = "outboundSMSMessageRequest";
	private static final String OUTBOUND_SMS_TEXT_MESSAGE = "outboundSMSTextMessage";
	private static final String ADDRESS = "address";
	private static final String MESSAGE = "message";
	private static final String CLIENT_CORRELATOR = "clientCorrelator";
	private static final String DELIVERY_INFO_LIST = "deliveryInfoList";
	private static final String DELIVERY_INFO = "deliveryInfo";
	private static final String DELIVERY_STATUS = "deliveryStatus";
	private static final String SENDER_ADDRESS = "senderAddress";
	private static final String SOUTH_BOUND = "south-bound";
	private static final String NORTH_BOUND = "north-bound";
	private static final String EVENT_TYPE_SEND_SMS = "Outbound";

	private static final String INBOUND_SMS_MESSAGE_LIST = "inboundSMSMessageList";
	private static final String INBOUND_SMS_MESSAGE = "inboundSMSMessage";
	private static final String DESTINATION_ADDRESS = "destinationAddress";
	private static final String OPERATOR_CODE = "operatorCode";
	private static final String MESSAGE_ID = "messageId";
	private static final String EVENT_TYPE_RECEIVE_SMS = "Inbound";

	private static final String DELIVERY_INFO_NOTIFICATION = "deliveryInfoNotification";
	private static final String EVENT_TYPE_DELIVERY_NOTIFICATION = "DNQuery";
	private static final String FILTER_CRITERIA = "filterCriteria";

	private static final String SEND_SMS = "sendSMS";
	private static final String RECEIVE_SMS = "receiveSMS";
	private static final String DELIVERY_NOTIFICATION = "deliveryNotification";
	private static final String DELIVERY_RECEIPT_SUBSCRIPTION = "deliveryReceiptSubscription";
	private static final String SENDER_ADDRESSES = "senderAddresses";


	VariableExpressionExecutor jsonVariableExecutor;
	ConstantExpressionExecutor jsonConstantExecutorForFunction;
	ConstantExpressionExecutor jsonConstantExecutorForDirection;

	public void start() {
	}

	public void stop() {
	}

	public Object[] currentState() {
		return new Object[0];
	}

	public void restoreState(Object[] state) {
	}

	protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
			StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

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
				long responseTime = (Long) parameterSet[1];
				long serviceTime = (Long) parameterSet[2];
				String serviceProvider = parameterSet[3].toString();
				String apiPublisher = parameterSet[4].toString();
				String applicationName = parameterSet[5].toString();
				String operatorId = parameterSet[6].toString();
				String responseCode = parameterSet[7].toString();
				String msisdn = parameterSet[8].toString();
				String direction = parameterSet[9].toString();
						
				int year = 0;
				int month = 0;
				int day = 0;
				int hour = 0;

				@SuppressWarnings("deprecation")
				JSONParser parser = new JSONParser();
				Object[] outputData = null;

				try {

					JSONObject content = (JSONObject) parser.parse(jsonBody);
					String clientCorrelator = null;
					String message = null;
					String senderAddress = null;

					// Handle Send SMS
					if (function.equals(SEND_SMS)) {
						
						year = (Integer) parameterSet[17];
						month = (Integer) parameterSet[18];
						day = (Integer) parameterSet[19];
						hour = (Integer) parameterSet[20];

						int count = 0;
						// Get the message, clientcorrelator and count
						if (checkFieldAvailability(content, OUTBOUND_SMS_MESSAGE_REQUEST)) {
							JSONObject outboundSMSMessageRequest = (JSONObject) getObject(content,
									OUTBOUND_SMS_MESSAGE_REQUEST);
							if (checkFieldAvailability(outboundSMSMessageRequest, OUTBOUND_SMS_TEXT_MESSAGE)) {
								JSONObject outboundSMSTextMessage = (JSONObject) getObject(outboundSMSMessageRequest,
										OUTBOUND_SMS_TEXT_MESSAGE);
								if (checkFieldAvailability(outboundSMSTextMessage, MESSAGE)) {
									message = getObject(outboundSMSTextMessage, MESSAGE).toString();
									count = checkMessageLength(message.toString());
								}

							}
							if (checkFieldAvailability(outboundSMSMessageRequest, CLIENT_CORRELATOR)) {
								clientCorrelator = getObject(outboundSMSMessageRequest, CLIENT_CORRELATOR).toString();
							}
							// Get required information for southbound
							if (direction.equals(SOUTH_BOUND)) {

						
								if (checkFieldAvailability(outboundSMSMessageRequest, SENDER_ADDRESS)) {
									senderAddress = getObject(outboundSMSMessageRequest, SENDER_ADDRESS).toString();
								}
								if (checkFieldAvailability(outboundSMSMessageRequest, DELIVERY_INFO_LIST)) {
									JSONObject deliveryInfoList = (JSONObject) getObject(outboundSMSMessageRequest,
											DELIVERY_INFO_LIST);
									if (checkFieldAvailability(deliveryInfoList, DELIVERY_INFO)) {
										JSONArray deliveryInfos = (JSONArray) getObject(deliveryInfoList, DELIVERY_INFO);
										for (Object deliveryInfo : deliveryInfos) {
											JSONObject infoObj = (JSONObject) deliveryInfo;
											String address = getObject(infoObj, ADDRESS).toString();
											String status = getObject(infoObj, DELIVERY_STATUS).toString();
											outputData = new Object[] { api, responseTime, serviceTime,
													serviceProvider, apiPublisher, applicationName, operatorId,
													responseCode, msisdn,direction, EVENT_TYPE_SEND_SMS, clientCorrelator,
													senderAddress, address, status, message, count, year, month, day,
													hour };
													addToComplexEventChunk(complexEventPopulater, newComplexEventChunk,
													outputData);
										}
									}
								}

							}
							// Get required information for northbound
							else if (direction.equals(NORTH_BOUND)) {
								
								//Adding empty string values
								String senderAddressValue ="";
								String statusValue = "";

								if (checkFieldAvailability(outboundSMSMessageRequest, ADDRESS)) {
									JSONArray addressArray = (JSONArray) getObject(outboundSMSMessageRequest, ADDRESS);
									for (Object addressObj : addressArray) {
										String address = addressObj.toString();
										outputData = new Object[] { api, responseTime, serviceTime, serviceProvider,
												apiPublisher, applicationName, operatorId, responseCode, msisdn,direction,
												EVENT_TYPE_SEND_SMS, clientCorrelator, senderAddressValue, address, statusValue,message, count, year,
												month, day, hour };
										addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, outputData);
									}
								}
							}

						}

					}
					// Handle Receive SMS
					else if (function.equals(RECEIVE_SMS)) {

						year = (Integer) parameterSet[17];
						month = (Integer) parameterSet[18];
						day = (Integer) parameterSet[19];
						hour = (Integer) parameterSet[20];

						// Get fields of the receive SMS
						if (direction.equals(SOUTH_BOUND) && checkFieldAvailability(content, INBOUND_SMS_MESSAGE_LIST)) {
							JSONObject inboundSMSMessageList = (JSONObject) getObject(content, INBOUND_SMS_MESSAGE_LIST);
							if (checkFieldAvailability(inboundSMSMessageList, CLIENT_CORRELATOR)) {
								clientCorrelator = getObject(inboundSMSMessageList, CLIENT_CORRELATOR).toString();
							}
							if (checkFieldAvailability(inboundSMSMessageList, INBOUND_SMS_MESSAGE)) {
								JSONArray inboundSMSMessages = (JSONArray) getObject(inboundSMSMessageList,
										INBOUND_SMS_MESSAGE);
								for (Object inboundSMSMessage : inboundSMSMessages) {
									JSONObject smsObj = (JSONObject) inboundSMSMessage;
									String destinationAddress = getObject(smsObj, DESTINATION_ADDRESS).toString();
									String operatorCode = getObject(smsObj, OPERATOR_CODE).toString();
									String messageId = getObject(smsObj, MESSAGE_ID).toString();
									message = getObject(smsObj, MESSAGE).toString();
									senderAddress = getObject(smsObj, SENDER_ADDRESS).toString();
									outputData = (new Object[] { api, responseTime, serviceTime, serviceProvider,
											apiPublisher, applicationName, operatorId, responseCode, msisdn,direction,
											EVENT_TYPE_RECEIVE_SMS, clientCorrelator, destinationAddress, operatorCode,
											messageId, message, senderAddress, year, month, day, hour });
									addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, outputData);
								}
							}

						}

					}
					//FIXME: This is not currently using for delivery notification as it is not an array
					// Handle Delivery Notification
					else if (function.equals(DELIVERY_NOTIFICATION)) {

						year = (Integer) parameterSet[15];
						month = (Integer) parameterSet[16];
						day = (Integer) parameterSet[17];
						hour = (Integer) parameterSet[18];

						JSONObject deliveryInfos = null;
						// Get fields of the delivery info
						if (direction.equals(SOUTH_BOUND)
								&& checkFieldAvailability(content, DELIVERY_INFO_NOTIFICATION)) {
							JSONObject deliveryInfoNotification = (JSONObject) getObject(content,
									DELIVERY_INFO_NOTIFICATION);
							if (checkFieldAvailability(deliveryInfoNotification, DELIVERY_INFO)) {
								deliveryInfos = (JSONObject) getObject(deliveryInfoNotification, DELIVERY_INFO);
								String address = getObject(deliveryInfos, ADDRESS).toString();
								String operatorCode = getObject(deliveryInfos, OPERATOR_CODE).toString();
								String filterCriteria = getObject(deliveryInfos, FILTER_CRITERIA).toString();
								String deliveryStatus = getObject(deliveryInfos, DELIVERY_STATUS).toString();
								outputData = (new Object[] { api, responseTime, serviceTime, serviceProvider,
										apiPublisher, applicationName, operatorId, responseCode, msisdn,direction,
										EVENT_TYPE_DELIVERY_NOTIFICATION, address, operatorCode, filterCriteria,
										deliveryStatus, year, month, day, hour });
								addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, outputData);
							}
						}
					}
					else if (function.equals(DELIVERY_RECEIPT_SUBSCRIPTION)) {
						year = (Integer) parameterSet[14];
						month = (Integer) parameterSet[15];
						day = (Integer) parameterSet[16];
						hour = (Integer) parameterSet[17];


						JSONObject deliveryReceiptSubscription = (JSONObject) getObject(content, DELIVERY_RECEIPT_SUBSCRIPTION);
						if (checkFieldAvailability(deliveryReceiptSubscription, SENDER_ADDRESSES)) {
							JSONArray senderAddresses = (JSONArray) getObject(deliveryReceiptSubscription, SENDER_ADDRESSES);
							for (Object senderAddressObject : senderAddresses) {
								JSONObject senderAddressJSONObject = (JSONObject) senderAddressObject;
								senderAddress = getObject(senderAddressJSONObject, SENDER_ADDRESS).toString();
								String operatorCode = getObject(senderAddressJSONObject, OPERATOR_CODE).toString();
								String filterCriteria = getObject(senderAddressJSONObject, FILTER_CRITERIA).toString();
								outputData = (new Object[]{api, responseTime, serviceTime, serviceProvider,
								                           apiPublisher, applicationName, operatorId, responseCode, msisdn, direction,
								                           EVENT_TYPE_DELIVERY_NOTIFICATION, senderAddress, operatorCode, filterCriteria,
								                           year, month, day, hour});
								addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, outputData);
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

	/**
	 * Add to complex event chunk
	 * 
	 * @param complexEventPopulater
	 *            complexEventPopulater
	 * @param newComplexEventChunk
	 *            newComplexEventChunk
	 * @param outputData
	 *            outputData
	 */
	private void addToComplexEventChunk(ComplexEventPopulater complexEventPopulater,
			ComplexEventChunk<StreamEvent> newComplexEventChunk, Object[] outputData) {
		StreamEvent newStreamEvent = new StreamEvent(0, 0, outputData.length);
		newStreamEvent.setOutputData(outputData);
		newStreamEvent.setTimestamp(System.currentTimeMillis());
		newComplexEventChunk.add(newStreamEvent);
	}

	/**
	 * Checks the message length and return the number of msgs
	 * 
	 * @param message
	 * @return number of msgs
	 */
	private int checkMessageLength(String message) {
		int numberOfSMSs = 0;
		numberOfSMSs = message.length() > 160 ? (int) Math.ceil(message.length() / 160) : 1;
		return numberOfSMSs;
	}

	/**
	 * Checks the availability of the object
	 * 
	 * @param content
	 *            content
	 * @param key
	 *            key
	 * @return boolean
	 */
	private boolean checkFieldAvailability(JSONObject content, String key) {
		if (content.containsKey((Object) key)) {
			return true;
		}
		return false;
	}

	/**
	 * Get the value
	 * 
	 * @param content
	 *            content
	 * @param key
	 *            key
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
}
