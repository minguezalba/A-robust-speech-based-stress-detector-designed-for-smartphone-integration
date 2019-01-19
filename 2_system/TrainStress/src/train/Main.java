package train;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.UnsupportedAudioFileException;

import train.DataTools;

public class Main {

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
			//=======================================================================================
			
			
			String setlist[] = {"set1", "set2"};
			
			for(int s=0; s<setlist.length; s++) {
				
				System.out.println("\n************************************");
				System.out.println(  "*  Processing " + setlist[s] + " - SNR " + SNR + "  *");
				System.out.println("************************************");
				
				//=======================================================================================
				// 0. Configuration
				//=======================================================================================
								
				String setname = setlist[s]; // Options: "set1" or "set2"
				String[] setID = DataTools.getSetID(setname);
				
				int fs = 16000;
				
				double win_sec = 20e-3;  //segundos
				double step_sec = 10e-3; //segundos
				int win = (int) (fs*win_sec); //samples
				int step = (int) (fs*step_sec); // samples
				
				int NFFT = 512; //Potencia de 2 mayor que ventana (20 ms = 320 samples)	
						
				//=======================================================================================
				// Process each user ID
				//=======================================================================================
						
				for(int i=0; i<setID.length; i++) {
					
				//=======================================================================================
				// 1. Generate user object and read its files
				//=======================================================================================
					
					System.out.println("\n=============================================================");
					System.out.println("Processing ID_" + setID[i]);
					User user = new User(setID[i], setname, SNR, ss);
					
				
				//=======================================================================================
				// 2. Normalize signals by its maximum
				//=======================================================================================
				
					user.getRecording().setNorSignal(ReadWAV.normalize(user.getRecording().getOriginalSignal()));
					user.getBaseline().setNorSignal(ReadWAV.normalize(user.getBaseline().getOriginalSignal()));
					
				//=======================================================================================
				// 3. Generate VAD
				//=======================================================================================
			
					VAD vadRecording = new VAD(user.getRecording().getNorSignal(), fs, win, step, NFFT);
					user.getRecording().setVadUser(vadRecording);
					
								
					VAD vadBaseline = new VAD(user.getBaseline().getNorSignal(), fs, win, step, NFFT);
					user.getBaseline().setVadUser(vadBaseline);
					
					
				//=======================================================================================
				// 4. Enhance signals
				//=======================================================================================
	
					if(ss) {
						// CON SS -> data/data_model
						user.getRecording().setCleanSignal(SS.denoiseSignal(user.getRecording().getNorSignal(), vadRecording, win, step, NFFT, fs));
						user.getBaseline().setCleanSignal(SS.denoiseSignal(user.getBaseline().getNorSignal(), vadBaseline, win, step, NFFT, fs));	
					}else {				
						//SIN SS -> data/data_model_sin_ss
						user.getRecording().setCleanSignal(user.getRecording().getNorSignal());
						user.getBaseline().setCleanSignal(user.getBaseline().getNorSignal());	
					}
					
					
				//=======================================================================================
				// 5. Remove silence areas in signals
				//=======================================================================================
				
					user.getRecording().setFinalSignal(
							vadRecording.removeSilence(user.getRecording().getCleanSignal()));
				
							
					user.getBaseline().setFinalSignal(
							vadBaseline.removeSilence(user.getBaseline().getCleanSignal()));
					
								
				//=======================================================================================
				// Save variables to check in Python
				//=======================================================================================
					
					Path path = Paths.get(Main.class.getResource(".").toURI());      
					String path_parent = path.getParent().getParent().getParent().toString();
					
					if(generate_ser) {
						
						String dir = "";
						// Defining filenames
						if(ss) {
						 dir = path_parent + "/data/data_ser/" + SNR + "/" + setname + "/";
						}else {
							dir = path_parent + "/data/data_ser_sin_ss/" + SNR + "/" + setname + "/";
						}
						String original = "_original.ser";
						String normalize = "_normalize.ser";
						String denoised = "_denoised.ser";
						String vad = "_vad.ser";
						String finaal = "_final.ser";
						
					
						// Save recording signals
						
						DataTools.generateSERfile(user.getRecording().getOriginalSignal(), dir + user.getID() + user.getRecording().getTipo() + original);
						DataTools.generateSERfile(user.getRecording().getNorSignal(), dir + user.getID() + user.getRecording().getTipo() + normalize);
						DataTools.generateSERfile(user.getRecording().getCleanSignal(), dir + user.getID() + user.getRecording().getTipo() + denoised);
						DataTools.generateSERfile(user.getRecording().getVadUser().getVadSamplesClean(), dir + user.getID() + user.getRecording().getTipo() + vad);
						DataTools.generateSERfile(user.getRecording().getFinalSignal(), dir + user.getID() + user.getRecording().getTipo() + finaal);
						
						/*
						// Save baseline signals
						DataTools.generateSERfile(user.getBaseline().getOriginalSignal(), dir + user.getID() + user.getBaseline().getTipo() + original);
						DataTools.generateSERfile(user.getBaseline().getNorSignal(), dir + user.getID() + user.getBaseline().getTipo() + normalize);
						DataTools.generateSERfile(user.getBaseline().getCleanSignal(), dir + user.getID() + user.getBaseline().getTipo() + denoised);
						DataTools.generateSERfile(user.getBaseline().getVadUser().getVadSamplesClean(), dir + user.getID() + user.getBaseline().getTipo() + vad);
						DataTools.generateSERfile(user.getBaseline().getFinalSignal(), dir + user.getID() + user.getBaseline().getTipo() + finaal);
						*/	
						
					}
					
					//=======================================================================================
					// 6. Generate stress labels and remove silence areas from it
					//=======================================================================================
				
					// Recording and baseline are calculated together because both HR threshold is based on baseline moment
					user.generateLabelsStress(vadRecording, vadBaseline); 
								
					// At this point, we have signal and their corresponding labels with silence areas removed from both
					
					//=======================================================================================
					// 7. Generate features from finalSignal (Normalize, denoise and without silences)
					//=======================================================================================
					
					user.getRecording().setFeatures(Features.generateFeatures(user.getRecording(), fs, win, step, NFFT));
					user.getBaseline().setFeatures(Features.generateFeatures(user.getBaseline(), fs, win, step, NFFT));		
								
					// At this time, labels and features (with resolution 1 per second) should have same length
					
					//=======================================================================================
					// 9. Adjust dimensions between labels and features and vadChunks length
					//=======================================================================================
			
					user.getRecording().finalAdjust();
					user.getBaseline().finalAdjust();
					
					//=======================================================================================
					// 10. Export labels and features to CSV 
					//=======================================================================================
					
					// Labels
					DataTools.exportUniDataToCSV(user.getRecording().getCsvLabelsStress(), user.getRecording().getLabelStress());
					DataTools.exportUniDataToCSV(user.getBaseline().getCsvLabelsStress(), user.getBaseline().getLabelStress());
					
					// Indexes
					DataTools.exportUniDataToCSV(user.getRecording().getCsvIndexes(), vadRecording.getVadChunks());
					DataTools.exportUniDataToCSV(user.getBaseline().getCsvIndexes(), vadBaseline.getVadChunks());
					
					//Features
					DataTools.exportMultiDataToCSV(user.getRecording().getCsvFeatures(), user.getRecording().getFeatures());
					DataTools.exportMultiDataToCSV(user.getBaseline().getCsvFeatures(), user.getBaseline().getFeatures());
					
					System.out.println("Finished ID_" + setID[i]);
					System.out.println("=============================================================");
					
				}// end for id
			
			} // end for setlist
		}
	}

}
