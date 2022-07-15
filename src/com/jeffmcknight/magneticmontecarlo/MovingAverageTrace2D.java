package com.jeffmcknight.magneticmontecarlo;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import java.awt.Color;
import java.util.Calendar;

public class MovingAverageTrace2D {
	int mMovingAveragePeriod;
	private ITrace2D mTrace;

	MovingAverageTrace2D(int movingAveragePeriod, Color traceColor){
		this(movingAveragePeriod, "", traceColor);
	}

	MovingAverageTrace2D(String traceName, Color traceColor){
		this(0, traceName, traceColor);
	}

	MovingAverageTrace2D(int movingAveragePeriod, String traceName, Color traceColor){
		mMovingAveragePeriod = movingAveragePeriod;
		// Set trace properties (name, color, point shape to disc) 
		setTrace(new Trace2DSimple()); 
		getTrace().setName(traceName);
		getTrace().setColor(traceColor);
		getTrace().setTracePainter(new TracePainterDisc());
	}

	/**
	 * @param movingAveragePeriod
	 * @param trace
	 * @param magneticMedia TODO
	 */
	public void generateMovingAverage(MagneticMedia magneticMedia) {
		// Calculate central moving average for each dipole.
	    for(int i=0; i<magneticMedia.size(); i++)
	  	{
	    	float intermediateNetM = 0;
	    	int dipoleCount = 0;
	    	int lowOffset = (mMovingAveragePeriod+1)/2;
	    	int highOffset = mMovingAveragePeriod/2;
	    	// Set lower bound of iteration at 0 for cumulative average if . 
	    	int lowerBound = (mMovingAveragePeriod==0) ? 0 : Math.max(0, i-lowOffset);
	    	for (int j=lowerBound; j<Math.min(magneticMedia.size(),i+highOffset); j++){
	    		intermediateNetM += magneticMedia.get(j).getM();
	    		dipoleCount++;
	    	}
//	    	System.out.println(TAG + "generateMovingAverage()" 
//	    			+ "i: " + i 
//	    			+ "\t -- intermediateNetM: " + intermediateNetM 
//	    			+ "\t -- get(i).getM(): " + magneticMedia.get(i).getM()
//	    			+ "\t -- dipoleCount: " + dipoleCount
//	    			);
	  		mTrace.addPoint(i, intermediateNetM/dipoleCount);
	  	}
	}
	
	
	public void generateScaledTotal(MagneticMedia magneticCube) {
		// Calculate scaled total at each dipole.
		double runningTotal = 0.0;
	    for(int i=0; i<magneticCube.size(); i++)
	  	{
	    	runningTotal += magneticCube.get(i).getM();
//	    	System.out.println(TAG + "\t -- generateScaledTotal()"
//	    			+ "i: " + i 
//	    			+ "\t -- get(i).getM(): " + magneticCube.get(i).getM()
//	    			+ "\t -- runningTotal: " + runningTotal
//	    			);
	  		mTrace.addPoint(i, runningTotal/magneticCube.size());
	  	}
	}

	/**
	 * @param title 
	 * @param count TODO
	 * @param fastAveragePeriod
	 * @param magneticMedia TODO
	 * @return
	 */
	public String buildTraceName(
			String title, 
			MagneticMedia magneticMedia) {
		Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(System.currentTimeMillis());
	    String traceName = new StringBuilder(title)
	    .append(" : ")
	    .append(magneticMedia.getXCount())
	    .append("x")
	    .append(magneticMedia.getYCount())
	    .append("x")
	    .append(magneticMedia.getZCount())
	    .append(" - Packing Fraction: ")
	    .append(magneticMedia.getPackingFraction())
	    .append(" - Radius [um]: ")
	    .append(magneticMedia.getDipoleRadius())
	    .append(" - Date: ")
	    .append(calendar.get(Calendar.YEAR))
	    .append("-")
	    .append(calendar.get(Calendar.MONTH))
	    .append("-")
	    .append(calendar.get(Calendar.DATE))
	    .append(" T")
	    .append(calendar.get(Calendar.HOUR_OF_DAY))
	    .append(":")
	    .append(calendar.get(Calendar.MINUTE))
	    .append(":")
	    .append(calendar.get(Calendar.SECOND))
	    .append(" -- ")
	    .append(chartDescription(mMovingAveragePeriod))
	    .toString();
	    mTrace.setName(traceName);
		return traceName;
	}

	private String chartDescription(int fastAveragePeriod) {
		String string;
		switch (fastAveragePeriod) {
		case 0:
			string = "Cumulative Average";
			break;
		case -1:
			string = "Total (Normalized to " + MonteCarloHysteresisPanel.SATURATION_M + ")";
			break;
		default:
			string = "Moving Average over " + fastAveragePeriod + " dipoles"; 
			break;
		}
		return string;
	}

	public ITrace2D getTrace() {
		return mTrace;
	}

	public void setTrace(ITrace2D trace) {
		mTrace = trace;
	}
}
