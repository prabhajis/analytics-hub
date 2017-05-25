package org.wso2telco.analytics.hub.report.engine;

import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Map;


import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;

import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.config.ExecutionPlanContext;

import org.wso2telco.analytics.pricing.service.*;

import java.util.HashMap;

public class BillingStreamProcessor extends StreamProcessor {

    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext
                                           executionPlanContext) {


        return new ArrayList<Attribute>();
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        List<ComplexEventChunk<StreamEvent>> complexEventChunkList = new ArrayList<ComplexEventChunk<StreamEvent>>();
        synchronized (this) {
            while (streamEventChunk.hasNext()) {


                // Creates a new complex event chunk
                ComplexEventChunk<StreamEvent> newComplexEventChunk = new ComplexEventChunk<StreamEvent>(true);

                StreamEvent compressedEvent = (StreamEvent) streamEventChunk.next();
                Object[] parameterSet = compressedEvent.getOutputData();




                String direction = parameterSet[4].toString();
                if (direction.equals("sb")) {

                    String api = parameterSet[5].toString();
                    Integer applicationid = Integer.parseInt(parameterSet[8].toString());
                    Integer response_count = (Integer) parameterSet[19];
                    String requestId = parameterSet[2].toString();
                    String operatorId = parameterSet[12].toString();
                    String operatorRef = parameterSet[14].toString();
                    BigDecimal chargeAmount = new BigDecimal(parameterSet[15].toString()) ;
                    Date reqtime = new Date((long) parameterSet[3]);
                    String category = parameterSet[16].toString();
                    String subcategory = parameterSet[17].toString();
                    String merchant = parameterSet[18].toString();
                    String operation = parameterSet[13].toString();
                    String serviceProvider = parameterSet[9].toString();

                    StreamRequestData streamRequestData = new StreamRequestData(
                            api, serviceProvider, applicationid,
                            response_count, requestId, operatorId,
                            operatorRef, chargeAmount, reqtime,
                            category, subcategory, merchant, operation);

                    int count = (int) parameterSet[0];

//                    Map<CategoryCharge, BilledCharge> apiCount = new HashMap<CategoryCharge, BilledCharge>();
//
//                    BilledCharge billcharge = new BilledCharge(count);
//                    CategoryCharge categoryCharge = new CategoryCharge(200, category, subcategory);
//                    apiCount.put(categoryCharge, billcharge);



//                    StreamRequestData reqdata = null;
//                    //Sample stream data
//                    reqdata = new StreamRequestData("smsmessaging", "admin", 1, 1, "1448128113683PA8602", "1", "DLG2-1448128113683",
//                            new BigDecimal(200), new java.sql.Date(2017, 5, 19), "349", "651", "25", 201);



                    BigDecimal hbCommision = new BigDecimal(parameterSet[23].toString());
                    BigDecimal opCommision = new BigDecimal(parameterSet[21].toString());
                    BigDecimal spCommision = new BigDecimal(parameterSet[22].toString());


                    BilledCharge billcharge = new BilledCharge(0);
                    billcharge.setAdscom(hbCommision);
                    billcharge.setOpcom(opCommision);
                    billcharge.setSpcom(spCommision);
                    billcharge.setCount(count);
                    billcharge.addTax(BigDecimal.TEN);//todo

                    Map<CategoryCharge, BilledCharge> categoryEntry = new HashMap<CategoryCharge, BilledCharge>();
                    CategoryCharge categoryCharge = null;
                    categoryCharge = new CategoryCharge(operation , category, subcategory);
                    categoryEntry.put(categoryCharge, billcharge);

                    PriceServiceImpl instance = new PriceServiceImpl();
                    instance.priceNorthBoundRequest(streamRequestData, categoryEntry.entrySet().iterator().next());
                    //temp code

                    parameterSet[0] = count + 1;

                    double total = (Double) parameterSet[1];
                    parameterSet[1] = total + 150;

                    double totalOpCommision = (Double) parameterSet[30];
                    parameterSet[30] = totalOpCommision + 60;
                    double totalSpCommision = (Double) parameterSet[31];
                    parameterSet[31] = totalSpCommision + 10;
                    double totalHbCommision = (Double) parameterSet[32];
                    parameterSet[32] = totalHbCommision + 30;


                    //end of temp code

                    parameterSet[20] = "RC-112015";//rate card
                    parameterSet[21] = streamRequestData.getOpcom();  //60;//opCommision
                    parameterSet[22] = streamRequestData.getSpcom(); //10;//spCommision
                    parameterSet[23] = streamRequestData.getAdscom(); //30;//hbCommision
                    //parameterSet[24] = 15.5;//tax
                    parameterSet[25] = streamRequestData.getPrice(); //250;//price

                }




                addToComplexEventChunk(complexEventPopulater, newComplexEventChunk, parameterSet);
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

    public void start() {
    }

    public void stop() {
    }

    public Object[] currentState() {
        return new Object[0];
    }

    public void restoreState(Object[] state) {
    }

}
