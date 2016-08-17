package org.wso2.carbon.ml.siddhi.extension.streamingml.samoa;

import com.github.javacliparser.IntOption;
import org.apache.samoa.instances.*;
import org.apache.samoa.moa.core.DataPoint;
import org.apache.samoa.moa.core.Example;
import org.apache.samoa.moa.core.InstanceExample;
import org.apache.samoa.moa.core.ObjectRepository;
import org.apache.samoa.moa.tasks.TaskMonitor;
import org.apache.samoa.streams.InstanceStream;
import org.apache.samoa.streams.clustering.ClusteringStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by mahesh on 7/17/16.
 */
public class StreamingClusteringStream  extends ClusteringStream {
    private static final Log logger = LogFactory.getLog(StreamingClusteringStream.class);
    protected InstancesHeader streamHeader;
    private int numGeneratedInstances;
    private int nextEventCounter;
    LinkedList<DataPoint> points = new LinkedList<DataPoint>();

    //LinkedList<double[]>cepEvents;
    public ConcurrentLinkedQueue<double[]> cepEvents;
    //public ConcurrentLinkedQueue<Clustering>samoaClusters;

    double [] values; //Cep Event
    private int numAttributes;

    public IntOption numClusterOption = new IntOption("numCluster", 'K',
            "The average number of centroids in the model.", 5, 1, Integer.MAX_VALUE);

    @Override
    protected void prepareForUseImpl(TaskMonitor taskMonitor, ObjectRepository objectRepository) {
        taskMonitor.setCurrentActivity("Preparing random RBF...", -1.0);
        this.numAttributes =this.numAttsOption.getValue();
        logger.info("Number of Attributes in the Stream : "+this.numAttributes);
        generateHeader();
        restart();
        //logger.info("Succefully Prepare MyClusteringStream for Implementation");
        values = new double[numAttributes];

        for(int i=0;i<numAttributes;i++){
            values[i]=0;
        }
    }

    @Override
    public InstancesHeader getHeader() {
        return streamHeader;
        //return null;
    }

    @Override
    public long estimatedRemainingInstances() {
        return -1;
    }

    @Override
    public boolean hasMoreInstances() {
        return true;
    }

    @Override
    public Example<Instance> nextInstance() {

        if(numGeneratedInstances == 0){
            logger.info("Sending First Samoa Instance.....");
            numGeneratedInstances++;
            //double[] values = this.values;
            double[] values_new = new double[numAttsOption.getValue()]; // +1
            int clusterChoice = -1;
            while(cepEvents == null);
            while (cepEvents.isEmpty()) ;
            double[] values = cepEvents.poll();
            System.arraycopy(values, 0, values_new, 0, values.length);
            Instance inst = new DenseInstance(1.0, values_new);
            inst.setDataset(getHeader());
            return new InstanceExample(inst);

        }else {
            numGeneratedInstances++;
           // logger.info("Sending Samoa Instance :"+numGeneratedInstances);
            double[] values_new = new double[numAttsOption.getValue()]; // +1
            //logger.info("I am here");

            //while(cepEvents == null);
            while (cepEvents.isEmpty()) ;
            //logger.info("Cep Events Not Empty");
            double[] values = cepEvents.poll();
            int clusterChoice = -1;
            System.arraycopy(values, 0, values_new, 0, values.length);
            Instance inst = new DenseInstance(1.0, values_new);
            inst.setDataset(getHeader());
            return new InstanceExample(inst);
        }
    }

    @Override
    public boolean isRestartable() {
        return true;
    }

    @Override
    public void restart() {
        numGeneratedInstances = 0;
    }

    @Override
    public void getDescription(StringBuilder stringBuilder, int i) {

    }

    private void addInstance(Instance instance) {
        DataPoint point = new DataPoint(instance, numGeneratedInstances);
        points.add(point);

    }

    protected void generateHeader() { // 2013/06/02: Noise label
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < this.numAttsOption.getValue(); i++) {
            attributes.add(new Attribute("att" + (i + 1)));
        }

        ArrayList<String> classLabels = new ArrayList<String>();
        for (int i = 0; i < this.numClusterOption.getValue(); i++) {
            classLabels.add("class" + (i + 1));
        }

        attributes.add(new Attribute("class", classLabels));
        streamHeader = new InstancesHeader(new Instances(getCLICreationString(InstanceStream.class), attributes, 0));
        streamHeader.setClassIndex(streamHeader.numAttributes() - 1);
    }


    public void setCepEvents(ConcurrentLinkedQueue<double[]>cepEvents){
        this.cepEvents = cepEvents;
    }
}
