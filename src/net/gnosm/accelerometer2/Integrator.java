package net.gnosm.accelerometer2;

public class Integrator {
	private double lastT;
	private double lastVal;
	
	private boolean firstSample;
	private double out;
	
	public Integrator () {
		out = 0.0;
		firstSample = true;
	}
	
	public double integrate (double t, double val) {
		if (!firstSample) {
			out += (val + lastVal)/2 * (t - lastT); // trapezoidal approximation
		} else {
			firstSample = false;
		}
		lastVal = val;
		lastT = t;
		
		return out;
	}
	
	public void reset () {
		firstSample = true;
		out = 0.0;
	}
}
