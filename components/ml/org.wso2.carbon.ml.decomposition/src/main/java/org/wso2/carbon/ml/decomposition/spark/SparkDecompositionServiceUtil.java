/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.decomposition.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.dto.Feature;
import org.wso2.carbon.ml.database.dto.Workflow;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.decomposition.exceptions.DecompositionException;
import org.wso2.carbon.ml.decomposition.exceptions.SparkConfigException;
import org.wso2.carbon.ml.decomposition.internal.DecompositionConstants;
import org.wso2.carbon.ml.decomposition.internal.SparkProperty;
import org.wso2.carbon.ml.decomposition.internal.SparkSettings;
import org.wso2.carbon.ml.decomposition.internal.ds.DecompositionServiceValueHolder;
import org.wso2.carbon.ml.decomposition.spark.transformations.HeaderRemovingFilter;
import org.wso2.carbon.ml.decomposition.spark.transformations.LineToDataPointMapper;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class contains a set of static helper methods used by
 * Spark implementation of the decomposition service.
 */
public class SparkDecompositionServiceUtil {

    private static final Log log = LogFactory.getLog(SparkDecompositionServiceUtil.class);

    /**
     * Save serialized matrix in hard disk.
     *
     * @param workflowID The workflow ID associated with this dataset
     * @param matrix The matrix which is going to save in the disk
     * @throws DecompositionException
     */
    public static void saveMatrix(String workflowID, Matrix matrix)
            throws DecompositionException {
        FileOutputStream fileOutStream = null;
        ObjectOutputStream matrixOutStream = null;
        try {
            String fullPath = buildPCAMatrixPath(workflowID);

            // if file is already exists, delete it
            File currentMatrix = new File(fullPath);
            if (currentMatrix.exists()) {
                boolean isSuccess = currentMatrix.delete();
                if (!isSuccess) {
                    throw new DecompositionException(
                        "An error occurred while deleting matrix, in workflow : " +workflowID);
                }
            }

            // serialize and save matrix object into disk
            fileOutStream = new FileOutputStream(fullPath);
            matrixOutStream = new ObjectOutputStream(fileOutStream);
            matrixOutStream.writeObject(matrix);

        } catch (IOException ex) {
            throw new DecompositionException(
                "An error occurred while saving a matrix: " + ex.getMessage(), ex);
        } finally {
            closeResource(fileOutStream);
            closeResource(matrixOutStream);
        }
    }

