package net.gnosm.accelerometer2;

import android.hardware.SensorEvent;

public class FallingState {
	private int countNeeded;
	private RestDetector rd;
	private int count;
	
	
	public enum fallingState {
		FALLING,
		REST,
		RISING
	};
	
	public FallingState (int count)
	{
		countNeeded = count;
		rd = new RestDetector(.25);
		count = 0;
	}
	
	// eh this is messy as shit
	public void addData(SensorEvent event, double in) {
		rd.addData(event);
		if (in < 0) {
			count++;
			if (count >= countNeeded) count = countNeeded;
		} else if (in > 0) {
			count--;
			if (count <= -countNeeded) count = -countNeeded;
		}
	}
	
	public void reset() {
		count = 0;
		rd.reset();
	}
	
	public fallingState getState() {
		if (rd.atRest()) {
			return fallingState.REST;
		} else {
			if (count <= -countNeeded) {
				return fallingState.RISING;
			} else if (count >= countNeeded) {
				return fallingState.FALLING;
			} else {
				return fallingState.REST;
			}
		}
	}
}
