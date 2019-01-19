package train;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import MathTools.Maths;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import soundBites.AnalysisInterface;
import soundBites.MFCC;

public class Features {

	public static double[][] generateFeatures(UserFile userfile, int fs, int win, int step, int NFFT) throws IllegalArgumentException, IOException {

		double signal[] = userfile.getFinalSignal();
		
		/*
		int win2 = 512;
		int step2 = win2/2;
		
		// All feature functions need signal length as a multiple of step2
				
		int dif = signal.length % step2;
		
		double signalCut[] = Arrays.copyOfRange(signal, 0, signal.length - dif);
		*/
		
		double mfccRaw[][] = MFCC(signal, fs, win, NFFT); // MFCC matrix: 13 coefficients x nFrames
		
		double mfccStats[][] = redim1seg(mfccRaw, fs, step); // MFCC matrix: 26 coefficients x N1segBlocks
		 
		double otherFeaturesRaw[][] = getOtherFeatures(signal, step); // feature matrix: 8 coefficients x nFrames
		
		double otherFeaturesStats[][] = redim1seg(otherFeaturesRaw, fs, step); // feature matrix: 16 coefficients x N1segBlocks
		
		double pitchStats[][] = getPitchStats(signal, fs); // first row: mean pitch values per second
														   // second row: std pitch values per second
				
		// Adjusting dimensions cutting by the end
				
		int dimensions[] = new int[] {mfccStats[0].length, otherFeaturesStats[0].length, pitchStats[0].length};
		int min = Maths.min(dimensions);
		
		if(mfccStats[0].length > min) mfccStats = Maths.adjustColDimension(mfccStats, min);
		if(otherFeaturesStats[0].length > min) otherFeaturesStats = Maths.adjustColDimension(otherFeaturesStats, min);
		if(pitchStats[0].length > min) pitchStats = Maths.adjustColDimension(pitchStats, min);
		
		// Concat vertically all the features
		double[][] finalFeatures = Maths.vercat(mfccStats, otherFeaturesStats);
		finalFeatures = Maths.vercat(finalFeatures, pitchStats);
		
		return finalFeatures;
	}
	


	public static double[][] redim1seg(double[][] data, int fs, int step) {

		int nRows = data.length;

		// Tomamos los valores de ventanas que hemos utilizado para sacar los MFCC

		// Antes hemos extraido un coef por cada hopSize muestras (256)
		// Ahora queremos una resolución de un coef por cada 1 seg = 16000 samples = sampleRate muestras

		double[][] dataRedim = new double[2*nRows][];

		for (int i = 0; i < nRows; i++) {

			// Redimensionamos esa fila concreta
			double[] rowMean = redimRow1seg(data[i], "mean", fs, step);
			double[] rowStd = redimRow1seg(data[i], "std", fs, step);

			dataRedim[i] = rowMean;
			dataRedim[i+nRows] = rowStd;

		}
		
		return dataRedim;

	}

	public static double[] redimRow1seg(double[] row, String op, int fs, int stepSamples) {
		
		// How many blocks of stepSize are in a 2seg = 2*fs(win) block?
		int winRedim = (int) Math.floor((2*fs) / stepSamples);
		
		// And inn a 1seg = fs(step) block?
		int stepRedim = (int) Math.floor(winRedim/2);
		
		int n1 = 0;
		int n2 = winRedim;
		
		int Ntimes = (int) Math.ceil(row.length / stepRedim);
		
		double rowRedim[] = new double[Ntimes];
		
		double frame[] = new double[0];
		
		
		for(int i=0; i<Ntimes; i++) {
			
			if(i<Ntimes-1) {
				
				frame = Arrays.copyOfRange(row, n1, n2);
				
			}else if(i==Ntimes-1) {
				
				frame = Arrays.copyOfRange(row, n1, row.length);
				
			}
			
			if(op.equals("mean")) {
				rowRedim[i] = Maths.calculateMean(frame);
			}else if(op.equals("std")) {
				rowRedim[i] = Maths.calculateSD(frame);
			}
			
			n1 = n1 + stepRedim;
			n2 = n2 + stepRedim;
			
		}

		return rowRedim;
	}


	/**
	 * 
	 * @param signal
	 * @param fs
	 * @param win
	 * @param step
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */

