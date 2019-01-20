package test;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.sound.sampled.UnsupportedAudioFileException;


public class User {
	
	//=======================================================================================
	// User properties
	//=======================================================================================

	private String numTest; 
	private String SNR;
	
	//=======================================================================================
	// User files associated with each instant.
	//=======================================================================================
	
	private UserFile userfile;

	/**
	 * 
	 * @param id
	 * @param set
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * @throws URISyntaxException 
	 */
	
	public User(String numTest, String snr, boolean ss) throws UnsupportedAudioFileException, IOException, URISyntaxException {
		
		this.numTest = numTest;
		this.SNR = snr;

		//=======================================================================================
		// Generating paths and UserFiles for each instant
		//=======================================================================================

		userfile = new UserFile(numTest, SNR, ss);

		
	}
	
	//=======================================================================================
	// Getters and Setters
	//=======================================================================================

	public String getNumTest() {
		return numTest;
	}

	public void setNumTest(String numTest) {
		this.numTest = numTest;
	}

	public UserFile getUserfile() {
		return userfile;
	}

	public void setUserfile(UserFile userfile) {
		this.userfile = userfile;
	}
	

}
