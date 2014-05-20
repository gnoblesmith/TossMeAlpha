package net.gnosm.accelerometer2;

public class LowPass {
	private double alpha;
	private double out;
	
	public LowPass (double a) {
		alpha = a;
	}
	
	public double filter (double in) {
		out = out*(1-alpha) + alpha * in;
		return out;
	}
	
	
}
