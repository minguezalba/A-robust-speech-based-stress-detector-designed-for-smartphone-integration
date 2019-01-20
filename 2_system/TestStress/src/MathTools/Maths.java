package MathTools;

import java.util.Arrays;

public class Maths {
	
	public static double[] multiply(double[] a, double[] b) {
		
		int Na = a.length;
		int Nb = b.length;
		
		double res[] = new double[Na];
				
		if(Na == Nb) {
			
			for(int i=0; i<Na; i++) {
				res[i] = a[i] * b[i];
			}
			return res;
			
		}else {
			
			System.out.println("Error en las dimensiones de los arrays en multiply(double[] a, double[] b)");
			System.exit(0);
			return res;
		}
	}
	
	
	public static double[] multiply(double[] a, double k) {
		
		int Na = a.length;
		
		double res[] = new double[Na];

		for(int i=0; i<Na; i++) {
			res[i] = a[i] * k;
		}
		
		return res;

	}
	
	public static Complex[] multiply(double[] a, Complex k) {
		
		int Na = a.length;
		
		Complex res[] = new Complex[Na];

		for(int i=0; i<Na; i++) {
			
			double re = a[i] * k.re();
			double im = a[i] * k.im();
			res[i] = new Complex(re, im);
		}
		
		return res;

	}
	
	public static Complex[] multiply(double[] a, Complex[] b) {
		
		int Na = a.length;
		int Nb = b.length;
		
		Complex res[] = new Complex[Na];
				
		if(Na == Nb) {
			
			for(int i=0; i<Na; i++) {
				double re = a[i] * b[i].re();
				double im = a[i] * b[i].im();
				res[i] = new Complex(re, im);
			}
			return res;
			
		}else {
			
			System.out.println("Error en las dimensiones de los arrays en multiply(double[] a, Complex[] b)");
			System.exit(0);
			return res;
			
		}
	}
	
	
	public static double[] divide(double[] a, double[] b) {
		
		int Na = a.length;
		int Nb = b.length;
		
		double res[] = new double[Na];
				
		if(Na == Nb) {
			
			for(int i=0; i<Na; i++) {
				res[i] = a[i] / b[i];
			}
			return res;
			
		}else {
			
			System.out.println("Error en las dimensiones de los arrays en divide(double[] a, double[] b)");
			System.exit(0);
			return res;
		}
	}
	
	public static double[] add(double[] a, double[] b) {
		
		int Na = a.length;
		int Nb = b.length;
		
		double res[] = new double[Na];
				
		try {
			
			for(int i=0; i<Nb; i++) {
				res[i] = a[i] + b[i];
			}
		
			
		}catch(Exception e) {
			
			System.err.println(e);
			System.out.println("Error en las dimensiones de los arrays en add(double[] a, double[] b)");
			System.exit(0);
		}
		
		return res;
	}
	
	public static double[] magnitude(Complex[] a) {
		
		int Na = a.length;
		
		double res[] = new double[Na];
				

		for(int i=0; i<Na; i++) {
			res[i] = a[i].abs();
		}
		return res;

	}
	
	
	public static double[] phase(Complex[] a) {
		
		int Na = a.length;
		
		double res[] = new double[Na];
				

		for(int i=0; i<Na; i++) {
			res[i] = a[i].phase();
		}
		
		return res;

	}
	
	public static double[] sqrt(double[] a) {
		
		int Na = a.length;
		
		double res[] = new double[Na];
				

		for(int i=0; i<Na; i++) {
			res[i] = Math.sqrt(a[i]);
		}
		return res;

	}
	
	public static double[] exp(double[] a) {
		
		int Na = a.length;
		
		double res[] = new double[Na];
				

		for(int i=0; i<Na; i++) {
			res[i] = Math.exp(a[i]);
		}
		return res;

	}
	
	public static Complex[] exp(Complex[] a) {
		
		int Na = a.length;
		
		Complex res[] = new Complex[Na];
				

		for(int i=0; i<Na; i++) {
			res[i] = a[i].exp();
		}
		return res;

	}
	
	
	public static double[] real(Complex[] a) {
		
		int Na = a.length;
		
		double res[] = new double[Na];
				

		for(int i=0; i<Na; i++) {
			res[i] = a[i].re();
		}
		return res;

	}
	
