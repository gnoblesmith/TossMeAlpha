package net.gnosm.accelerometer2;
import com.jjoe64.graphview.GraphViewDataInterface;


public class MyGraphViewData implements GraphViewDataInterface {

	private double x,y;
	
	public MyGraphViewData(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public MyGraphViewData(MyGraphViewData other) {
		this.x = other.x;
		this.y = other.y;
	}
	
	@Override
	public double getX() {
		return x;
	}
	
	@Override
	public double getY() {
		return y;
	}
}
