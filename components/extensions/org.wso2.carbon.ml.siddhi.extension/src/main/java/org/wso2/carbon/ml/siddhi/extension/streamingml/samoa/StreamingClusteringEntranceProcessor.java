package org.wso2.carbon.ml.siddhi.extension.streamingml.samoa;


import org.apache.samoa.core.ContentEvent;
import org.apache.samoa.core.EntranceProcessor;
import org.apache.samoa.core.Processor;
import org.apache.samoa.instances.Instance;
import org.apache.samoa.instances.Instances;
import org.apache.samoa.learners.clusterers.ClusteringContentEvent;
import org.apache.samoa.moa.core.DataPoint;
import org.apache.samoa.moa.options.AbstractOptionHandler;
import org.apache.samoa.streams.InstanceStream;
import org.apache.samoa.streams.StreamSource;
import org.apache.samoa.streams.clustering.ClusteringStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;

/**
 * Created by mahesh on 7/26/16.
 */


public class StreamingClusteringEntranceProcessor implements EntranceProcessor {

    private static final long serialVersionUID = 4169053337917578558L;

    private static final Log logger = LogFactory.getLog(StreamingClusteringEntranceProcessor.class);

    private StreamSource streamSource;
    private Instance firstInstance;
    private boolean isInited = false;
    private Random random = new Random();
    private double samplingThreshold;
    private int numberInstances;
    private int numInstanceSent = 0;

    private int groundTruthSamplingFrequency;

    @Override
    public boolean process(ContentEvent event) {
        // TODO: possible refactor of the super-interface implementation
        // of source processor does not need this method
        return false;
    }

    @Override
    public void onCreate(int id) {
        logger.debug("Creating ClusteringSourceProcessor with id {}"+ id);
    }

    @Override
    public Processor newProcessor(Processor p) {
        StreamingClusteringEntranceProcessor newProcessor = new StreamingClusteringEntranceProcessor();
        StreamingClusteringEntranceProcessor originProcessor = (StreamingClusteringEntranceProcessor) p;
        if (originProcessor.getStreamSource() != null) {
            newProcessor.setStreamSource(originProcessor.getStreamSource().getStream());
        }
        return newProcessor;
    }

    @Override
    public boolean hasNext() {
        return (!isFinished());
    }

    @Override
    public boolean isFinished() {
        return (!streamSource.hasMoreInstances() || (numberInstances >= 0 && numInstanceSent >= numberInstances));
    }

    // /**
    // * Method to send instances via input stream
    // *
    // * @param inputStream
    // * @param numberInstances
    // */
    // public void sendInstances(Stream inputStream, Stream evaluationStream, int
    // numberInstances, double samplingThreshold) {
    // int numInstanceSent = 0;
    // this.samplingThreshold = samplingThreshold;
    // while (streamSource.hasMoreInstances() && numInstanceSent <
    // numberInstances) {
    // numInstanceSent++;
    // DataPoint nextDataPoint = new DataPoint(nextInstance(), numInstanceSent);
    // ClusteringContentEvent contentEvent = new
    // ClusteringContentEvent(numInstanceSent, nextDataPoint);
    // inputStream.put(contentEvent);
    // sendPointsAndGroundTruth(streamSource, evaluationStream, numInstanceSent,
    // nextDataPoint);
    // }
    //
    // sendEndEvaluationInstance(inputStream);
    // }

    public double getSamplingThreshold() {
        return samplingThreshold;
    }

    public void setSamplingThreshold(double samplingThreshold) {
        this.samplingThreshold = samplingThreshold;
    }

    public int getGroundTruthSamplingFrequency() {
        return groundTruthSamplingFrequency;
    }

    public void setGroundTruthSamplingFrequency(int groundTruthSamplingFrequency) {
        this.groundTruthSamplingFrequency = groundTruthSamplingFrequency;
    }

    public StreamSource getStreamSource() {
        return streamSource;
    }

    public void setStreamSource(InstanceStream stream) {
        if (stream instanceof AbstractOptionHandler) {
            ((AbstractOptionHandler) (stream)).prepareForUse();
        }

        this.streamSource = new StreamSource(stream);
        firstInstance = streamSource.nextInstance().getData();
    }

    public Instances getDataset() {
        return firstInstance.dataset();
    }

    private Instance nextInstance() {
        if (this.isInited) {
            return streamSource.nextInstance().getData();
        } else {
            this.isInited = true;
            return firstInstance;
        }
    }

    // private void sendEndEvaluationInstance(Stream inputStream) {
    // ClusteringContentEvent contentEvent = new ClusteringContentEvent(-1,
    // firstInstance);
    // contentEvent.setLast(true);
    // inputStream.put(contentEvent);
    // }

    // private void sendPointsAndGroundTruth(StreamSource sourceStream, Stream
    // evaluationStream, int numInstanceSent, DataPoint nextDataPoint) {
    // boolean sendEvent = false;
    // DataPoint instance = null;
    // Clustering gtClustering = null;
    // int samplingFrequency = ((ClusteringStream)
    // sourceStream.getStream()).getDecayHorizon();
    // if (random.nextDouble() < samplingThreshold) {
    // // Add instance
    // sendEvent = true;
    // instance = nextDataPoint;
    // }
    // if (numInstanceSent > 0 && numInstanceSent % samplingFrequency == 0) {
    // // Add GroundTruth
    // sendEvent = true;
    // gtClustering = ((RandomRBFGeneratorEvents)
    // sourceStream.getStream()).getGeneratingClusters();
    // }
    // if (sendEvent == true) {
    // ClusteringEvaluationContentEvent evalEvent;
    // evalEvent = new ClusteringEvaluationContentEvent(gtClustering, instance,
    // false);
    // evaluationStream.put(evalEvent);
    // }
    // }

    public void setMaxNumInstances(int value) {
        numberInstances = value;
    }

    public int getMaxNumInstances() {
        return this.numberInstances;
    }

    @Override
    public ContentEvent nextEvent() {

        groundTruthSamplingFrequency = ((ClusteringStream) streamSource.getStream()).getDecayHorizon(); // FIXME should it be takend from the ClusteringEvaluation -f option instead?
        if (isFinished()) {
            // send ending event
            ClusteringContentEvent contentEvent = new ClusteringContentEvent(-1, firstInstance);
            contentEvent.setLast(true);
            return contentEvent;
        } else {
            DataPoint nextDataPoint = new DataPoint(nextInstance(), numInstanceSent);
            numInstanceSent++;

            ClusteringContentEvent contentEvent = new ClusteringContentEvent(numInstanceSent, nextDataPoint);

            //logger.info("Number Of Instanced Sent"+numInstanceSent);
            return contentEvent;

        }
    }
}
