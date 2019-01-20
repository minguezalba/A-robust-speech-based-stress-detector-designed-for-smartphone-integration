package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class DataTools {
	
	
	public static double[] importUniCSVToArray(String fileCSV, int N) throws IOException {
		
		//N = numbers of lines to skip (N=1 if there are headers)
		//Build reader instance
	      //Read data.csv
	      //Default seperator is comma
	      //Default quote character is double quote
	      //Start reading from line number 2 (line numbers start from zero)
	      CSVReader reader = new CSVReader(new FileReader(fileCSV), ',' , '"' , N);
	       
	      //Read CSV line by line and use the string array as you want
	      String[] nextLine;
	      List<Double> HRlist = new ArrayList<Double>();
	      
	      while ((nextLine = reader.readNext()) != null) {
	         if (nextLine != null) {
	            //Verifying the read data here
	        	 HRlist.add(Double.parseDouble(nextLine[0]));
	         }
	       }
	      
	      		
	      double[] HR = new double[HRlist.size()];
	      for (int i=0; i < HR.length; i++)
	      {
	    	  HR[i] = HRlist.get(i).doubleValue();
	      }
	      
	      reader.close();
	      
	      return HR;
	}
	
	@SuppressWarnings("resource")
	public static double[][] importMultiCSVToArray(String fileCSV, int N) throws IOException {
		
		
		//N = numbers of lines to skip (N=1 if there are headers)
		//Build reader instance
		// Read data.csv
		// Default seperator is comma
		// Default quote character is double quote
		// Start reading from line number 2 (line numbers start from zero)
		
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(fileCSV), ',', '"', N);
		}catch(FileNotFoundException e) {
			System.out.println("No file found");
			return null;
		}

		 List<String[]> lines = reader.readAll();
		 
		 String[][] result = lines.toArray(new String[lines.size()][]);
		 
		 
		 double data[][] = new double[result.length][result[0].length];
		 
		 for(int i=0; i<result.length; i++) {
			 for(int j=0; j<result[0].length; j++) {
				 data[i][j] = Double.parseDouble(result[i][j]);
			 }
		 }
		 
		 return data;
		
	}

    public static void exportMultiDataToCSV(String fileName, double[][] data) throws FileNotFoundException, IOException
    {
        File file = new File(fileName);
        if (!file.isFile())
            file.createNewFile();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(file));

        int rowCount = data.length;

        for (int i = 0; i < rowCount; i++)
        {
            int columnCount = data[i].length;
            String[] values = new String[columnCount];
            for (int j = 0; j < columnCount; j++)
            {
                values[j] = data[i][j] + "";
            }
            csvWriter.writeNext(values);
        }

        csvWriter.flush();
        csvWriter.close();
    }
    
   // exportUniDataToCSV funciona, pero al guardar las señales genera archivos demasiado pesados. Sale mas rentable con ficheros SER.
    
    public static void exportUniDataToCSV(String fileName, int[] data) throws FileNotFoundException, IOException
    {
        File file = new File(fileName);
        if (!file.isFile())
            file.createNewFile();

        CSVWriter csvWriter = new CSVWriter(new FileWriter(file,false),',','\0');
        
                
        int size = data.length;
        String[] str = new String[size];
     
        for(int i=0; i<size; i++) {
           str[i] = String.valueOf(data[i]);
         }

        csvWriter.writeNext(str);
        
        csvWriter.flush();
        csvWriter.close();
    }
    
    
	public static void generateSERfile(double[] variable, String filename) throws FileNotFoundException, IOException {
		
		FileOutputStream fos;
		ObjectOutputStream oos;
		
		oos = new ObjectOutputStream(fos = new FileOutputStream(filename));
		oos.writeObject(variable);
		oos.flush();
		oos.close();
		fos.close();	
		
	}
	
	public static void generateSERfile(int[] variable, String filename) throws FileNotFoundException, IOException {
		
		FileOutputStream fos;
		ObjectOutputStream oos;
		
		oos = new ObjectOutputStream(fos = new FileOutputStream(filename));
		oos.writeObject(variable);
		oos.flush();
		oos.close();
		fos.close();	
		
	}
	
	

}
