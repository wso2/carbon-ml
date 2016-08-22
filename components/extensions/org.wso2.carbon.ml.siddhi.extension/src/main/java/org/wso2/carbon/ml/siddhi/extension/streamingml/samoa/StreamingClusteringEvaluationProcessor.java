package org.wso2.carbon.ml.siddhi.extension.streamingml.samoa;

import org.apache.samoa.core.ContentEvent;
import org.apache.samoa.core.Processor;
import org.apache.samoa.evaluation.ClusteringEvaluationContentEvent;
import org.apache.samoa.evaluation.ClusteringResultContentEvent;
import org.apache.samoa.instances.Instance;
import org.apache.samoa.learners.clusterers.ClusteringContentEvent;
import org.apache.samoa.moa.cluster.Clustering;
import org.apache.samoa.moa.clusterers.clustream.WithKmeans;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by mahesh on 7/19/16.
 */
public class StreamingClusteringEvaluationProcessor implements Processor {
    private static final long serialVersionUID = -6043613438148776446L;
    private int processorId;
    private static final Log logger = LogFactory.getLog(StreamingClusteringEvaluationProcessor.class);

    String evalPoint;
    public ConcurrentLinkedQueue<Clustering>samoaClusters;
    public int numClusters=0;

    Clustering gtClustering;
    StreamingClusteringEvaluationProcessor(String evalPoint){
        this.evalPoint = evalPoint;
    }
    @Override
    public boolean process(ContentEvent event) {

       // logger.info("Process");
        if (event instanceof ClusteringContentEvent) {
            logger.info(event.getKey()+""+evalPoint+"ClusteringContentEvent");
            ClusteringContentEvent e= (ClusteringContentEvent)event;
            Instance inst = e.getInstance();

            int numAttributes=inst.numAttributes();

        }

        else if(event instanceof ClusteringResultContentEvent){
            logger.info(event.getKey()+" "+evalPoint+" ClusteringResultContentEvent "+numClusters);
            ClusteringResultContentEvent resultEvent = (ClusteringResultContentEvent)event;

            // Clustering clustering = KMeans.gaussianMeans(gtClustering, resultEvent.getClustering());
            Clustering clustering=resultEvent.getClustering();

            Clustering kmeansClustering = WithKmeans.kMeans_rand(numClusters,clustering);
            logger.info("Kmean Clusters: "+kmeansClustering.size()+" with dimention of : "+kmeansClustering.dimension());
            //Adding samoa Clusters into my class
            samoaClusters.add(kmeansClustering);

            int numClusters = clustering.size();
            logger.info("Number of Kernal Clusters : "+numClusters+" Number of KMeans Clusters :"+kmeansClustering.size());

        }

        else if(event instanceof ClusteringEvaluationContentEvent){
            logger.info(event.getKey()+""+evalPoint+"ClusteringEvaluationContentEvent\n");
        }
        else{
            logger.info(event.getKey()+""+evalPoint+"ContentEvent\n");
        }

        return true;
    }

    @Override
    public void onCreate(int id) {
        this.processorId = id;
    }

    @Override
    public Processor newProcessor(Processor p) {
        StreamingClusteringEvaluationProcessor newEval = (StreamingClusteringEvaluationProcessor)p;
        return newEval;
    }

    public void setSamoaClusters(ConcurrentLinkedQueue<Clustering> samoaClusters){
        this.samoaClusters = samoaClusters;
    }

    public void setNumClusters(int numClusters){
        this.numClusters = numClusters;
    }

}
