package net.gnosm.accelerometer2;

public class Averager {
	private long numSamples;
	private double avg;
	
	public Averager () {
		numSamples = 0;
		avg = 0.0;
	}
	
	public double average (double samp) {
		avg = (avg * numSamples + samp) / (numSamples + 1);
		numSamples++;
		return avg;
	}
	
	public void reset () {
		numSamples = 0;
		avg = 0.0;
	}
	
	public double getAvg () {
		return avg;
	}
}
