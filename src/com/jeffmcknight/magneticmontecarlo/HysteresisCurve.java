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
	RecordPoint[] mRecordPoints;

	//******************** HysteresisCurve() -- Constructor ********************
	/**
	 * Constructor - three-parameter
	 * @param hMin - the minimum applied DC field (Hdc)
	 * @param hMax - the maximum applied DC field (Hdc)
	 * @param stepSize
	 */
	public HysteresisCurve(float hMin, float hMax, float stepSize) {
		int pointCount = (int)((hMax-hMin)/stepSize);
		mRecordPoints = new RecordPoint[pointCount + 1] ;
		for (int i = 0; i < mRecordPoints.length; i++)
		{
			float appliedH = (stepSize*i)  + hMin;
			mRecordPoints[i] = new RecordPoint(appliedH);
		}
	}
	
	//******************** getDipoleSet() ********************
	public RecordPoint[] getRecordPoints()
	{
		return mRecordPoints;
	}

	//******************** getDipole() ********************
	public RecordPoint getDipole(int i) 
	{
		return mRecordPoints[i];
	}

	//******************** getH() ********************
	public float getH(int i) 
	{
		return mRecordPoints[i].getH();
	}	

	//******************** getLength() ********************
	public int getLength() 
	{
		return mRecordPoints.length;
	}

//	//******************** getM() ********************
//	public float getM(int i) 
//	{
//		return dipoleSet[i].getM();
//	}	

	//******************** HysteresisCurve() ********************
	public void generateCurve(MagneticMedia magneticMedia) 
	{
		for (int i = 0; i < mRecordPoints.length; i++)
		{
			mRecordPoints[i].setM( magneticMedia.recordWithAcBias(mRecordPoints[i].getH()) );
			System.out.print("."); // show progress
		}
	} 	// END ******************** HysteresisCurve() ********************
	
} // END ******************** class - HysteresisCurve ********************