	public static double[][] MFCC(double signal[], int fs, int winReal, int NFFT) throws IllegalArgumentException, IOException {
		
		int numberCoefficients = 13;
		boolean useFirstCoefficient = true;
		int minFreq = 50;
		int maxFreq = 12000 / 2; 
		int numberFilters = 40;
	
		MFCC mfccObj = new MFCC(fs, NFFT, numberCoefficients, useFirstCoefficient, minFreq, maxFreq, numberFilters);
		
		// Devuelve una matriz nFrames x nCoefficients
		double[][] mfccRaw = mfccObj.process(signal, winReal);
		
		// Transponemos la matriz para redimensionar fila por fila, quedando nCoeffi x nFrames
		mfccRaw = Maths.trasposeMatrix(mfccRaw);
				
		
		return mfccRaw;
		
	}
	
	/**
	 * 
	 */
		
	public static double[][] getOtherFeatures(double signal[], int hopSize) {
		
		// Dividimos el audio en tramas
		double audioWindows[][] = wav2windows(signal, hopSize);
						
		// Calculamos las features para una ventana de 30 ms
		
		double[][] f2_matrix_raw = new double[audioWindows.length][];
	    
	    // Vamos cogiendo cada trama y analizamos sus features
	    for(int i=0;i<audioWindows.length;i++)
	    {
	    	f2_matrix_raw[i] = new double[7]; //matriz de nTramas x nCoeffs
	    	
	    	// Vamos guardando las features
	       	f2_matrix_raw[i][0] = AnalysisInterface.RMS(audioWindows[i]);
	    	f2_matrix_raw[i][1] = AnalysisInterface.zeroCrossRate(audioWindows[i]);
	    	f2_matrix_raw[i][2] = AnalysisInterface.spectralCentroid(audioWindows[i]);
	    	f2_matrix_raw[i][3] = AnalysisInterface.spectralFlux(audioWindows[i]);
	    	f2_matrix_raw[i][4] = AnalysisInterface.spectralRolloff(audioWindows[i]);
	    	f2_matrix_raw[i][5] = AnalysisInterface.spectralFlatness(audioWindows[i]);
	    	f2_matrix_raw[i][6] = AnalysisInterface.bandEnergyRatio(audioWindows[i], 0.5);
	    }
	    
	    // Transponemos la matriz para redimensionar fila por fila, quedando nCoeffi x nFrames

	    f2_matrix_raw = Maths.trasposeMatrix(f2_matrix_raw);
	    
	    return f2_matrix_raw;
	}
	
	/**
	 * 
	 */
	
	private static double[][] wav2windows(double signal[], int hopSize){
		
		double[] samples = signal;
		
		// Calculamos el numero de tramas que vamos a tener de tamaño hopSize
		int numFilas = (samples.length/hopSize); // Debe dar un numero exacto ya que en MFCC() forzamos multiplo de hopSize
		int numColumnas = hopSize;
				
		// Inicializamos array		
		double audioWindows[][] = new double[numFilas][numColumnas];
		
		// Rellenamos array con las tramas
		//Cada fila de la matriz audioWindows sera una trama de tamaño hopSize para analizar
		for(int i = 0, pos = 0; pos < samples.length - hopSize; i++, pos+=hopSize) {
			audioWindows[i] = Arrays.copyOfRange(samples, pos, pos+hopSize);
		}
		 
		return audioWindows;
		
	}
	
	/**
	 * 
	 * @param signal
	 * @param fs
	 * @return
	 * @throws FileNotFoundException
	 */
	
	//*vEsther
	
