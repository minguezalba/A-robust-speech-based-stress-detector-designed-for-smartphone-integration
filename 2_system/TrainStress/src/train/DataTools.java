package train;

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
	
	public static String[] getSetID(String setname) {
		
		String[] setID = null;
				
		if(setname.equals("set1")) {
			
			String[] setID1 = {"62963719", "652033332", "935941053", "1015666824", "1395228143", 
					"1420900415", "1739028311", "1777769661", "2054751935"};
			
		
			// Dan problemas:
			// "1397020749": Toma toda la señal como si fuera silencio porque los 150ms primeros son ya de voz
			
			setID = setID1;
			
		}else if(setname.equals("set2")) {
			
			
			String[] setID2 = {"12782919", "304102792", "513604950", "852630991", 
					"902398068", "1686645257", "1756953694"};
			
			// Falta adaptar el código para los audios más largos de 15 minutos
			/*
			String[] setID2 = {"12782919", "49425811", "92305089", "304102792", "334844205", "513604950", "852630991", 
					"902398068", "1143102813", "1458206716", "1626125349", "1686645257", "1756953694", "1777108864"};
			
			*/
			setID = setID2;
			
		}
		
		return setID;
		
	}
	
	public static double[] CSV2Array(String fileCSV) throws IOException {
		//Build reader instance
	      //Read data.csv
	      //Default seperator is comma
	      //Default quote character is double quote
	      //Start reading from line number 2 (line numbers start from zero)
	      CSVReader reader = new CSVReader(new FileReader(fileCSV), ',' , '"' , 1);
	       
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
