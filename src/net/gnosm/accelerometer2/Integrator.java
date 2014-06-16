package net.gnosm.accelerometer2;

public class Integrator {
	private double lastT;
	private double lastVal;
	
	private boolean firstSample;
	public double out;
	
	public Integrator () {
		out = 0.0;
		firstSample = true;
	}
	
	public double integrate (double dt, double val) {
		if (!firstSample) {
			out += (val + lastVal)*dt/2; // trapezoidal approximation
		} else {
			firstSample = false;
		}
		lastVal = val;
		
		return out;
	}
	
	public void reset () {
		firstSample = true;
		out = 0.0;
	}
}
