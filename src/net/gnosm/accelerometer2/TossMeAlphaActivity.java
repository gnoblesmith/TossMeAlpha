package net.gnosm.accelerometer2;

import java.util.ArrayList;
import java.util.List;
import net.gnosm.TossMeAlpha.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;


public class TossMeAlphaActivity extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAcc;
	private Sensor mLinAcc;
	private Sensor mGrav;
	
	private boolean dataGatherState;
	private long numSamples;
	
	TextView debugTV;
	
	private double height;
	
	private double xGrav;
	private double yGrav;
	private double zGrav;
	
	private double xLinAcc;
	private double yLinAcc;
	private double zLinAcc;
	
	FallingState fs;

	private Integrator UDIntegrator; 	// Up-Down Integrator
	private Integrator UDIntegrator2; 	// Up-Down Double Integrator
	
	boolean firstGraphDraw;
	GraphView gView;
	GraphViewSeries gViewSeriesUD;
	GraphViewSeries gViewSeriesUDInt1;
	GraphViewSeries gViewSeriesUDInt2;
	ArrayList<MyGraphViewData> UpDownData;
	ArrayList<MyGraphViewData> UpDownInt1Data;
	ArrayList<MyGraphViewData> UpDownInt2Data;
	
	
	// avgSize must be odd! putting data points in the middle of the averaging window
	// is most accurate, i think
	private MyGraphViewData[] movingAverageFilter(MyGraphViewData[] in, int avgSize) {
		MyGraphViewData[] ret;
		int returnLength = in.length - avgSize + 1; // have to throw out the ends
		ret = new MyGraphViewData[returnLength];
		
		
		for (int i = 0; i < returnLength; i++) {
			int x = i + avgSize/2;
			double y = 0.0;
			
			for (int j = 0; j < avgSize; j++) { // get the windows average
				int ind = x - (avgSize/2 - j);
				y += in[ind].getY();
			}
			y /= avgSize;
			
			ret[i] = new MyGraphViewData(x, y);
		}
		
		return ret;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.toss_me_alpha);
		
		debugTV = (TextView) findViewById(R.id.debugTV);
		dataGatherState = false;
		UDIntegrator  = new Integrator();
		UDIntegrator2 = new Integrator();
		height = 0.0;
		lastTime = 0.0;
		firstGraphDraw = true;
		UpDownData = new ArrayList<MyGraphViewData>();
		UpDownInt1Data = new ArrayList<MyGraphViewData>();
		UpDownInt2Data = new ArrayList<MyGraphViewData>();
		fs = new FallingState(5);
		        
        final OnClickListener goButtonListener = new OnClickListener() {
        	public void onClick(View v) {
        	}
        };
        
        final Button goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(goButtonListener);
	
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLinAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGrav = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	}
	
	private void findAllDevices () {
		// code to find all sensors on device - go into debug and peek at "ListSensorTypes"
		List<Sensor> ListSensor = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		List<String> ListSensorTypes = new ArrayList<String>();
				
		for (int i = 0; i < ListSensor.size(); i++) {
			ListSensorTypes.add(ListSensor.get(i).getName());
		}
		int q;
		q = 0;
		q += 1;
	}
	
	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		// what to do here
	}
	
	private double lastTime = 0.0;
	private boolean lastRestState;
	private int sampleCount = 0;
	private int samplesMax = 100;
	private boolean reallyFalling=false;
	private int reallyFallingCount = 0;
	private int fallCountNeeded = 5;
	@Override
	public final void onSensorChanged(SensorEvent event) {
		double thisTime = ((double)SystemClock.elapsedRealtime())/1000.0;
		gatherData(event);
		
		if (!(lastTime == 0.0)) {
			double dt = thisTime - lastTime;
			fs.addData(event, getUpDownAcc());
			
			if (fs.getState() == FallingState.fallingState.FALLING) {
				UDIntegrator.integrate(dt, getUpDownAcc());
				UDIntegrator2.integrate(dt, UDIntegrator.out);
						
				UpDownData.add(new MyGraphViewData(sampleCount + 1, getUpDownAcc()));
				UpDownInt1Data.add(new MyGraphViewData(sampleCount + 1, UDIntegrator.out));
				UpDownInt2Data.add(new MyGraphViewData(sampleCount + 1, UDIntegrator2.out));
				sampleCount++;

			} else {
				if (sampleCount < 100) { // if we didn't get enough samples to be convincing
					UDIntegrator.reset();
					UDIntegrator2.reset();
					UpDownData.clear();
					UpDownInt1Data.clear();
					UpDownInt2Data.clear();
					sampleCount = 0;
				} else {
					if (firstGraphDraw) {
						firstGraphDraw = false;
						gViewSeriesUD = new GraphViewSeries("UD Raw", new GraphViewSeriesStyle(Color.BLACK, 3), listToArray(UpDownData));
						gViewSeriesUDInt1 = new GraphViewSeries("UD Int 1", new GraphViewSeriesStyle(Color.RED, 3), listToArray(UpDownInt1Data));
						gViewSeriesUDInt2 = new GraphViewSeries("UD Int 2", new GraphViewSeriesStyle(Color.BLUE, 3), listToArray(UpDownInt2Data));
					} else {
						gViewSeriesUD.resetData(listToArray(UpDownData));
						gViewSeriesUDInt1.resetData(listToArray(UpDownInt1Data));
						gViewSeriesUDInt2.resetData(listToArray(UpDownInt2Data));
					}
					gView = new LineGraphView(this, "Amaze");
					gView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
					gView.addSeries(gViewSeriesUD);
					gView.addSeries(gViewSeriesUDInt1);
					gView.addSeries(gViewSeriesUDInt2);
					LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
					layout.addView(gView);  
				}
			}
		}
		
		lastTime = thisTime;
	}
	
	private void gatherData(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			xGrav = event.values[0];
			yGrav = event.values[1];
			zGrav = event.values[2];
		} else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			xLinAcc = event.values[0];
			yLinAcc = event.values[1];
			zLinAcc = event.values[2];
		} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			debugTV.setText("finally");
		}
	}
	
	private MyGraphViewData[] listToArray(ArrayList<MyGraphViewData> in) {
		MyGraphViewData[] ret;
		ret = new MyGraphViewData[in.size()];
		
		for (int i = 0; i < in.size(); i++) {
			ret[i] = new MyGraphViewData(in.get(i));
		}
		
		return ret;
	}
	
	// this function gets the projection of the linear accelerometer vector onto
	// the gravity vector. ie. the magnitude of the linear accelerometer in the direction
	// of the gravity vector (which we hope is downwards). some calc 3 shit
	private double getUpDownAcc () {
		// dot-product
		double dp = xLinAcc * xGrav + yLinAcc * yGrav + zLinAcc * zGrav;
		
		// divide by magnitude of gravity vector
		return dp / Math.sqrt((xGrav*xGrav + yGrav*yGrav + zGrav*zGrav));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mLinAcc, SensorManager.SENSOR_DELAY_FASTEST); 
		mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST); 
		mSensorManager.registerListener(this, mGrav, SensorManager.SENSOR_DELAY_FASTEST); 
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
}
