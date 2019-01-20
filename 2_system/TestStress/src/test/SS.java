package test;
import java.util.Arrays;

import MathTools.Complex;
import MathTools.FFT;
import MathTools.Maths;
import MathTools.WindowFunction;

public class SS {
	
	/**
	 * 
	 * @param signal
	 * @param mode
	 * @param NoisePrevious
	 * @return
	 */
	
	public static double[] estimateNoise(double signal[], int mode, double noisePrevious[], int fs, int NFFT, int win, int step) {
		
		double noiseActual[] = new double[NFFT];
		double spectrogram[] = new double[NFFT];
		Complex xf_t[];
		Complex[] xf_w;
		
		int n1 = 0;
		int n2 = win;
		
	
		switch (mode) {
		
			// ===============================================================================================================================
			case 0:  // Initial estimation of the noise (150 ms)
				
				int Nframes_noise = (int) Math.floor((150e-3*fs)/step);
								
				WindowFunction winH = new WindowFunction();
				winH.setWindowType("HAMMING");
				
				for(int n=0; n<Nframes_noise; n++) {
								
					//xf_t = Maths.double2Complex(Maths.multiply(Arrays.copyOfRange(signal, n1, n2), winHamming));
					xf_t = Maths.double2Complex(Arrays.copyOfRange(signal, n1, n2));
					
					xf_w = FFT.fft(xf_t, NFFT); // Arrays to contain fft
					
					double[] mag = Maths.magnitude(xf_w);
					spectrogram = Maths.add(spectrogram, mag);
					
					n1 = n1 + step;
					n2 = n2 + step;
				
				}
				
				noiseActual = Maths.divide(spectrogram, Nframes_noise);
								
				
			break;
				
			// ===============================================================================================================================
			case 1:  // If we are NOT in a "noise burst", so previous vad_sample is 1. We do NOT take into account previous estimations.
								
				xf_t = Maths.double2Complex(signal);
				
				xf_w = FFT.fft(xf_t, NFFT); // Arrays to contain fft
				
				noiseActual = Maths.magnitude(xf_w);
					
			break;
						
			// ===============================================================================================================================
			case 2:  // If we are in a "noise burst" where previous vad_sample is also 0. Mean estimation:
								
				xf_t = Maths.double2Complex(signal);
				
				xf_w = FFT.fft(xf_t, NFFT); // Arrays to contain fft
				
				noiseActual = Maths.magnitude(xf_w);
				
				// Weighted mean
				
				double alpha = 0.5;
								
				noiseActual = Maths.add(Maths.multiply(noiseActual, alpha), Maths.multiply(noisePrevious, (1-alpha)));
				
			break;
			
			// ===============================================================================================================================
			default:
				System.out.println("Error in 'mode' variable estimateNoise()");
				System.exit(0);
		
		
		}// end switch
		
		return noiseActual;
		
	} //end estimateNoise()
	
	/**
	 * 
	 * @param matrix_mag_Y_w
	 * @param D_w
	 * @return
	 */
	
	public static double[] spectral_over_subtraction(double matrix_mag_Y_w[], double D_w[]) {
		
		double mag_Y_w_squared[] = Maths.multiply(matrix_mag_Y_w, matrix_mag_Y_w);
		double D_w_squared[] = Maths.multiply(D_w, D_w);
		
		double SSNR = Maths.sumatorio(mag_Y_w_squared)/Maths.sumatorio(D_w_squared);
		
		double alpha = 4 - (3/20)*SSNR;
		
	    if(alpha <= -5) {
	    	alpha = 4 - (3/20)*-5;
	    }else if(alpha >= 20) {
	    	alpha = 4 - (3/20)*20;
	    }
	            
	    double beta = 0.01;
	    
	    double threshold_vector[] = Maths.multiply(D_w_squared, (alpha+beta));
	    
	    double s_hat_w_squared[] = new double[D_w.length];
	    
	    for(int i=0; i<D_w.length; i++) {
	    	
	    	if(mag_Y_w_squared[i] > threshold_vector[i]) {
	    		
	    		s_hat_w_squared[i] = mag_Y_w_squared[i] - alpha*D_w_squared[i];
	    		
	    	}else {
	    		
	    		s_hat_w_squared[i] = beta*D_w_squared[i];
	    		
	    	}
	    	
	    }
	    
	    return s_hat_w_squared;
		
		
	}
	
	/**
	 * 
	 * @param xf_t
	 * @param D_w
	 * @param Nframes
	 * @param NFFT
	 * @param win
	 * @return
	 */
	
