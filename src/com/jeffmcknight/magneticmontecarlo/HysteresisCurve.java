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
	RecordPoint[] mRecordPoint;

	//******************** HysteresisCurve() -- Constructor ********************
	/**
	 * Constructor - three-parameter
	 * @param hMin - the minimum applied DC field (Hdc)
	 * @param hMax - the maximum applied DC field (Hdc)
	 * @param stepSize
	 */
	public HysteresisCurve(float hMin, float hMax, float stepSize) {
		int pointCount = (int)((hMax-hMin)/stepSize);
		mRecordPoint = new RecordPoint[pointCount + 1] ;
		for (int i = 0; i < mRecordPoint.length; i++) 
		{
			float appliedH = (stepSize*i)  + hMin;
			mRecordPoint[i] = new RecordPoint(appliedH);
		}
	}
	
	//******************** getDipoleSet() ********************
	public RecordPoint[] getDipoleSet() 
	{
		return mRecordPoint;
	}

	//******************** getDipole() ********************
	public RecordPoint getDipole(int i) 
	{
		return mRecordPoint[i];
	}

	//******************** getH() ********************
	public float getH(int i) 
	{
		return mRecordPoint[i].getH();
	}	

	//******************** getLength() ********************
	public int getLength() 
	{
		return mRecordPoint.length;
	}

//	//******************** getM() ********************
//	public float getM(int i) 
//	{
//		return dipoleSet[i].getM();
//	}	

	//******************** HysteresisCurve() ********************
	public void generateCurve(MagneticMedia magneticMedia) 
	{
		for (int i = 0; i < mRecordPoint.length; i++) 
		{
			mRecordPoint[i].setM( magneticMedia.recordToM(mRecordPoint[i].getH()) );	
			System.out.print("."); // show progress
		}
	} 	// END ******************** HysteresisCurve() ********************
	
} // END ******************** class - HysteresisCurve ********************

