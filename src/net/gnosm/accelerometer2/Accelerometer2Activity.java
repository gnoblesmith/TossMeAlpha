package net.gnosm.accelerometer2;

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
import com.jjoe64.graphview.LineGraphView;


public class Accelerometer2Activity extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAcc;
	
	Integrator xAccInteg;
	Integrator xVelInteg;
	
	Integrator yAccInteg;
	Integrator yVelInteg;
	
	Integrator zAccInteg;
	Integrator zVelInteg;
	
	Averager xAccAvg;
	Averager yAccAvg;
	Averager zAccAvg;
	
	LowPass xlp;
	LowPass ylp;
	LowPass zlp;
	
	GraphView gView;
	GraphViewSeries gViewSeries;
	private boolean firstGraphDraw;
	
	private MyGraphViewData[] calibSamples;
	
	private boolean calibrateState;
	private long numSamplesCalib;
	
	TextView debugTV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accelerometer2);
		
		debugTV = (TextView) findViewById(R.id.debugTV);
		calibrateState = false;
		xAccInteg = new Integrator();
		yAccInteg = new Integrator();
		zAccInteg = new Integrator();
		
		xAccAvg = new Averager();
		yAccAvg = new Averager();
		zAccAvg = new Averager();
		
		xlp = new LowPass(.95);
		ylp = new LowPass(.95);
		zlp = new LowPass(.95);
		
		calibSamples = new MyGraphViewData[100];
		firstGraphDraw = true;
        final OnClickListener resetButtonListener = new OnClickListener() {
        	public void onClick(View v) {
        		xAccInteg.reset();
        		yAccInteg.reset();
        		zAccInteg.reset();
        	}
        };
        
        final Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(resetButtonListener);
		
        final OnClickListener calibrateButtonListener = new OnClickListener() {
        	public void onClick(View v) {
        		numSamplesCalib = 0;
        		xAccAvg.reset();
        		yAccAvg.reset();
        		zAccAvg.reset();
        		calibrateState = true;
        	}
        };
        
        final Button calibrateButton = (Button) findViewById(R.id.calibrateButton);
        calibrateButton.setOnClickListener(calibrateButtonListener);
		
   
        
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		// what to do here
	}
	
	@Override
	public final void onSensorChanged(SensorEvent event) {
		if (calibrateState) {
			calibrateRoutine(event);
		} else {
			normalRoutine(event);
		}
	}
	
	public final void calibrateRoutine(SensorEvent event) {
		double xAcc = event.values[0];
		double yAcc = event.values[1];
		double zAcc = event.values[2];
		xAccAvg.average(xAcc);
		yAccAvg.average(yAcc);
		zAccAvg.average(zAcc);
		
		calibSamples[(int) numSamplesCalib] = new MyGraphViewData(numSamplesCalib+1, xAcc);
		
		numSamplesCalib++;
		
		if (numSamplesCalib >= 100) {
			numSamplesCalib = 0;
			calibrateState = false;

			TextView xxx = (TextView) findViewById(R.id.xpos);
			TextView yyy = (TextView) findViewById(R.id.ypos);
			TextView zzz = (TextView) findViewById(R.id.zpos);
			xxx.setText(String.valueOf(xAccAvg.getAvg()));
			yyy.setText(String.valueOf(yAccAvg.getAvg()));
			zzz.setText(String.valueOf(zAccAvg.getAvg()));
			
			if (firstGraphDraw) {
				firstGraphDraw = false;
		        gViewSeries = new GraphViewSeries(calibSamples);
			} else {
				gViewSeries.resetData(calibSamples);
			}
	        gView = new LineGraphView(this, "Amaze");
	        gView.getGraphViewStyle().setVerticalLabelsColor(Color.RED);
	        gView.addSeries(gViewSeries);
	        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
	        layout.addView(gView);     
		}
		
		debugTV.setText(String.valueOf(numSamplesCalib));
		
	}
		
		
	public final void normalRoutine(SensorEvent event) {
		double thisTime = ((double)SystemClock.elapsedRealtime())/1000;
		double xAcc = xlp.filter(event.values[0]) - xAccAvg.getAvg();
		double yAcc = ylp.filter(event.values[1]) - yAccAvg.getAvg();
		double zAcc = zlp.filter(event.values[2]) - zAccAvg.getAvg();

		TextView tvXA = (TextView) findViewById(R.id.xacc);
		tvXA.setText(String.valueOf(xAcc));
	
		TextView tvYA = (TextView) findViewById(R.id.yacc);
		tvYA.setText(String.valueOf(yAcc));
		
		TextView tvZA = (TextView) findViewById(R.id.zacc);
		tvZA.setText(String.valueOf(zAcc));

		double xVel = xAccInteg.integrate(thisTime, xAcc);
		double yVel = yAccInteg.integrate(thisTime, yAcc);
		double zVel = zAccInteg.integrate(thisTime, zAcc);
		
		TextView tvXV = (TextView) findViewById(R.id.xvel);
		tvXV.setText(String.valueOf(xVel));
	
		TextView tvYV = (TextView) findViewById(R.id.yvel);
		tvYV.setText(String.valueOf(yVel));
		
		TextView tvZV = (TextView) findViewById(R.id.zvel);
		tvZV.setText(String.valueOf(zVel));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL); // fixme different speeds?
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
}
