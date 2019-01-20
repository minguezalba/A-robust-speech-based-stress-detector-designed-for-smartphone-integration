package test;
import java.util.ArrayList;
import java.util.Arrays;

import MathTools.Complex;
import MathTools.FFT;
import MathTools.Maths;
import MathTools.WindowFunction;

public class VAD {
	
	private int fs;
	private int win; //samples
	private int step; // samples
	
	int NFFT = 512; //Potencia de 2 mayor que ventana (20 ms = 320 samples)	
	
	private double noisePrevious[];
	private int vadFrames[]; // Vad generate from the algorithm with win = 30 ms. This is the one used by Denoiser
	private int vadSamples[]; // Extrapolation from vadFrames to the length of the original signal in 16000 Hz
	private int vadChunks[]; // Mean vector from vadSamples with 1 or 0 per second (1=voiced, 0=unvoiced)
	private int vadSamplesClean[]; //Extrapolation from vadChunks making sure 1s and 0s appears in 1second blocks but with resolution per sample.
	

	public VAD(double[] signal, int fs, int win, int step, int NFFT) {
		
		this.fs = fs;
		this.win = win;
		this.step = step;
		this.NFFT = NFFT;		
				
		
		// 1.1 We need a first estimation of the noise (150 ms)
		
		this.noisePrevious = SS.estimateNoise(signal, 0, null, this.fs, this.NFFT, this.win, this.step);
				
		// 1.2 Generate VAD vector [Nx1]
		
		this.vadFrames = this.generateVADFrames(signal, this.noisePrevious, this.fs, this.NFFT, this.win, this.step);
		this.vadSamples = this.generateVADplot(signal, this.vadFrames, this.step);

		
	}
	
	// Getters and Setters
	
	public int getFs() {
		return fs;
	}

	public int getWin() {
		return win;
	}

	public int getStep() {
		return step;
	}

	public int getNFFT() {
		return NFFT;
	}

	public double[] getNoisePrevious() {
		return noisePrevious;
	}

	public void setNoisePrevious(double[] noisePrevious) {
		this.noisePrevious = noisePrevious;
	}

	public int[] getVadFrames() {
		return vadFrames;
	}

	public void setVadFrames(int[] vadFrames) {
		this.vadFrames = vadFrames;
	}

	public int[] getVadSamples() {
		return vadSamples;
	}

	public void setVadSamples(int[] vadSamples) {
		this.vadSamples = vadSamples;
	}

	public int[] getVadChunks() {
		return vadChunks;
	}

	public void setVadChunks(int[] vadChunks) {
		this.vadChunks = vadChunks;
	}

	public int[] getVadSamplesClean() {
		return vadSamplesClean;
	}

	public void setVadSamplesClean(int[] vadSamplesClean) {
		this.vadSamplesClean = vadSamplesClean;
	}

	/**
	 * 
	 * @param enhancedSignal
	 * @return
	 */

	public double[] removeSilence(double[] enhancedSignal) {

		int fs = 16000;
		
		this.vadChunks = this.redimVAD(this.vadSamples, fs);
		
		this.vadSamplesClean = this.cleanVAD(this.vadChunks, this.vadSamples, fs);
				
		double readySignal[] = this.applyVAD(enhancedSignal, this.vadSamplesClean);
		
		return readySignal;
	}

	
	/**
	 * 
	 * @param E
	 * @return
	 */
	
	public static double thresholdComputation(double E) {
		
		//Datos 
		int E_0 = 50;
		int E_1 = 70;
		int Gamma_0 = 9;
		int Gamma_1 = 6;
		
		double U = 0;
	
		if (E<= E_0) {
		    
		    U = Gamma_0;
		    
		}else if(E>E_0 && E<E_1) {
		    
		    U = ((Gamma_0-Gamma_1)/(E_0 - E_1))*E + Gamma_0 - ((Gamma_0-Gamma_1)/(1-(E_1/E_0)));
		    
		}else if(E>=E_1) {
		    
		    U = Gamma_1;
		}
		
		return U;
		
	}
	

	
	/**
	 * 
	 * @param signal
	 * @param NFFT
	 * @param win
	 * @param step
	 * @return
	 */
	
