package test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.UnsupportedAudioFileException;

public class UserFile {
	
	
	//=======================================================================================
	// User files to load/import
	//=======================================================================================
	
	private String wavFile; 
	
	//=======================================================================================
	// User files to generate/export
	//=======================================================================================

	private String csvLabelsStress; 
	private String csvLabelsSpeaker; 
	private String csvFeatures; 
		
	//=======================================================================================
	// WAV File variables
	//=======================================================================================

	private double[] originalSignal; // Original signal from wav file
	private double[] norSignal; // Original signal normalize by its maximum
	private double[] cleanSignal; // Enhanced and normalize signal
	private double[] finalSignal; // Enhanced signal with VAD apply
	
	//=======================================================================================
	// VAD variables
	//=======================================================================================

	private VAD vadUser; 
	
	//=======================================================================================
	// Features variables
	//=======================================================================================

	private double[][] features;
	
	/**
	 * Constructor
	 * @param ID
	 * @param setname
	 * @param tipo
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * @throws URISyntaxException 
	 */

	public UserFile(String numTest, String SNR, boolean ss) throws UnsupportedAudioFileException, IOException, URISyntaxException {
		
		Path path = Paths.get(Main_Features.class.getResource(".").toURI());      
		String path_parent = path.getParent().getParent().getParent().toString();
		
				
		//=======================================================================================
		// Defining extensions
		//=======================================================================================
		
		String test = "test" + numTest;
		String path_data = "";
		
		if(ss) {
			path_data =  path_parent + "/data/data_test/" + SNR + "/";
		}else {
			path_data =  path_parent + "/data/data_test_sin_ss/" + SNR + "/";
		}
		
		String path_audios = path_data + "audios/";
		String path_features = path_data + "features/";
		String path_labels_java = path_data + "labels_java/";
		
		String extWav = ".wav";
		String extCSV = ".csv";
		String extStressLabels = "StressLabels_";
		String extFeatures = "features_";

		
		File f = new File(path_audios + test + extWav);
			
		if (f.exists()) {

			wavFile = path_audios + test + extWav;
			
			csvLabelsStress = path_labels_java + extStressLabels + test + extCSV;
			csvFeatures = path_features + extFeatures + test + extCSV;
			
			originalSignal = ReadWAV.loadFile(wavFile);
						
		}

	}
	
	
	//=======================================================================================
	// Getters and Setters
	//=======================================================================================

	
	public String getWavFile() {
		return wavFile;
	}

	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}

	
	public String getCsvLabelsStress() {
		return csvLabelsStress;
	}

	public void setCsvLabelsStress(String csvLabelsStress) {
		this.csvLabelsStress = csvLabelsStress;
	}

	public String getCsvLabelsSpeaker() {
		return csvLabelsSpeaker;
	}

	public void setCsvLabelsSpeaker(String csvLabelsSpeaker) {
		this.csvLabelsSpeaker = csvLabelsSpeaker;
	}

	public String getCsvFeatures() {
		return csvFeatures;
	}

	public void setCsvFeatures(String csvFeatures) {
		this.csvFeatures = csvFeatures;
	}

	public double[] getOriginalSignal() {
		return originalSignal;
	}

	public void setOriginalSignal(double[] originalSignal) {
		this.originalSignal = originalSignal;
	}

	public double[] getNorSignal() {
		return norSignal;
	}

	public void setNorSignal(double[] norSignal) {
		this.norSignal = norSignal;
	}

	public double[] getCleanSignal() {
		return cleanSignal;
	}

	public void setCleanSignal(double[] cleanSignal) {
		this.cleanSignal = cleanSignal;
	}

	public double[] getFinalSignal() {
		return finalSignal;
	}

	public void setFinalSignal(double[] finalSignal) {
		this.finalSignal = finalSignal;
	}

	public VAD getVadUser() {
		return vadUser;
	}

	public void setVadUser(VAD vadUser) {
		this.vadUser = vadUser;
	}

	public double[][] getFeatures() {
		return features;
	}

	public void setFeatures(double[][] features) {
		this.features = features;
	}


	
}
