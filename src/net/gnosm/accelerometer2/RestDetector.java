package net.gnosm.accelerometer2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class RestDetector {
	private double errorBuffer;
	
	private double xAcc;
	private double yAcc;
	private double zAcc;
	
	private double xGrav;
	private double yGrav;
	private double zGrav;
	
	private boolean haveAcc;
	private boolean haveGrav;
	
	public RestDetector (double _errorBuffer) {
		errorBuffer = _errorBuffer;
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
	private double x, y, z;
	public boolean atRest () {
		if (!(haveGrav && haveAcc)) {
			return false;
		} else {
			x = Math.abs(xAcc/xGrav);
			y = Math.abs(yAcc/yGrav);
			z = Math.abs(zAcc/zGrav);
			if (
				((1-errorBuffer) < x) && (x < (1+errorBuffer)) &&
				((1-errorBuffer) < y) && (y < (1+errorBuffer)) &&
				((1-errorBuffer) < z) && (z < (1+errorBuffer)) ) {
				return true;
			} else {
				return false;
			}
		}
	}
	
}
