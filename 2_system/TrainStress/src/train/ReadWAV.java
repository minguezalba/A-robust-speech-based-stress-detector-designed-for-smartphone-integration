package train;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class ReadWAV {
	
	public static double[] loadFile(String filename) throws UnsupportedAudioFileException, IOException {
		
		AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
		AudioFormat originalFormat = stream.getFormat();
		
		/* public AudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian)
		Constructs an AudioFormat with a linear PCM encoding and the given parameters. The frame size is set to the number of bytes required to contain one sample from each channel, and the frame rate is set to the sample rate.
		Parameters:
		sampleRate - the number of samples per second
		sampleSizeInBits - the number of bits in each sample
		channels - the number of channels (1 for mono, 2 for stereo, and so on)
		signed - indicates whether the data is signed or unsigned
		bigEndian - indicates whether the data for a single sample is stored in big-endian byte order (false means little-endian)
	 */
        
        boolean endian; 
        if(originalFormat.isBigEndian()) {
        	endian = true;
        }else {
        	endian = false;
        }
        
        boolean signed = true;
        if(originalFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
        	signed = true;
        }else if(originalFormat.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
        	signed = false;
        }
        		
        //AudioFormat format = new AudioFormat(originalFormat.getSampleRate(), 16, 1, true, true);
        AudioFormat newFormat = new AudioFormat(originalFormat.getSampleRate(), 16, 1, signed, endian);
       
               
        AudioInputStream localIs = null;

        if(!originalFormat.matches(newFormat)) {
            if(AudioSystem.isConversionSupported(newFormat, originalFormat)) {
                localIs = AudioSystem.getAudioInputStream(newFormat, stream);
            } else {
                throw new UnsupportedAudioFileException("Alas, the system could not decode your file type." +
                		"Try converting your file to some PCM 16bit 16000 Hz mono file format using dedicated " +
                		"software. (Hint : http://sox.sourceforge.net/");
            }
        } else {
            localIs = stream;
        }
        
        
        double[] audio = new double[(int)localIs.getFrameLength()];
        byte[] buffer = new byte[8192];
        int bytesRead = 0;
        int offset = 0;
        
        while((bytesRead = localIs.read(buffer)) > -1) {
            int wordCount = (bytesRead / 2) + (bytesRead % 2);
            for (int i = 0; i < wordCount; i++) {
                double d = (double) byteArrayToShort(buffer, 2 * i, newFormat.isBigEndian()) / 32768;
                audio[offset + i] = d;
            }
            offset += wordCount;
        }
        
        return audio;

	}
	
	/*
	 * 
	 * 
	 */
	
	private static short byteArrayToShort(byte[] bytes, int offset, boolean bigEndian) {
        int low, high;
        if (bigEndian) {
            low = bytes[offset + 1];
            high = bytes[offset + 0];
        } else {
            low = bytes[offset + 0];
            high = bytes[offset + 1];
        }
        return (short) ((high << 8) | (0xFF & low));
    }
	
	
	  /**
	   * Saves the double array as an audio file (using .wav or .au format).
	   *
	   * @param filename
	   *            the name of the audio file
	   * @param samples
	   *            the array of samples
	   * @throws IllegalArgumentException
	   *             if unable to save {@code filename}
	   * @throws IllegalArgumentException
	   *             if {@code samples} is {@code null}
	   */
	  public static void save(String filename, double[] samples, int fs) {
	  	if (samples == null) {
	  		throw new IllegalArgumentException("samples[] is null");
	  	}

	  	// assumes 44,100 samples per second
	  	// use 16-bit audio, mono, signed PCM, little Endian
	  	AudioFormat format = new AudioFormat(fs, 16, 1, true, false);
	  	byte[] data = new byte[2 * samples.length];
	  	for (int i = 0; i < samples.length; i++) {
	  		int temp = (short) (samples[i] * 32767); //Por ser double 32,767
	  		data[2 * i + 0] = (byte) temp;
	  		data[2 * i + 1] = (byte) (temp >> 8);
	  	}

	  	// now save the file
	  	try {
	  		ByteArrayInputStream bais = new ByteArrayInputStream(data);
	  		AudioInputStream ais = new AudioInputStream(bais, format, samples.length);
	  		if (filename.endsWith(".wav") || filename.endsWith(".WAV")) {
	  			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));
	  		} else if (filename.endsWith(".au") || filename.endsWith(".AU")) {
	  			AudioSystem.write(ais, AudioFileFormat.Type.AU, new File(filename));
	  		} else {
	  			throw new IllegalArgumentException("unsupported audio format: '" + filename + "'");
	  		}
	  	} catch (IOException ioe) {
	  		throw new IllegalArgumentException("unable to save file '" + filename + "'", ioe);
	  	}
	  }
	  
	  
    /**
     * Normalize gain of the given sample. The given audio buffer is directly modified.
     * @param audioSample the voice sample
     * @param sampleRate the sample rate
     * @return the applied factor (i.e. 1.0 / Math.abs(maxValue))
     */
    public static double[] normalize(double[] audioSample) {

        double max = Double.MIN_VALUE;
        double[] audioSampleNor = new double[audioSample.length];

        for (int i = 0; i < audioSample.length; i++) {
            double abs = Math.abs(audioSample[i]);
            if (abs > max) {
                max = abs;
            }
        }
        if(max > 1.0d) {
            throw new IllegalArgumentException("Expected value for audio are in the range -1.0 <= v <= 1.0 ");
        }

        for (int i = 0; i < audioSample.length; i++) {
        	audioSampleNor[i] = audioSample[i]/max;
        }
        return audioSampleNor;
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