	public static int[] VadAlgorithm(double signal[], double R_k[], int NFFT, int win, int step, double U) {
		
		int N = signal.length;
		
		WindowFunction winH = new WindowFunction();
		winH.setWindowType("HAMMING");
		
		int Nframes = (int) Math.floor((N-win+step)/step)+1; //Compute the number of windows
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		//LTSE estimation																					//
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		
		int N_LTSE = 6;
		
		double X_k_l[][] = new double[Nframes][];
		Complex xf_t[] = new Complex[0];
		
		int n1 = 0;
		int n2 = win;

		for(int n=0; n<Nframes; n++) {
				
			if(n<Nframes-1) {
				//xf_t =  Maths.double2Complex(Maths.multiply(Arrays.copyOfRange(signal, n1, n2), winHamming));
				xf_t =  Maths.double2Complex(Arrays.copyOfRange(signal, n1, n2));
			}else if(n==Nframes-1) {
				double aux[] = Arrays.copyOfRange(signal, n1, signal.length);
				//xf_t = Maths.double2Complex(Maths.multiply(Arrays.copyOf(aux, win), winHamming));
				xf_t = Maths.double2Complex(Arrays.copyOf(aux, win));
			}

					
			Complex[] xf_w = FFT.fft(xf_t, NFFT); // Arrays to contain fft
			
			X_k_l[n] = Maths.magnitude(xf_w);	    	
			
			n1 = n1 + step;
			n2 = n2 + step;
		
		}
		
		
		double zeros[][] = new double[N_LTSE][NFFT];
		
		double X_k_l_extended[][] = Maths.vercat(zeros, X_k_l);
		X_k_l_extended = Maths.vercat(X_k_l_extended, zeros);
		
		
		double X_k_l_extended_transp[][] = Maths.trasposeMatrix(X_k_l_extended);
		
		double LTSE_transp[][] = new double[NFFT][Nframes];
		
		for(int k=0; k<NFFT; k++) {
		
			double fila_aux[] = X_k_l_extended_transp[k];
			
			for(int l=N_LTSE; l<Nframes+N_LTSE; l++) {
			
				double aux[] = Arrays.copyOfRange(fila_aux, l-N_LTSE, l+N_LTSE+1);
				LTSE_transp[k][l-N_LTSE] = Arrays.stream(aux).max().getAsDouble();
			
			}
		
		}
		
		double LTSE[][] = Maths.trasposeMatrix(LTSE_transp);
		
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		// 	LTSD computation																				//
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		
		double LTSD[] = new double[Nframes];
		
		for(int i=0; i<Nframes; i++) {
		
			double[] a = Maths.multiply(LTSE[i], LTSE[i]); //al cuadrado
			double[] b = Maths.multiply(R_k, R_k);
			
			double sumatorio = Maths.sumatorio(Maths.divide(a, b));
			LTSD[i] = 10*Math.log10(sumatorio/NFFT);
					
		}
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		// 	Label Decision																				//
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		
		int vadVector[] = new int[Nframes];
		
		for(int i=0; i<Nframes; i++) {
		
			if(LTSD[i]>U) {
				vadVector[i] = 1;
			}else {
				vadVector[i] = 0;
			}
		
		}
		
		return vadVector;
		
	}
	
	
	/**
	 * 
	 * @param signal
	 * @param noiseInitial
	 * @return
	 */
	
	public int[] generateVADFrames(double[] signal, double[] noiseInitial, int fs, int NFFT, int win, int step) {

		double E_db = 10*Math.log10(Maths.sumatorio(Maths.multiply(noiseInitial, noiseInitial))/NFFT);
	    
	    double U = VAD.thresholdComputation(E_db);
		
	    int vadVector[] = VAD.VadAlgorithm(signal, noiseInitial, NFFT, win, step, U);
					
		return vadVector;
	}
	