	public static double[][] getPitchStats(double[] signal, int fs) throws FileNotFoundException {
		
		// Detecting pitch for each second of audio with TarsosDSP library

		float sampleRate = fs;
		int audioBufferSize = 512;
		int bufferOverlap = 256;
		int tramas = (int) Math.ceil(signal.length / sampleRate);
		int start = 0;
		int finish = 0;
		double[] samples_ = signal;
		double[] finalPitchMean = new double[tramas];
		double[] finalPitchStd = new double[tramas];

		for (int n = 0; n < tramas; n++) {

			finish = (int) sampleRate * (n + 1);

			try {

				// Create an AudioInputStream from vadNorSignal (double[]) array

				double[] samples = Arrays.copyOfRange(samples_, start, finish);
				byte[] data = new byte[2 * samples.length];

				for (int i = 0; i < samples.length; i++) {
					int temp = (short) (samples[i] * 32767);
					data[2 * i + 0] = (byte) temp;
					data[2 * i + 1] = (byte) (temp >> 8);
				}

				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
				AudioInputStream stream = new AudioInputStream(bais, format, samples.length);

				// Convert into TarsosDSP API

				JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
				AudioDispatcher dispatcher = new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
				PitchDetector myPitchDetector = new PitchDetector();
				dispatcher.addAudioProcessor(
						new PitchProcessor(PitchEstimationAlgorithm.YIN, sampleRate, audioBufferSize, myPitchDetector));

				// Saving pitch data in text file

				PrintStream fileOut = new PrintStream("./auxPitch.txt");
				System.setOut(fileOut);
				dispatcher.run();
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

			} catch (FileNotFoundException fne) {
				fne.printStackTrace();
			}

			// Leer datos de pitch en variable

			File txt = new File("./auxPitch.txt");
			Scanner scan = new Scanner(txt);
			ArrayList<String> data_ = new ArrayList<String>();

			while (scan.hasNextLine()) {
				data_.add(scan.nextLine());
			}

			scan.close();

			// Convert text read to String array and to Double array

			String[] pitchRawString = data_.toArray(new String[] {});
			
			double[] pitchRawDouble = Arrays.stream(pitchRawString).mapToDouble(Double::parseDouble).toArray();

			// Calculate mean and std of all pitch values detected over 1s

			double sum = 0;

			for (int w = 0; w < pitchRawDouble.length; w++) {

				sum += pitchRawDouble[w];

			}

			// Value for mean pitch
			finalPitchMean[n] = sum / pitchRawDouble.length;

			// Limiting it from above and below, to 350 and 0 Hz respectively

			if (finalPitchMean[n] > 350) {
				finalPitchMean[n] = 350;
			} else if (finalPitchMean[n] < 0) {
				finalPitchMean[n] = 0;
			}

			// Calculate std pitch
			double auxstd = 0;

			for (int z = 0; z < pitchRawDouble.length; z++) {

				auxstd += Math.pow(pitchRawDouble[z] - finalPitchMean[n], 2);
			}
			
			// Value for std pitch
			finalPitchStd[n] = Math.sqrt(auxstd / pitchRawDouble.length);

			// Limiting it from above and below

			if (finalPitchStd[n] > 200) {
				finalPitchStd[n] = 200;
			} else if (finalPitchStd[n] < 0) {
				finalPitchStd[n] = 0;
			}

			start = finish + 1;

		}

		// Checking if there are NaN values in finalPitchMean and finalPitchStsd, these
		// vectors have one value per
		// second

		// 3 options for substituting NaN
		// 1) Repeating last pitch value
		// 2) Using 0 as pitch value
		// 3) Removing pitch and mfcc frames when pitch is NaN

		// Using option 1) provisionally

		for (int n = 0; n < tramas; n++) {

			if (Double.isNaN(finalPitchMean[n])) { // If there is a NaN value in mean

				if (n > 0) {

					finalPitchMean[n] = finalPitchMean[n - 1];

				} else { // In case it is the first value
					
					// while hasta el siguiente que sea distinto de nan
					int j = n+1;
					
					while(Double.isNaN(finalPitchMean[j])) {
						j++;
					}
					
					finalPitchMean[n] = finalPitchMean[j];


				}
			}

			if (Double.isNaN(finalPitchStd[n])) { // If there is a NaN value in std

				if (n > 0) {

					finalPitchStd[n] = finalPitchStd[n - 1];

				} else { // In case it is the first value

					// while hasta el siguiente que sea distinto de nan
					int j = n+1;
					
					while(Double.isNaN(finalPitchStd[j])) {
						j++;
					}
					
					finalPitchStd[n] = finalPitchStd[j];


				}

			}
		} // end for n tramas
		
		double[][] pitchStats = Maths.vercat(finalPitchMean, finalPitchStd);

		
		return pitchStats;

	} // end getPitchStats()

} // end Features class