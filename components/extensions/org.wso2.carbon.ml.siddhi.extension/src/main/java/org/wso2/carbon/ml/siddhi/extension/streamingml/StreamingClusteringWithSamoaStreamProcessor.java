package org.wso2.carbon.ml.siddhi.extension.streamingml;

import org.wso2.carbon.ml.siddhi.extension.streamingml.samoa.StreamingClustering;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahesh on 6/4/16.
 */
public class StreamingClusteringWithSamoaStreamProcessor extends StreamProcessor {

    private int learnType=0;
    private int windowShift =0 ;

    private int paramCount = 0;                                         // Number of x variables +1
    private int calcInterval = 1;                                       // The frequency of regression calculation
    private int batchSize = 10;                                 // Maximum # of events, used for regression calculation
    private double ci = 0.95;                                           // Confidence Interval
    private double miniBatchFraction=1;
    private int paramPosition = 0;

    private int numIterations = 100;
    private int numClusters = 1;
    private int alpha = 0;
    private double stepSize = 0.00000001;
    private int featureSize=1;  //P
    private StreamingClustering streamingClusteringWithSamoa = null;

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        paramCount = attributeExpressionLength;
        int PARAM_WIDTH=7;
        // Capture constant inputs
        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {

            paramCount = paramCount - PARAM_WIDTH;
            featureSize=paramCount;//

            paramPosition = PARAM_WIDTH;
            try {
                learnType = ((Integer) attributeExpressionExecutors[0].execute(null));
                windowShift = ((Integer) attributeExpressionExecutors[1].execute(null));
                batchSize = ((Integer) attributeExpressionExecutors[2].execute(null));
                numIterations = ((Integer) attributeExpressionExecutors[3].execute(null));
                numClusters = ((Integer) attributeExpressionExecutors[4].execute(null));
                alpha         = ((Integer) attributeExpressionExecutors[5].execute(null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("learn type, windowShift, batchSize and number of Iterations should be of type int");
            }

            /*try{
                stepSize = ((Double) attributeExpressionExecutors[4].execute(null));
                miniBatchFraction = ((Double) attributeExpressionExecutors[5].execute(null));

            }catch(ClassCastException c){
                throw new ExecutionPlanCreationException("Step Size, Mini Batch Fraction should be in double format");
            }*/

            try {
                ci = ((Double) attributeExpressionExecutors[6].execute(null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Confidence interval should be of type double and a value between 0 and 1");
            }
        }
        //System.out.println("Streaming Clustering  Parameters: "+" "+batchSize+" "+" "+ci+"\n");
        // Pick the appropriate regression calculator

        streamingClusteringWithSamoa = new StreamingClustering(learnType,paramCount, batchSize, ci,numClusters, numIterations,alpha);
        try {
            Thread.sleep(1000);
        }catch(Exception e){

        }
        new Thread(streamingClusteringWithSamoa).start();
        // Add attributes for standard error and all beta values
        String betaVal;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(numClusters+1);
        attributes.add(new Attribute("stderr", Attribute.Type.DOUBLE));

        for (int itr = 0; itr < numClusters; itr++) {
            betaVal = "center" + itr;
            attributes.add(new Attribute(betaVal, Attribute.Type.STRING));
        }

        return attributes;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();

                Object[] inputData = new Object[attributeExpressionLength - paramPosition];
                Double[] eventData = new Double[attributeExpressionLength - paramPosition];
                double [] cepEvent = new double[attributeExpressionLength - paramPosition];
                Double value;
                for (int i = paramPosition; i < attributeExpressionLength; i++) {
                    inputData[i - paramPosition] = attributeExpressionExecutors[i].execute(complexEvent);
                    value=eventData[i - paramPosition] = (Double) attributeExpressionExecutors[i].execute(complexEvent);
                    cepEvent[i - paramPosition] = (double)value;
                }

                //Object[] outputData = regressionCalculator.calculateLinearRegression(inputData);
                Object[] outputData = null;

                // Object[] outputData= streamingLinearRegression.addToRDD(eventData);
                //Calling the regress function
                outputData = streamingClusteringWithSamoa.cluster(cepEvent);

                // Skip processing if user has specified calculation interval
                if (outputData == null) {
                    streamEventChunk.remove();
                } else {
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk);

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
    public void restoreState(Object[] state) {

    }

}
