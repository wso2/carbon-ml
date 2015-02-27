package org.wso2.carbon.ml.core.impl;

import java.util.Properties;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;

public class MLIOFactoryTest {
    private MLIOFactory ioFactory;
  @BeforeClass
  public void beforeClass() {
      Properties configuration = new Properties();
      configuration.put("data.file.input", "org.wso2.carbon.ml.core.impl.FileInputAdapter");
      configuration.put("data.file.output", "org.wso2.carbon.ml.core.impl.FileOutputAdapter");
      configuration.put("target.home", "/tmp");
      ioFactory = new MLIOFactory(configuration);
  }

  @Test
  public void getInputAdapter() {
    MLInputAdapter inputAdapter = ioFactory.getInputAdapter("file.input");
    Assert.assertEquals(true, inputAdapter instanceof FileInputAdapter);
    
    // default input adapter is file input adapter
     inputAdapter = ioFactory.getInputAdapter("hdfs");
     Assert.assertEquals(true, inputAdapter instanceof FileInputAdapter);
     
  }

  @Test
  public void getOutputAdapter() {
      MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter("file.output");
      Assert.assertEquals(true, outputAdapter instanceof FileOutputAdapter);
      
   // default output adapter is file output adapter
      outputAdapter = ioFactory.getOutputAdapter("hdfs");
      Assert.assertEquals(true, outputAdapter instanceof FileOutputAdapter);
  }

  @Test
  public void getTargetPath() {
      String path = ioFactory.getTargetPath("test-ml.csv");
      Assert.assertEquals("/tmp/test-ml.csv", path);
  }
}