    /**
     * Read matrix object from the hard disk.
     *
     * @param workflowID The workflow ID associated with this dataset
     * @return The matrix retrieve from the disk
     * @throws DecompositionException
     */
    public static Matrix loadMatrix(String workflowID) throws DecompositionException {
        FileInputStream fileInputStream = null;
        ObjectInputStream matrixInputStream = null;

        try {
            String fullPath = buildPCAMatrixPath(workflowID);

            fileInputStream =  new FileInputStream(fullPath);
            matrixInputStream = new ObjectInputStream(fileInputStream);
            return (Matrix)matrixInputStream.readObject();

        } catch (IOException ex) {
            throw new DecompositionException(
                    "An error occurred while reading a matrix: " + ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            throw new DecompositionException(
                    "An error occurred while reading a matrix Object: " + ex.getMessage(), ex);
        } finally {
            closeResource(fileInputStream);
            closeResource(matrixInputStream);
        }
    }

    /**
     * Get a random sample from a given dataset.
     *
     * @param workflowID The workflow ID associated with this dataset
     * @return Random sample drawn from the dataset
     * @throws DecompositionException
     */
    public static JavaRDD<LabeledPoint> getSamplePoints(String workflowID, String response)
                                            throws DecompositionException {

        // Spark looks for various configuration files using it's class loader. Therefore, the
        // class loader needed to be switched temporarily.
        // assign current thread context class loader to a variable
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());

            SparkConf sparkConf = getSparkConf(DecompositionConstants.SPARK_CONFIG_XML);
            sparkConf.setAppName(workflowID);

            DatabaseService dbService = DecompositionServiceValueHolder.getDatabaseService();
            Workflow workflow = dbService.getWorkflow(workflowID);
            String dataSetURL = workflow.getDatasetURL();

            JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
            JavaRDD<String> lines = sparkContext.textFile(dataSetURL);
            String headerRow = lines.take(1).get(0);

            // remove header and create the dataset
            JavaRDD<String> data = lines.filter(new HeaderRemovingFilter(headerRow));

            // create dataset with response variable
            Set<Integer> featureIndices = getFeatureIndices(workflow);
            int responseIndex = getResponseIndex(workflow, response);
            Pattern pattern = Pattern.compile(SparkDecompositionServiceUtil.getColumnSeparator(dataSetURL));
            JavaRDD<LabeledPoint> dataPoints = data.map(new LineToDataPointMapper(pattern,featureIndices,responseIndex));

            // if dataset size is larger than  DecompositionConstants.RANDOM_SAMPLE_SIZE
            // take a random sample of  DecompositionConstants.RANDOM_SAMPLE_SIZE
            long dataSetSize = dataPoints.count();
            if (dataSetSize > DecompositionConstants.RANDOM_SAMPLE_SIZE) {
                double sampleFraction = DecompositionConstants.RANDOM_SAMPLE_SIZE/dataSetSize;
                return dataPoints.sample(false, sampleFraction, DecompositionConstants.RANDOM_SEED);
            }
            return dataPoints;

        } catch (DatabaseHandlerException ex) {
            throw new DecompositionException(
                "An error occurred while reading data from database: "+ex.getMessage(), ex);

        } catch (DecompositionException ex) {
            throw new DecompositionException(
                "An error occurred while reading data from database: "+ex.getMessage(), ex);
        } finally {
            // Switching back to original class loader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    /**
     * Utility method for closing resources.
     *
     * @param resource Represents closeable resources
     * @throws DecompositionException
     */
    private static void closeResource(Closeable resource) throws DecompositionException {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (IOException ex) {
            throw  new DecompositionException(
                    "An error occurred while closing the resource: "+ex.getMessage(), ex);
        }
    }

    /**
     * Infer column separator from the dataset url.
     *
     * @param dataSetURL Path of the dataset
     * @return Column separator of the dataset
     * @throws DecompositionException
     */
    private static String getColumnSeparator(String dataSetURL) throws
            DecompositionException {
        if (dataSetURL.endsWith(DecompositionConstants.CSV)) {
            return ",";
        } else if (dataSetURL.endsWith(DecompositionConstants.TSV)) {
            return "\t";
        } else {
            String fileFormat = dataSetURL.substring(dataSetURL.lastIndexOf('.'));
            throw new DecompositionException("Unsupported file format: "+fileFormat);
        }
    }

    /**
     * Get feature indices of the dataset associated with this dataset.
     *
     * @param workflow The workflow ID associated with this dataset
     * @return Indices (zero based) of features used to build the decomposition model
     */
    private static Set<Integer> getFeatureIndices(Workflow workflow) {
        Set<Integer> indices = new HashSet<Integer>();
        for (Feature feature : workflow.getFeatures()) {
            if (feature.isInclude()) {
                indices.add(feature.getIndex());
            }
        }
        return indices;
    }

    /**
     * Get the index of response variable of the dataset associated
     * with this workflow.
     *
     * @param workflow The workflow ID associated with this dataset
     * @param response Name of the response variable
     * @return Index (zero based) of the response variable
     * @throws DecompositionException
     */
    private static int getResponseIndex(Workflow workflow, String response)
            throws DecompositionException {

        for (Feature feature : workflow.getFeatures()) {
            if (response.equalsIgnoreCase(feature.getName())) {
                return feature.getIndex();
            }
        }
        throw new DecompositionException(
            "Response variable: "+response+" is not available in the dataset");
    }

    /**
     * Build an object from SparkConf file.
     *
     * @param sparkConfigXML
     * @return
     * @throws DecompositionException
     */
    private static SparkConf getSparkConf(String sparkConfigXML) throws
            DecompositionException {
        try {
            SparkSettings sparkSettings = (SparkSettings) parseXML(sparkConfigXML);
            SparkConf sparkConf = new SparkConf();
            for (SparkProperty sparkProperty : sparkSettings.getProperties()) {
                sparkConf.set(sparkProperty.getName(), sparkProperty.getProperty());
            }
            return sparkConf;
        } catch (SparkConfigException e) {
            throw new DecompositionException(
                    "An error occurred while parsing spark configuration: " + e.getMessage(), e);
        }

    }

    /**
     * Parse XML file located at given file path.
     *
     * @param xmlFilePath Absolute path to an xml file
     * @return Returns unmarshalled xml
     * @throws DecompositionException
     */
    private static Object parseXML(String xmlFilePath) throws SparkConfigException {
        try {
            File file = new File(xmlFilePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(SparkSettings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new SparkConfigException("An error occurred while parsing: " + xmlFilePath + ": " +
                    e.getMessage(), e);
        }
    }

    /**
     * Build full path of the matrix associated with this workflowID.
     *
     * @param workflowID The workflow ID associated with this dataset.
     * @return Full path of the PCA matrix
     */
    private static String buildPCAMatrixPath(String workflowID) {
        StringBuilder path = new StringBuilder();
        String separator = System.getProperty(DecompositionConstants.FILE_SEPARATOR);

        path.append(System.getProperty(DecompositionConstants.HOME));
        path.append(separator);
        path.append(DecompositionConstants.ML_PROJECTS);
        path.append(separator);
        path.append(workflowID);
        path.append(separator);
        path.append(workflowID);
        path.append("PCAMatrix.mat");

        return path.toString();
    }
}