	public static double[] denoise_frame(Complex xf_t[], double D_w[], int NFFT, int win) {
		
	    double matrix_mag_Y_w[] = new double[NFFT];
	    double matrix_phase_Y_w[] = new double[NFFT];
	    double s_hat_w_squared_magnitude[] = new double[NFFT];
	    Complex s_hat_w[] = new Complex[NFFT];		

		//////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1. Spectrum of each frame																		//
		//////////////////////////////////////////////////////////////////////////////////////////////////////

    	Complex[] xf_w = FFT.fft(xf_t, NFFT);	    	
    	
    	matrix_mag_Y_w = Maths.magnitude(xf_w);
    	matrix_phase_Y_w = Maths.phase(xf_w);
    	
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2. Spectral over-subtraction squared magnitude													//
		//////////////////////////////////////////////////////////////////////////////////////////////////////

    	s_hat_w_squared_magnitude = SS.spectral_over_subtraction(matrix_mag_Y_w, D_w);
    	
    	
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3. Spectral over-subtraction complex number														//
		//////////////////////////////////////////////////////////////////////////////////////////////////////
   
    		    	
    	s_hat_w = Maths.multiply(Maths.sqrt(s_hat_w_squared_magnitude), 
    								Maths.exp(Maths.multiply(matrix_phase_Y_w, new Complex(0,1))));
    			
    				    			

		//////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. Enhanced speech signal s_hat[n] in time domain												//
		//////////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	Complex s_hat_ifft[] = FFT.ifft(s_hat_w, NFFT);
    	
    	// Nos quedamos solo con la parte real porque la compleja debería salir muy pequeña
    	
    	return Arrays.copyOfRange(Maths.real(s_hat_ifft), 0, win);
		
	}
	
	

	/**
	 * 
	 * @param s_hat_temp
	 * @param win
	 * @param step
	 * @return
	 */
	
	
	public static double[] overlapadd(double s_hat_temp[][], double signal[], int win, int step) {
		
		int Nframes = s_hat_temp.length; //each frame has 320 samples = win
		
		int len = signal.length; //buffer length: lo que seria la longitud de la señal original
		
		double enhanced_signal[] = new double[len];
		
		//=====================================================================================
		
		for(int n=0; n < Nframes; n++) {
		
			if(n<Nframes-1) {
				
				double first_half[] = Arrays.copyOfRange(s_hat_temp[n], 0, step);
				System.arraycopy(first_half, 0, enhanced_signal, n*step, step);
				
			}else if(n==Nframes-1) {
				
				int size = len - (Nframes-1)*step;
				
				System.arraycopy(s_hat_temp[n], 0, enhanced_signal, n*step, size);
				
			}
			
			
		}		
				
		return enhanced_signal;
		
	}

	public static double[] denoiseSignal(double[] signal, VAD vadUser, int win, int step, int NFFT, int fs) {
		
		 int[] vadFrames = vadUser.getVadFrames();
		 double[] noisePrevious = vadUser.getNoisePrevious();
		 
		int N = signal.length;
		
		//=======================================================================================
		// 1. Process frame by frame
		//=======================================================================================
		
		int Nframes = vadFrames.length;
		int n1 = 0;
		int n2 = win;
		
		double s_hat_temp[][] = new double[Nframes][win];
		double noiseActual[] = noisePrevious.clone();
		Complex xf_t[] = new Complex[0];
		double xf_aux[] = new double[0];
		
		for(int n=0; n<Nframes; n++) {
			
			// Frame rectangular window
			if(n<Nframes-1) {
				xf_aux = Arrays.copyOfRange(signal, n1, n2); //double type
			}else if(n==Nframes-1) {
				xf_aux = Arrays.copyOfRange(signal, n1, N);
				if(xf_aux.length==256) {
					xf_aux = Arrays.copyOf(xf_aux, NFFT);
				}
			}
			
			xf_t = Maths.double2Complex(xf_aux); //complex type
	    	 
	    	// Case 1: We are in a voiced sample or in the first unvoiced sample. We do not re-estimate noise
	    	if(vadFrames[n]==1 || (vadFrames[n]==0 && n==0)) {
	    		
	    		s_hat_temp[n] = SS.denoise_frame(xf_t, noiseActual,  NFFT, win);	    		
	    		
	    	// Case 2: We are in an unvoiced sample. We re-estimate noise
	    	}else if (vadFrames[n]==0) {
	    		
	    		// Case 2.1: The estimation of the noise is from the actual frame
	    		if (vadFrames[n-1]==1) {	    		
	    			
	    			noiseActual = SS.estimateNoise(xf_aux, 1, null, fs, NFFT, win, step);
	    			s_hat_temp[n] = SS.denoise_frame(xf_t, noiseActual, NFFT, win);	   
	    			
	    		
	    		// Case 2.2: The estimation of the noise is the mean of the actual and previous frames
	    		}else if (vadFrames[n-1]==0) {
	    			    			
	    			noisePrevious = noiseActual;
	    			noiseActual = SS.estimateNoise(xf_aux, 2, noisePrevious, fs, NFFT, win, step);
	    			s_hat_temp[n] = SS.denoise_frame(xf_t, noiseActual, NFFT, win);	   
	    		
	    		}
	    	}
	    		    	
	    	 	
	        n1 = n1 + step;
	        n2 = n2 + step;
			
		}
		
		//=======================================================================================
		// 2. Recovering whole signal			
		//=======================================================================================
		
		double enhancedSignal[] = SS.overlapadd(s_hat_temp, signal, win, step);
		
		// There are some extra sampling cause we did zero padding to the last window
		enhancedSignal = Arrays.copyOfRange(enhancedSignal, 0, N);
		
		return enhancedSignal;
	}

}
