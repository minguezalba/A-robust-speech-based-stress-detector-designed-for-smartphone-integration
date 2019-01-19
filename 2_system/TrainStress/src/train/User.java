package train;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import MathTools.Maths;

public class User {
	
	//=======================================================================================
	// User properties
	//=======================================================================================

	private String ID; // Identificador del usuario
	private String setname; // Set al que pertenece
	
	//=======================================================================================
	// User files associated with each instant.
	//=======================================================================================
	
	private UserFile recording;
	private UserFile baseline;

	/**
	 * 
	 * @param id
	 * @param set
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * @throws URISyntaxException 
	 */
	
	public User(String id, String set, String SNR, boolean ss) throws UnsupportedAudioFileException, IOException, URISyntaxException {
		
		ID = "ID_" + id;
		setname = set;

		//=======================================================================================
		// Generating paths and UserFiles for each instant
		//=======================================================================================

		recording = new UserFile(id, set, "_rec", SNR, ss);
		baseline = new UserFile(id, set, "_base", SNR, ss);

		
	}
	
	//=======================================================================================
	// Getters and Setters
	//=======================================================================================

	
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

	public UserFile getRecording() {
		return recording;
	}

	public void setRecording(UserFile recording) {
		this.recording = recording;
	}

	public UserFile getBaseline() {
		return baseline;
	}

	public void setBaseline(UserFile baseline) {
		this.baseline = baseline;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	
	public void generateLabelsStress(VAD vadRecording, VAD vadBaseline) throws IOException {

		// Cargamos los ficheros que contienen los valores de HR
		double[] HRvaluesRec = DataTools.CSV2Array(this.recording.getCsvZecg());
		double[] HRvaluesBase = DataTools.CSV2Array(this.baseline.getCsvZecg());

		Percentile p = new Percentile();
		double umbral = p.evaluate(HRvaluesBase, 75.0); // Sacamos el umbral a partir del estado baseline
		
		this.recording.setLabelStress(fillLabelsStress(HRvaluesRec, umbral, vadRecording));
		this.baseline.setLabelStress(fillLabelsStress(HRvaluesBase, umbral, vadBaseline));
		

	}
	
	/**
	 * 
	 * @param HRvalues
	 * @param umbral
	 * @return
	 */
	
	public static int[] fillLabelsStress(double[] HRvalues, double umbral, VAD vad) {

		// Los valores HR tienen una resolucion de 1 muestra por segundo
		// Nosotros queremos 1 etiqueta por segundo
		// Por tanto win = 2 muestras (2 segundos) y step = 1 muestra (1 segundo) = HRvalues.length
		
		double win = 2;
		double step = win/2;
		
		double N = HRvalues.length;
		int Ntimes = (int) Math.ceil((N-win+step)/step); //Compute the number of windows
		
		int labels[] = new int[Ntimes];
		double frame[] = new double[0];
		
		int n1 = 0;
		int n2 = (int) win;

		for(int n=0; n<Ntimes; n++) {
			
			if(n<Ntimes-1) {
				
				frame = Arrays.copyOfRange(HRvalues, n1, n2);
				
			}else if(n==Ntimes-1) {
				
				frame = Arrays.copyOfRange(HRvalues, n1, (int)N);
				
			}
			
			double mean = Maths.calculateMean(frame);
			
			if (mean > umbral) {
				labels[n] = 1;
			} else {
				labels[n] = 0;
			}
			
			n1 = (int) (n1 + step);
			n2 = (int) (n2 + step);
			
		}
		
		// Now we have labels generated 1label per second, we remove silence areas from them
		
		int[] vadChunks = vad.getVadChunks(); // 1 samples per second (1=voiced, 0=unvoiced)
		
		// In this point, vadChunks and labels should have same size, but this dont usually happens 
		// because HRvalues were taken (or stopped) at different time compared to the signal
		
		// So we're going to clean the HRvalues with the information we have
		int dim[] = {vadChunks.length, labels.length};
		int minLength = Maths.min(dim);
		
		if(vadChunks.length>minLength) {
			vadChunks = Arrays.copyOfRange(vadChunks, 0, minLength);
			vad.setVadChunks(vadChunks);
		}
		
		ArrayList<Integer> labelsCleanList = new ArrayList<Integer>();
				
		for(int i=0; i<minLength; i++) {
			
			if(vadChunks[i]==1) {
				labelsCleanList.add(labels[i]);
			}
			
		}
		
		// Convert list to array

		int labelsClean[] = new int[labelsCleanList.size()];
		
		for (int i = 0; i < labelsClean.length; i++) {
			labelsClean[i] = labelsCleanList.get(i).intValue();
		}
		
					

		return labelsClean;
	}


}
