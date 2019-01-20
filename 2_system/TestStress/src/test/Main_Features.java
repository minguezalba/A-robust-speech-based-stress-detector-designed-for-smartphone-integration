package test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.UnsupportedAudioFileException;

import test.SS;

public class Main_Features {

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, URISyntaxException {

		String SNRlist[] = {"original", "20", "15", "10", "5", "0", "_5"};
	    boolean ss = false;
	    
	    boolean generate_ser = false;
		
		for(int k=0; k<SNRlist.length; k++) {
			//=======================================================================================
			// CHOOSE ONE OF THE POSSIBLE SNR VALUES: 
			// Options: "original", "20", "15", "10", "5", "0", "_5"
			//
			      String SNR = SNRlist[k]; 

			//
			// Options: 1-10 (included)
			//
			      
			for(int n=1; n<=10; n++) {
				
			     String numTest = String.valueOf(n); 
			
			
				//=======================================================================================
				// 0. Configuration
				//=======================================================================================
								
				
				int fs = 16000;
				
				double win_sec = 20e-3;  //segundos
				double step_sec = 10e-3; //segundos
				int win = (int) (fs*win_sec); //samples
				int step = (int) (fs*step_sec); // samples
				
				int NFFT = 512; //Potencia de 2 mayor que ventana (20 ms = 320 samples)	
			
					
				//=======================================================================================
				// 1. Generate user object and read its files
				//=======================================================================================
					
					System.out.println("\n************************************");
					System.out.println(  "*  Processing test" + numTest + " - SNR " + SNR + "  *");
					System.out.println("************************************");
				
					User user = new User(numTest, SNR, ss);
										
				//=======================================================================================
				// 2. Normalize signals by its maximum
				//=======================================================================================
				
					user.getUserfile().setNorSignal(ReadWAV.normalize(user.getUserfile().getOriginalSignal()));
								
				//=======================================================================================
				// 3. Generate VAD
				//=======================================================================================
			
					VAD vadUser = new VAD(user.getUserfile().getNorSignal(), fs, win, step, NFFT);
					user.getUserfile().setVadUser(vadUser);
			
					
				//=======================================================================================
				// 4. Enhance signals
				//=======================================================================================
				
					if(ss) {
						// CON SS -> data/data_model
						user.getUserfile().setCleanSignal(SS.denoiseSignal(user.getUserfile().getNorSignal(), vadUser, win, step, NFFT, fs));
					}else {				
						//SIN SS -> data/data_model_sin_ss
						user.getUserfile().setCleanSignal(user.getUserfile().getNorSignal());
					}
					
				//=======================================================================================
				// 5. Remove silence areas in signals
				//=======================================================================================
				
					user.getUserfile().setFinalSignal(
							vadUser.removeSilence(user.getUserfile().getCleanSignal()));
					
								
				//=======================================================================================
				// Save variables to check in Python
				//=======================================================================================
					
					Path path = Paths.get(Main_Features.class.getResource(".").toURI());      
					String path_parent = path.getParent().getParent().getParent().toString();
					
					if(generate_ser) {
						
						// Defining filenames
						String dir = "";
						// Defining filenames
						if(ss) {
							dir = path_parent + "/data/data_ser/" + SNR + "/test/test" + numTest;
						}else {
							dir = path_parent + "/data/data_ser_sin_ss/" + SNR + "/test/test" + numTest;
						}
						
						String original = "_original.ser";
						String normalize = "_normalize.ser";
						String denoised = "_denoised.ser";
						String vad = "_vad.ser";
						String finaal = "_final.ser";
						
					
						// Save recording signals
						DataTools.generateSERfile(user.getUserfile().getOriginalSignal(), dir + "test" + numTest + original);
						DataTools.generateSERfile(user.getUserfile().getNorSignal(), dir + "test" + numTest + normalize);
						DataTools.generateSERfile(user.getUserfile().getCleanSignal(), dir + "test" + numTest + denoised);
						DataTools.generateSERfile(user.getUserfile().getVadUser().getVadSamplesClean(), dir + "test" + numTest + vad);
						DataTools.generateSERfile(user.getUserfile().getFinalSignal(), dir + "test" + numTest + finaal);
						 
					}
					
					if(user.getUserfile().getFinalSignal().length != 0) {
						
						//=======================================================================================
						// 6. Generate features from finalSignal (Normalize, denoise and without silences)
						//=======================================================================================
						
						user.getUserfile().setFeatures(Features.generateFeatures(user.getUserfile(), fs, win, step, NFFT));
												
						// At this time, labels and features (with resolution 1 per second) should have same length
						
						//=======================================================================================
						// 7. Export labels and features to CSV 
						//=======================================================================================
			
						//Features
						DataTools.exportMultiDataToCSV(user.getUserfile().getCsvFeatures(), user.getUserfile().getFeatures());			
						System.out.println("Finished test/" + numTest);
						System.out.println("=============================================================");
						
						
					}else {
						System.out.println("Silence test");
					}
			}
		}
	}// end main

}
