package test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.UnsupportedAudioFileException;

import MathTools.Maths;

public class Main_Test {
	
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, URISyntaxException {
		
		
		String[] list_SNR = {"original", "20", "15", "10", "5", "0", "_5"};

		String[] list_timestamp_con_SS = {"20190119_201325_",
				                         "20190119_212719_",
				                         "20190119_215209_",
				                         "20190119_221312_",
				                         "20190119_224030_",
				                         "20190119_231955_",
				                         "20190119_235745_"};

		String[] list_timestamp_sin_SS = {"20190120_104626_",
				                         "20190120_112635_",
				                         "20190120_120857_",
				                         "20190120_124941_",
				                         "20190120_133723_",
				                         "20190120_142235_",
				                         "20190120_150538_"};
		
		
		 boolean ss = false;
		 
		 
		 for(int s=0; s<list_SNR.length; s++) {
		      
			 String SNR = list_SNR[s]; 
		     
			 String timestamp = "";
			 
			 if(ss) {
				 timestamp = list_timestamp_con_SS[s];
			 }else {
				 timestamp = list_timestamp_sin_SS[s];
			 }
		     		      	      
   
			for(int n=1; n<=10; n++) {
				
				String numTest = String.valueOf(n); 
				
			    //=======================================================================================
				// 1. Generate user object and read its files
				//=======================================================================================
					
				System.out.println("\n************************************");
				System.out.println(  "*  Processing test" + numTest + " - SNR " + SNR + "  *");
				System.out.println("************************************");
					User user = new User(numTest, SNR, ss);
		
				//=======================================================================================
				// 8. Normalization of features with training set 
				//=======================================================================================
			
				// importMultiCSVToArray returns an 2d array nfeatures x nframes (42 x nframes)
				double features[][] = DataTools.importMultiCSVToArray(user.getUserfile().getCsvFeatures(), 0);
				
				if(features!=null) {
										
				    Path path = Paths.get(Main_Test.class.getResource(".").toURI());      
					String path_parent = path.getParent().getParent().getParent().toString();
						
					// Defining filenames
					String dir_parameters = "";
					// Defining filenames
					if(ss) {
						dir_parameters = path_parent + "/model/parameters/" + SNR + "/" + timestamp;
					}else {
						dir_parameters = path_parent + "/model/parameters_sin_ss/" + SNR + "/" + timestamp;
					}
									
					double meanTrain[] = DataTools.importUniCSVToArray(dir_parameters + "mean.csv", 0);
					double stdTrain[] = DataTools.importUniCSVToArray(dir_parameters + "std.csv", 0);
					
					
					// Each row is a sample. Columns are features
					double xtest[][] = Maths.trasposeMatrix(Maths.normalizeMatrix(features, meanTrain, stdTrain));
									
			
					//=======================================================================================
					// 9. Classification 
					//=======================================================================================
					
			        // Parameters:
			        String modelData = dir_parameters + "model.json";
			
			        // Estimators:
			        MLPClassifier clf = new MLPClassifier(modelData);
			        
			        int ytest_hat[] = new int[xtest.length];
			
			        for(int i=0; i<xtest.length; i++) {
			        	// Prediction:
			        	ytest_hat[i] = clf.predict(xtest[i]);
			            System.out.println(ytest_hat[i]);
			        }
			        
			        // Estimation labels
					DataTools.exportUniDataToCSV(user.getUserfile().getCsvLabelsStress(), ytest_hat);
					
				}
		        					
			}
		 }
	}

}
