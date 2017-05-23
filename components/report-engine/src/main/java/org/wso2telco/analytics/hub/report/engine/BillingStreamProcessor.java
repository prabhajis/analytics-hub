package org.wso2telco.analytics.hub.report.engine;

import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;

import java.util.List;
import java.util.ArrayList;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;

import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
// import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.event.ComplexEventChunk;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
// import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

// import net.minidev.json.JSONArray;
// import net.minidev.json.JSONObject;
// import net.minidev.json.parser.JSONParser;
// import net.minidev.json.parser.ParseException;


public class BillingStreamProcessor extends StreamProcessor {

    // private int paramCount = 0;                                         // Number of x variables +1
    // private int calcInterval = 1;                                       // The frequency of regression calculation
    // private int batchSize = 1000000000;                                 // Maximum # of events, used for
    // regression calculation
    // private double ci = 0.95;                                           // Confidence Interval
    // private final int SIMPLE_LINREG_INPUT_PARAM_COUNT = 2;              // Number of Input parameters in a simple
    // linear regression
    // // private RegressionCalculator regressionCalculator = null;
    // private int paramPosition = 0;

    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext
                                           executionPlanContext) {

        // if (attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor) {
        //     jsonConstantExecutorForFunction = (ConstantExpressionExecutor) attributeExpressionExecutors[1];
        // } else {
        //     throw new ExecutionPlanValidationException(
        //             "This should have a constant parameter as the 2nd attribute but doesn't found a constant "
        //                     + attributeExpressionExecutors[1].getClass().getCanonicalName());
        // }

        // if (attributeExpressionExecutors[0] instanceof VariableExpressionExecutor) {
        //     jsonVariableExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[0];
        // } else {
        //     throw new ExecutionPlanValidationException(
        //             "This should have a variable expression as the 1st attribute but doesn't found a variable
        // expression "
        //                     + attributeExpressionExecutors[0].getClass().getCanonicalName());
        // }

        return new ArrayList<Attribute>();
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        log.error("IN.........................");
        List<ComplexEventChunk<StreamEvent>> complexEventChunkList = new ArrayList<ComplexEventChunk<StreamEvent>>();
        synchronized (this) {
            while (streamEventChunk.hasNext()) {


                // Creates a new complex event chunk
                ComplexEventChunk<StreamEvent> newComplexEventChunk = new ComplexEventChunk<StreamEvent>(true);

                StreamEvent compressedEvent = (StreamEvent) streamEventChunk.next();
                Object[] parameterSet = compressedEvent.getOutputData();


                parameterSet[5] = "nuwan";

                if (parameterSet[1] == null) {
                    parameterSet[1] = 0.0;
                }

                if (parameterSet[2] == null) {
                    parameterSet[2] = 0.0;
                }

                double count = (Double) parameterSet[1];
                parameterSet[1] = count + 1;

                //     Object[] inputData = new Object[attributeExpressionLength-paramPosition];
                //     for (int i = paramPosition; i < attributeExpressionLength; i++) {
                //         inputData[i-paramPosition] = attributeExpressionExecutors[i].execute(complexEvent);
                //     }
                //     Object[] outputData = regressionCalculator.calculateLinearRegression(inputData);

                //     // Skip processing if user has specified calculation interval
                //     if (outputData == null) {
                //         streamEventChunk.remove();
                //     } else {
                //         complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                //     }
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