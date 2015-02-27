package org.wso2.carbon.ml.core.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.ml.commons.domain.SamplePoints;
import org.wso2.carbon.ml.core.exceptions.MLInputAdapterException;
import org.wso2.carbon.ml.core.exceptions.MLMalformedDatasetException;
import org.wso2.carbon.ml.core.impl.FileInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;

public class MLUtilsTest {
  @BeforeClass
  public void beforeClass() {
  }


  @Test
  public void getSamplePoints() throws URISyntaxException, MLInputAdapterException {
      MLInputAdapter inputAdapter = new FileInputAdapter();
      
      /*
       * Dataset 
       * Columns: 55
       * Rows: 1000
       */
      String uriString = "src/test/resources/fcSample.csv";
      URI uri ;
      InputStream in = null;
      uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
      in = inputAdapter.readDataset(uri);
      SamplePoints samplePoints;
      try {
          
          // extract limited set of points
         int size = 10000;
         samplePoints = MLUtils.getSamplePoints(in, "csv", size);
        List<List<String>> columnData = samplePoints.getSamplePoints();
        Assert.assertEquals(55, samplePoints.getSamplePoints().size());
        int totalPoints = columnData.get(0).size();
        Assert.assertEquals(size/55, totalPoints);
        
        // extract all points
        in = inputAdapter.readDataset(uri);
        samplePoints = MLUtils.getSamplePoints(in, "csv", -1);
        columnData = samplePoints.getSamplePoints();
         totalPoints = columnData.get(0).size();
        Assert.assertEquals(1000, totalPoints);
        
    } catch (MLMalformedDatasetException e) {
        Assert.assertNull(e);
    }
      
      // malformed document
      uriString = "src/test/resources/fcMalformedSample.csv";
      uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
      in = inputAdapter.readDataset(uri);
      try {
        samplePoints = MLUtils.getSamplePoints(in, "csv", 10);
    } catch (Exception e) {
        Assert.assertNotNull(e);
        Assert.assertEquals(true, e instanceof MLMalformedDatasetException);
    }
      
      // null input stream
      in = null;
      try {
          samplePoints = MLUtils.getSamplePoints(in, "csv", 10);
      } catch (Exception e) {
          Assert.assertNotNull(e);
          Assert.assertEquals(true, e instanceof MLMalformedDatasetException);
      }
      
      // test TSV file
      uriString = "src/test/resources/fcSample.tsv";
      uri = new URI(System.getProperty("user.dir") + File.separator + uriString);
      in = inputAdapter.readDataset(uri);
      try {
        samplePoints = MLUtils.getSamplePoints(in, "tsv", 550);
        List<List<String>> columnData = samplePoints.getSamplePoints();
        Assert.assertEquals(55, samplePoints.getSamplePoints().size());
        int totalPoints = columnData.get(0).size();
        Assert.assertEquals(550/55, totalPoints);
    } catch (Exception e) {
        Assert.assertNull(e);
    }
  }
}
