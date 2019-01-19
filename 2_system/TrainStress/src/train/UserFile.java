package train;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.UnsupportedAudioFileException;

import MathTools.Maths;

public class UserFile {
	
	//=======================================================================================
	// General File properties
	//=======================================================================================

	private String ID;
	private String setname;
	private String tipo; // Puede ser "_rec" o "_base"
	private String SNR;
	
	//=======================================================================================
	// User files to load/import
	//=======================================================================================
	
	private String wavFile; 
	private String csvZecg; 	
	
	//=======================================================================================
	// User files to generate/export
	//=======================================================================================

	private String csvLabelsStress; 
	private String csvLabelsSpeaker; 
	private String csvFeatures; 
	private String csvIndexes; 
		
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
	// Labels variables
	//=======================================================================================

	private int[] labelStress;
	private int[] labelSpeaker;
	
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

	public UserFile(String id, String setname, String tipo, String snr, boolean ss) throws UnsupportedAudioFileException, IOException, URISyntaxException {
		
		this.ID = "ID_" + id;
		this.setname = setname;
		this.SNR = snr;
		this.tipo = tipo;
		
			
		//=======================================================================================
		// Defining prefixes and suffixes
		//=======================================================================================			
		
		String extWav = ".wav";
		String extCSV = ".csv";
		String extRec = "_rec";
		String extBase = "_base";
		String extWavBase = "_baseline";
		String extWavPreBase = "_prebaseline";
		String extSensors = "_sensors";
		String extStressLabels = "StressLabels_";
		String extSpeakerLabels = "SpeakerLabels_";
		String extFeatures = "features_";
		String extIndexes = "indexes_";
		
		
		//=======================================================================================
		// Defining paths
		//=======================================================================================			
	

		Path path = Paths.get(Main.class.getResource(".").toURI());      
		String path_parent = path.getParent().getParent().getParent().toString();
				
		String data_recordings = path_parent + "/data/data_recordings/";
		String path_audios = data_recordings + "audios/" + SNR + "/" + setname + "/";
		String path_sensors = data_recordings + "sensors/" + setname + "/";
		
		String data_model = "";
		if(ss) {
			data_model = path_parent + "/data/data_model/" + SNR + "/";
		}else {
			data_model = path_parent + "/data/data_model_sin_ss/" + SNR + "/";
		}

		
		String path_features = data_model + "features/" + setname + "/" + extFeatures;
		String path_labels_stress = data_model + "labels/" + setname + "/stress/" + extStressLabels;
		String path_labels_speaker = data_model + "labels/" + setname + "/speaker/" + extSpeakerLabels;
		String path_indexes = data_model + "indexes/" + setname + "/" + extIndexes;
				
	
		//=======================================================================================
		// Filenames
		//=======================================================================================			

		
		if(tipo.equals("_rec")) {
			
			File fRec = new File(path_audios + ID + extWav);
			if (fRec.exists()) {

				wavFile = path_audios + ID + extWav;
				csvZecg = path_sensors + ID + extSensors + extCSV;

				csvLabelsStress =  path_labels_stress + ID + extRec + extCSV;
				csvLabelsSpeaker = path_labels_speaker + ID + extRec + extCSV;
				csvFeatures = path_features + ID + extRec + extCSV;
				csvIndexes = path_indexes + ID + extRec + extCSV;
				
				originalSignal = ReadWAV.loadFile(wavFile);
						
			}
			
		}else if(tipo.equals("_base")){
			
			File fBase = new File(path_audios + ID + extWavBase + extWav);
			File fPre = new File(path_audios + ID + extWavPreBase + extWav);
			
			if (fBase.exists()) {

				wavFile = path_audios + ID + extWavBase + extWav;
				csvZecg = path_sensors + ID + extWavBase + extSensors + extCSV;
				
				csvLabelsStress = path_labels_stress + ID + extBase + extCSV;
				csvLabelsSpeaker = path_labels_speaker + ID + extBase + extCSV;
				csvFeatures = path_features + ID + extBase + extCSV;
				csvIndexes = path_indexes + ID + extBase + extCSV;
				
				originalSignal = ReadWAV.loadFile(wavFile);
				
			}else if (fPre.exists()) {

				wavFile = path_audios + ID + extWavPreBase + extWav;
				csvZecg = path_sensors + ID + extWavPreBase + extSensors + extCSV;

				csvLabelsStress = path_labels_stress + ID + extBase + extCSV;
				csvLabelsSpeaker = path_labels_speaker + ID + extBase + extCSV;
				csvFeatures = path_features + ID + extBase + extCSV;
				csvIndexes = path_indexes + ID + extBase + extCSV;
				
				originalSignal = ReadWAV.loadFile(wavFile);
			}

		}
	}
	
	public void finalAdjust() {
		
		// Adjust dimensions between labels and features cutting by the end
		
		int dimensions[] = new int[] {this.labelStress.length, this.features[0].length};
		int min = Maths.min(dimensions);
		
		if(this.labelStress.length > min) this.labelStress = Maths.adjustColDimension(this.labelStress, min);
		if(this.features[0].length > min) this.features = Maths.adjustColDimension(this.features, min);
		
		
	}
	
	//=======================================================================================
	// Getters and Setters
	//=======================================================================================
	

	public String getCsvIndexes() {
		return csvIndexes;
	}

	public void setCsvIndexes(String csvIndexes) {
		this.csvIndexes = csvIndexes;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getSetname() {
		return setname;
	}

	public void setSetname(String setname) {
		this.setname = setname;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getWavFile() {
		return wavFile;
	}

	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}

	public String getCsvZecg() {
		return csvZecg;
	}

	public void setCsvZecg(String csvZecg) {
		this.csvZecg = csvZecg;
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

	public int[] getLabelStress(){
		return labelStress;
	}

	public void setLabelStress(int[] labelStressOriginal) {
		this.labelStress = labelStressOriginal;
	}

	public int[] getLabelSpeaker() {
		return labelSpeaker;
	}

	public void setLabelSpeaker(int[] labelSpeaker) {
		this.labelSpeaker = labelSpeaker;
	}

	public double[][] getFeatures() {
		return features;
	}

	public void setFeatures(double[][] features) {
		this.features = features;
	}


	
}