	public static double[] imag(Complex[] a) {
		
		int Na = a.length;
		
		double res[] = new double[Na];
				

		for(int i=0; i<Na; i++) {
			res[i] = a[i].im();
		}
		return res;

	}
	

	
	public static double[] divide(double[] a, int k) {
		
		int Na = a.length;
		
		double res[] = new double[Na];

		for(int i=0; i<Na; i++) {
			res[i] = a[i] / k;
		}
		
		return res;

	}
	
	public static double sumatorio(double[] a) {
		
		int Na = a.length;
		
		double res = 0;

		for(int i=0; i<Na; i++) {
			res = res + a[i];
		}
		
		return res;

	}
	
	
	public static double calculateMean(double[] porcion_trama) {

		// Calculamos la media de porcion_trama
		double sum = 0.0;

		for (int i = 0; i < porcion_trama.length; i++) {
			sum += porcion_trama[i];
		}

		double media_trama = sum / porcion_trama.length;

		return media_trama;

	}

	public static double calculateSD(double numArray[]) {
		double sum = 0.0, standardDeviation = 0.0;
		int elements = numArray.length;
		
		for (double num : numArray) {
			sum += num;
		}

		double mean = sum / elements;

		for (double num : numArray) {
			standardDeviation += Math.pow(num - mean, 2);
		}

		return Math.sqrt(standardDeviation / elements);
	}
	
	
	public static Complex[] double2Complex(double x[]) {
		
		int N = x.length;
		Complex[] res = new Complex[N];
		
		for(int i=0; i<N; i++) {
			
			res[i] = new Complex(x[i], 0);
			
		}
		
		return res;	
		
	}
	
	public static double[][] vercat(double[][] a, double[][] b){
		
		int filas_a = a.length;
		int col_a = a[0].length;
		int filas_b = b.length;
		int col_b = b[0].length;
		
		double[][] result = new double[filas_a + filas_b][];
		
		if(col_a == col_b) {
			
			System.arraycopy(a, 0, result, 0, a.length);
			System.arraycopy(b, 0, result, a.length, b.length);
						
		}else {
			System.out.println("Error de dimensiones al hacer horzcat.");
		}
		
		
		return result;
		
		
	}
	
	public static double[][] vercat(double[] a, double[] b){
		
		int filas_a = 1;
		int filas_b = 1;
		
		double[][] result = new double[filas_a + filas_b][];
		
		if(a.length == b.length) {
			
			result[0] = a;
			result[1] = b;
						
		}else {
			System.out.println("Error de dimensiones al hacer horzcat.");
		}
		
		
		return result;
		
		
	}

	
	
	public static double[][] trasposeMatrix(double[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;

		double[][] trasposedMatrix = new double[n][m];

		for (int x = 0; x < n; x++) {
			for (int y = 0; y < m; y++) {
				trasposedMatrix[x][y] = matrix[y][x];
			}
		}

		return trasposedMatrix;
	}


	public static int min(int[] dimensions) {
		
		int min = 9999999;
		
		for(int i=0; i<dimensions.length; i++){
			
			if(dimensions[i]< min) {
				min = dimensions[i];
			}
			
		}
		
		
		return min;
	}
	
	public static double[][] adjustColDimension(double[][] matrix, int dim) {
		
		double adjustMatrix[][] = new double[matrix.length][dim];
		
		for(int i=0; i<matrix.length; i++) {
			
			adjustMatrix[i] = Arrays.copyOfRange(matrix[i], 0, dim);
			
		}
		
		
		return adjustMatrix;
	}
	
	public static double[] adjustColDimension(double[] vector, int dim) {
		
		double adjustVector[] = new double[dim];
		
		adjustVector = Arrays.copyOfRange(vector, 0, dim);

		
		
		return adjustVector;
	}
	
	public static int[] adjustColDimension(int[] vector, int dim) {
		
		int adjustVector[] = new int[dim];
		
		adjustVector = Arrays.copyOfRange(vector, 0, dim);

		
		
		return adjustVector;
	}


	public static double[][] normalizeMatrix(double[][] features, double[] meanTrain, double[] stdTrain) {

		double[][] normalized = new double[features.length][features[0].length];
		
		for(int i=0; i<features[0].length; i++) {
					
			for(int j=0; j<features.length; j++) {
					
				normalized[j][i] = (features[j][i]-meanTrain[j])/stdTrain[j];
			
			}
			
		}
		
		return normalized;
		
		
	}



	

}