	/**
	 * 
	 * @param signal
	 * @param labelsEstimated
	 * @param step
	 * @return
	 */
	
	
	public int[] generateVADplot(double[] signal, int[] labelsEstimated, int step) {

		int vadPlot[] = new int[signal.length];
		int Nframes = labelsEstimated.length;
			
		for(int i=0; i<Nframes; i++) {
						
			if(i<Nframes-1) {
				
				Arrays.fill(vadPlot, i*step, (i+1)*step, labelsEstimated[i]);
				
			}else if(i==Nframes-1) {
				
				Arrays.fill(vadPlot, i*step, signal.length, labelsEstimated[i]);
			}
					
		}
		
		
		return vadPlot;
	}
	
	/**
	 * 
	 * @param vadSamples
	 * @param i
	 * @return
	 */

	public int[] redimVAD(int[] vadSamples, int fs) {
		
		double N = vadSamples.length;
		int chunkDuration = fs; // samples fs = 1 second
		double chunkWin = 2*chunkDuration;
		double chunkStep = chunkDuration;

		int Nchunks = (int) Math.ceil(N/chunkStep);
		
		int vadChunks[] = new int[Nchunks];
		int frame[];

		
		int n1 = 0;
		int n2 = (int) chunkWin;
		
		for(int n=0; n<Nchunks; n++) {
			
			if(n<Nchunks-1) { // If we are NOT in the last chunk
				frame = Arrays.copyOfRange(vadSamples, n1, n2);
			}else { // If we ARE in the last chunk
				frame = Arrays.copyOfRange(vadSamples, n1, (int) N);
			}
			
			vadChunks[n] = this.countBinay(frame);
						
			
			n1 = (int) (n1 + chunkStep);
			n2 = (int) (n2 + chunkStep);
			
		}
				
		
		return vadChunks ;
	}
	

	public int[] cleanVAD(int[] vadChunks, int[] vadSamples, int fs) {
		
			
		int N = vadSamples.length;
		int chunkDuration = 1; // seconds
		double chunkStep = fs*chunkDuration;
		
		int vadSamplesClean[] = new int[N];
		
		int n1 = 0;
		int n2 = (int) chunkStep;
		
		for(int n=0; n<vadChunks.length; n++) {
			
			if(n<vadChunks.length-1) { // If we are NOT in the last chunk
				Arrays.fill(vadSamplesClean, n1, n2, vadChunks[n]);
			}else { // If we ARE in the last chunk
				Arrays.fill(vadSamplesClean, n1, N, vadChunks[n]);
			}
			
	
			n1 = (int) (n1 + chunkStep);
			n2 = (int) (n2 + chunkStep);
			
		}
		
		
		
		return vadSamplesClean;
	}

	/**
	 * 
	 * @param frame
	 * @return
	 */
	
	private int countBinay(int[] frame) {
		
		int zeroCount = 0;
		int oneCount = 0;
		
		for(int i=0; i<frame.length; i++) {
			
			if(frame[i]==0) {
				zeroCount++;
			}else {
				oneCount++;
			}
			
		}
		
		if(zeroCount >= oneCount) {
			return 0;
		}else {
			return 1;
		}

	}
	

	/**
	 * 
	 * @param enhanced_signal
	 * @param vadChunks
	 * @return
	 */
	
	
	public double[] applyVAD(double[] enhancedSignal, int[] vadSamplesClean) {
				
		ArrayList<Double> readySignalList = new ArrayList<Double>();
				
		for(int i=0; i<vadSamplesClean.length; i++) {
			
			// We only save voiced samples
			if(vadSamplesClean[i] == 1) {
				readySignalList.add(enhancedSignal[i]);
			}
			
		}
		
		// Convert list to array

		double readySignal[] = new double[readySignalList.size()];
		
		for (int i = 0; i < readySignal.length; i++) {
			readySignal[i] = readySignalList.get(i).doubleValue();
		}
		
		return readySignal;

	}

}
