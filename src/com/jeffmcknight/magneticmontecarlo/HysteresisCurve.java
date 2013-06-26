/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

//******************** class - HysteresisCurve ********************
/**
 * @author jeffmcknight
 *
 */
public class HysteresisCurve 
{
	int length;
	RecordPoint[] dipoleSet;

	//******************** HysteresisCurve() ********************
	/**
	 * 
	 */
	public HysteresisCurve(float hMax, float stepSize) 
	{
		int positivePointCount = (int)(hMax/stepSize);
		float appliedH;
		length = ( (2*positivePointCount)+1 );
		dipoleSet = new RecordPoint[length] ;
		for (int i = 0; i < dipoleSet.length; i++) 
		{
			appliedH = stepSize*(i - positivePointCount);
			dipoleSet[i] = new RecordPoint(appliedH);
//			dipoleSet[i].setH(stepSize*(i - positivePointCount));
		}
	} // END ******************** HysteresisCurve() ********************
	
	//******************** getDipoleSet() ********************
	public RecordPoint[] getDipoleSet() 
	{
		return dipoleSet;
	}

	//******************** getDipole() ********************
	public RecordPoint getDipole(int i) 
	{
		return dipoleSet[i];
	}

	//******************** getH() ********************
	public float getH(int i) 
	{
		return dipoleSet[i].getH();
	}	

	//******************** getLength() ********************
	public int getLength() 
	{
		return length;
	}

	//******************** setLength() ********************
	public void setLength(int length) 
	{
		this.length = length;
	}

//	//******************** getM() ********************
//	public float getM(int i) 
//	{
//		return dipoleSet[i].getM();
//	}	

	//******************** HysteresisCurve() ********************
	public void generateCurve(MagneticMedia magneticMedia) 
	{
		for (int i = 0; i < dipoleSet.length; i++) 
		{
			dipoleSet[i].setM( magneticMedia.recordToM(dipoleSet[i].getH()) );	
			System.out.print("."); // show progress
		}
	} 	// END ******************** HysteresisCurve() ********************
	
} // END ******************** class - HysteresisCurve ********************

