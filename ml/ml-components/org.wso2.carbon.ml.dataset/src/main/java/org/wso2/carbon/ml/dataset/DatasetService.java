package org.wso2.carbon.ml.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class DatasetService {
	HashMap<String,String> currentJobs = new HashMap<String,String> ();
	List <List <Double>> columns = new ArrayList <List<Double>>();	//Holds actual data column-wise
	List <DescriptiveStatistics> summaries = new ArrayList <DescriptiveStatistics>();	//holds descriptive statistics of each column
	List <Integer> missing = new ArrayList <Integer>();
	List <Integer> unique = new ArrayList <Integer>();
	List <HashMap<Double,Integer>> graphFrequencies = new ArrayList <HashMap<Double,Integer>>();
	String [] header;
	
	/*
	 * get a summary of a sample from the given csv file, including descriptive-stats, missing points, 
	 * unique values and etc. to display in the data view.
	 */
	public Object getSummary(String dataSource,int noOfRecords) throws IOException{
		Logger logger = Logger.getLogger(DatasetService.class);
		Configuration configuration = new Configuration();
		FileSystem fileSystem = FileSystem.get(configuration);
		configuration.addResource(new Path(fileSystem.getWorkingDirectory() + "/repository/conf/advanced/hive-site.xml"));
		fileSystem.setConf(configuration);

		logger.info("Data Source: "+dataSource);
		logger.info("Sample size: "+noOfRecords);
		
		//read the csv file
		FSDataInputStream dataStream = fileSystem.open(new Path(dataSource));
		BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataStream));

		//read headers
		header = dataReader.readLine().split(",");

		//initialize the Lists
		for (String element : header) {
			summaries.add(new DescriptiveStatistics());
			columns.add(new ArrayList <Double>());
			missing.add(0);
			unique.add(0);
		}

		String[] data;
		double cellValue;

		//iterate through each row
		for(int row=0 ; row<noOfRecords ; row++){
			data = dataReader.readLine().split(",");
			//iterate through each column in a row
			for (int col = 0; col < header.length; col++) {
				//if the cell is not empty
				if(!data[col].isEmpty()){

					//convert the cell value to double
					cellValue= Double.parseDouble(data[col]);

					//append the value of the cell to the descriptive-stats of the respective column
					summaries.get(col).addValue(cellValue);

					//update the unique value count of the column
					if(!columns.get(col).contains(cellValue)){
						unique.set(col, unique.get(col).intValue()+1);
					}

					//append the cell value to the respective column
					columns.get(col).add(cellValue);
				}else{
					missing.set(col, missing.get(col).intValue()+1);
					continue;
				}
			}
		}
		calculateFrequencies(noOfRecords);

		// for testing purposes
		System.out.println("Variable\t|\t\tMean\t\t|\t\tUnique\t\t|\t\tfreqiencies\n--------------------------------------------------------------------------------------------------------");
		for(int column=0 ; column<header.length ; column++){
			System.out.println(header[column]+":\t"+summaries.get(column).getMean()+"\t\t\t"+unique.get(column)+"\t\t\t"+graphFrequencies.get(column));
		}

		for (int i=0 ; i<20 ; i++){
			System.out.println(graphFrequencies.get(1).get((double)i));
		}
		
		return null;
	}

	/*
	 * calculate the frequencies of each bin (i.e. each category/interval), needed to plot bar graphs/pie charts/histograms
	 */
	private void calculateFrequencies(int noOfRecords){
		for(int col = 0; col < header.length; col++){
			//if the column has qualitative (i.e. categorical) data (i.e. unique values are less than or equal to twenty)
			if(unique.get(col).intValue()<=20){
				HashMap<Double,Integer> frequencies= new HashMap<Double,Integer>();
				//count the frequencies in each category
				for(int row=0 ; row<noOfRecords ; row++){
					//if the category has appeared before, increment the frequency
					if(frequencies.containsKey(columns.get(col).get(row))) {
						frequencies.put(columns.get(col).get(row), frequencies.get(columns.get(col).get(row))+1);
					} else{
						//if the category appeared for the first time, set the frequency to one
						frequencies.put(columns.get(col).get(row),1);
					}
				}
				graphFrequencies.add(frequencies);
			}else{	//if the data are quantitative
				int intervals=20;
				//if the column has Quantitative data (i.e. unique values are more than twenty)
				HashMap<Double,Integer> frequencies= new HashMap<Double,Integer>();

				for(int i=0 ; i<intervals ; i++){
					frequencies.put((double) i,0);
				}

				//define the size of an interval
				double intervalSize= (summaries.get(col).getMax()-summaries.get(col).getMin())/intervals;
				double lowerBound;

				for(int row=0 ; row<noOfRecords ; row++){
					//set the initial lower bound to the data-minimum
					lowerBound= summaries.get(col).getMin();
					//check to which interval does the data point belongs
					for(int interval=0 ; interval<intervals ; interval++){
						//if found
						if(lowerBound<=columns.get(col).get(row) && columns.get(col).get(row)<lowerBound+intervalSize){
							//increase the frequency of that interval by one
							frequencies.put((double) interval,frequencies.get((double) interval)+1);
							break;
						}
						//set the lower bound to the lower bound of the next interval
						lowerBound=lowerBound+intervalSize;
					}
				}
				graphFrequencies.add(frequencies);
			}
		}
	}
	
	
	
	public void importData(String source, String destination){
		//TODO
	}
	
	public List<Object> getSamplePoints(String feature1, String feature2, int maxNoOfPoints, String SelectionPolicy){
		//TODO
		return null;
	}
	
	public List<Object> getSampleDistribution(String feature, int noOfBins){
		//TODO
		return null;		
	}
}
