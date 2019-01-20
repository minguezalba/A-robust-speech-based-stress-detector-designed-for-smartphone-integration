package test;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;

public class PitchDetector implements PitchDetectionHandler{
	
	 @Override
	 public void handlePitch(PitchDetectionResult pitchDetectionResult,
	         AudioEvent audioEvent) {
		 
	     if(pitchDetectionResult.getPitch() != -1){
	    	 
	         //double timeStamp = audioEvent.getTimeStamp();
	         float pitch = pitchDetectionResult.getPitch();
	         //float probability = pitchDetectionResult.getProbability();
	         //double rms = audioEvent.getRMS() * 100;
	         //String message = String.format("Pitch detected at %.2fs: %.2fHz ( %.2f probability, RMS: %.5f )\n", timeStamp,pitch,probability,rms);
	         System.out.println(pitch);
	         
	         //this.(timeStamp,pitch,probability,rms);
	     }
	     
	 }
	 

	
}
