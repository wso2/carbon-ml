package org.wso2.carbon.ml.siddhi.extension.streamingml.samoa;

import org.apache.samoa.moa.cluster.Cluster;
import org.apache.samoa.moa.cluster.Clustering;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by mahesh on 6/4/16.
 */
public class StreamingClustering extends Thread{
    private int learnType;
    private int paramCount = 0;
    private int numAttributes=0;// Number of x variables +1
    private int batchSize = 10;                                 // Maximum # of events, used for regression calculation
    private double ci = 0.95;                                           // Confidence Interval
    private int numClusters=1;
    private int numIterations = 100;
    private double alpha=0;
    private int nt=0;
    private int mt=0;
    private List<String> eventsMem=null;

    private boolean isBuiltModel;
    private MODEL_TYPE type;
    public enum MODEL_TYPE {BATCH_PROCESS, MOVING_WINDOW,TIME_BASED }

    public ConcurrentLinkedQueue<double[]>cepEvents;
    public ConcurrentLinkedQueue<Clustering>samoaClusters;
    public int maxNumEvents=1000000;
    public int numEventsReceived=0;

    public StreamingClusteringTaskBuilder clusteringTask;
    private static final Log logger = LogFactory.getLog(StreamingClustering.class);


    public StreamingClustering(int learnType,int paramCount, int batchSize, double ci, int numClusters,int numIteration, double alpha){
        this.learnType = learnType;
        this.paramCount =paramCount;
        this.numAttributes = paramCount;
        this.batchSize = batchSize;
        this.ci = ci;
        this.numClusters = numClusters;
        this.numIterations = numIteration ;
        this.alpha = alpha;
        this.isBuiltModel = false;
        type= MODEL_TYPE.BATCH_PROCESS;
        // System.out.println("A");
        ///cepEvents = new LinkedList<double[]>();
        //  System.out.println("B");
        // samoaClusters = new LinkedList<Clustering>();
        // System.out.println("C");

        this.cepEvents = new ConcurrentLinkedQueue<double[]>();
        //StreamingClusteringStream.cepEvents = this.cepEvents;
        this.samoaClusters = new  ConcurrentLinkedQueue<Clustering>();
        this.maxNumEvents = 1000000;
        try {

            this.clusteringTask = new StreamingClusteringTaskBuilder(this.numClusters,this.cepEvents, this.samoaClusters, this.maxNumEvents);
        }catch(Exception e){
            System.out.println(e.toString());
        }
        logger.info("Successfully Initiated the Streaming Clustering Topology");

    }

    public void run(){
        this.clusteringTask.initTask(paramCount,numClusters,batchSize,maxNumEvents);
    }

    public Object[] cluster(double[] eventData) {
        // System.out.println("Events Added to the CEP Events");
        numEventsReceived++;
        //logger.info("CEP Event Received : "+numEventsReceived);
        cepEvents.add(eventData);
        Object[] output;
        if(!samoaClusters.isEmpty()){
            logger.info("Micro Clustering Done : Update the Model");
            output = new Object[numClusters +1];
            output[0] = 0.0;
            //System.out.println("++++ We got a hit ++++");
            Clustering clusters = samoaClusters.poll();
            int dim = clusters.dimension();
            logger.info("Number of KMeans Clusters : "+ clusters.size());
            for (int i=0;i<numClusters;i++){
                Cluster cluster= clusters.get(i);
                String centerStr="";
                double [] center=cluster.getCenter();
                centerStr += center[0];
                for(int j=1;j<numAttributes;j++){
                    centerStr += (","+center[j]);
                }
                output[i+1]= centerStr;
                logger.info("Center :"+i+": "+centerStr);
            }
            //for(int i=0;i<dim;i++){
            //  output[i+1] = ""      }

        }else{
            output=null;
        }
        return output;
    }



}
