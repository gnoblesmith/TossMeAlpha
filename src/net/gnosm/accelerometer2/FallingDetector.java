package net.gnosm.accelerometer2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class FallingDetector {
	private double xAcc;
	private double yAcc;
	private double zAcc;
	
	private double xGrav;
	private double yGrav;
	private double zGrav;
	
	private boolean haveAcc;
	private boolean haveGrav;
	
	public FallingDetector () {
		haveAcc = false;
		haveGrav = false;
	}
	
	public void reset () {
		haveAcc = false;
		haveGrav = false;
	}
	
	public void addData (SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			haveAcc = true;
			xAcc = event.values[0];
			yAcc = event.values[1];
			zAcc = event.values[2];
		} else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			haveGrav = true;
			xGrav = event.values[0];
			yGrav = event.values[1];
			zGrav = event.values[2];
		}
	}
	
	// gravity sensor should be roughly the same as the accelerometer
	// when we're at rest
	public boolean falling () {
		return false; // iunno
	}

}
