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
	RecordPoint[] dipoleSet;

	//******************** HysteresisCurve() -- Constructor ********************
	/**
	 * Constructor - three-parameter
	 * @param hMin - the minimum applied DC field (Hdc)
	 * @param hMax - the maximum applied DC field (Hdc)
	 * @param stepSize
	 */
	public HysteresisCurve(float hMin, float hMax, float stepSize) {
		int pointCount = (int)((hMax-hMin)/stepSize);
		dipoleSet = new RecordPoint[pointCount + 1] ;
		for (int i = 0; i < dipoleSet.length; i++) 
		{
			float appliedH = (stepSize*i)  + hMin;
			dipoleSet[i] = new RecordPoint(appliedH);
		}
	}
	
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
		return dipoleSet.length;
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

