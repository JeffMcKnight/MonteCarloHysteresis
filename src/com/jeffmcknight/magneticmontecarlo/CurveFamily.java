/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//******************** class - CurveFamily ********************
/**
 * @author jeffmcknight
 *
 */
public class CurveFamily 
{
   public static final String TAG = CurveFamily.class.getSimpleName();
   public static final int DEFAULT_RECORD_POINTS = 50;
	public static final int DEFAULT_RECORD_STEP_SIZE = 2;
	public static final float DEFAULT_DIPOLE_RADIUS = 0.5f;
	public static final float DEFAULT_LATTICE_CONSTANT = 1.0f;
	public static final float DEFAULT_PACKING_FRACTION = 1.0f;
	public static final float SATURATION_M = 100.0f;
	public static final float MAXIMUM_H = 75.0f;
	private static final float MINIMUM_H = 0;

	public int   curveCount;
	public int   numberRecordPoints;
	public int   mCubeEdgeX;
	public int   mCubeEdgeY;
	public int   mCubeEdgeZ;
//	private float defaultDipoleRadius = 0.000001f;
	private float latticeConst = DEFAULT_LATTICE_CONSTANT;
	
	MagneticMedia magneticCube;
	HysteresisCurve[] mhCurveSet;
	HysteresisCurve averageMCurve;
	HysteresisCurve minMCurve;
	HysteresisCurve maxMCurve;
   private float mPackingFraction;
   private float mDipoleRadius;
	
	
//	public static final float defaultIndexA  = 1.0f;
//	float recordedNetMNegative[][] = new float[numberRecordPoints][recordPasses];
//	float recordedNetMPositive[][] = new float[numberRecordPoints][recordPasses];

	//********** constructor - CurveFamily **********
	/**
	 * @param count TODO
	 * 
	 */
	public CurveFamily(int count) 
	{
		numberRecordPoints   = DEFAULT_RECORD_POINTS;
		curveCount = count;
		mCubeEdgeX       = 10;
		mCubeEdgeY       = 10;
		mCubeEdgeZ       = 10;
		magneticCube = new MagneticMedia(mCubeEdgeX, mCubeEdgeY, mCubeEdgeZ, DEFAULT_PACKING_FRACTION, DEFAULT_DIPOLE_RADIUS);
		mhCurveSet = new  HysteresisCurve[count];
		averageMCurve = new HysteresisCurve(MINIMUM_H, MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
	}

	public CurveFamily(int count, int xDim, int yDim, int zDim, float packingFraction, float dipoleRadius) 
	{
		numberRecordPoints   = (int) (2*(MAXIMUM_H/DEFAULT_RECORD_STEP_SIZE) + 1);
		curveCount = count;
		mCubeEdgeX       = xDim;
		mCubeEdgeY       = yDim;
		mCubeEdgeZ       = zDim;
		mPackingFraction = packingFraction;
		mDipoleRadius = dipoleRadius;
		latticeConst = 2f * dipoleRadius / packingFraction ; 
		magneticCube = new MagneticMedia(mCubeEdgeX, mCubeEdgeY, mCubeEdgeZ, packingFraction, dipoleRadius);
		averageMCurve = new HysteresisCurve(MINIMUM_H, MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
		minMCurve     = new HysteresisCurve(MINIMUM_H, MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
		maxMCurve     = new HysteresisCurve(MINIMUM_H, MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
		mhCurveSet = new  HysteresisCurve[curveCount];
		for (int i = 0; i < mhCurveSet.length; i++) 
		{
			mhCurveSet[i] = new HysteresisCurve(MINIMUM_H, MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
		}
	}

   //******************** getDefaultRecordPoints() ********************
	public static int getDefaultRecordPoints() 
	{
		return DEFAULT_RECORD_POINTS;
	}

	/**
	 * @return the averageMCurve
	 */
	protected HysteresisCurve getAverageMCurve()
	{
		return averageMCurve;
	}

	// ******************** getcubeEdgeX() ********************
   // @return cubeEdgeX
   int getCubeEdgeX()
   {
      return mCubeEdgeX;
   }

   // ******************** getcubeEdgeY() ********************
   // @return cubeEdgeY
   int getCubeEdgeY()
   {
      return mCubeEdgeY;
   }

   // ******************** getcubeEdgeZ() ********************
   // @return cubeEdgeZ
   int getCubeEdgeZ()
   {
      return mCubeEdgeZ;
   }

   // ******************** getmDipoleRadius() ********************
   // @return mDipoleRadius
   float getDipoleRadius()
   {
      return mDipoleRadius;
   }

   // ******************** getlatticeConst() ********************
   // @return latticeConst
   float getLatticeConst()
   {
      return latticeConst;
   }

   /**
	 * @return the minMCurve
	 */
	protected HysteresisCurve getMinMCurve()
	{
		return minMCurve;
	}

	/**
	 * @return the maxMCurve
	 */
	protected HysteresisCurve getMaxMCurve()
	{
		return maxMCurve;
	}

	/**
	 * @return the mhCurveSet
	 */
	protected HysteresisCurve[] getMhCurveSet()
	{
		return mhCurveSet;
	}

   // ******************** getmPackingFraction() ********************
   // @return mPackingFraction
   float getPackingFraction()
   {
      return mPackingFraction;
   }

	//******************** getRecordPasses() ********************
	public int getRecordPasses() 
	{
		return curveCount;
	}

	//******************** setRecordPasses() ********************
	public void setRecordPasses(int recordPasses) 
	{
		this.curveCount = recordPasses;
	}

	//******************** setCubeEdge() ********************
	public void setCubeEdgeX(int cubeDim) 
	{
		this.mCubeEdgeX = cubeDim;
	}

	//******************** setCubeEdge() ********************
	public void setCubeEdgeY(int cubeDim) 
	{
		this.mCubeEdgeY = cubeDim;
	}

	//******************** setCubeEdge() ********************
	public void setCubeEdgeZ(int cubeDim) 
	{
		this.mCubeEdgeZ = cubeDim;
	}

	//******************** getNumberRecordPoints() ********************
	public int getNumberRecordPoints() 
	{
		return numberRecordPoints;
	}

	//******************** setNumberRecordPoints() ********************
	public void setNumberRecordPoints(int intNumPoints) 
	{
		numberRecordPoints = intNumPoints;
	}

		//******************** recordMHCurves ********************
		public void recordMHCurves(float[][] recordedNetMNegative, float[][] recordedNetMPositive) 
		{
			float appliedH;
	//		magneticCube.randomizeLattice();
	
	//		PointCharts  mhChart = new PointCharts ();
	//		System.out.println("  H,    M,    M/H");
	
	//		run recording passes for "recordPasses" times
			for (int j = 0; j < curveCount; j++) 
			{
				System.out.println();
				System.out.print("Pass " + j + " ");
				magneticCube.randomizeLattice();
				mhCurveSet[j].generateCurve(magneticCube);
/*				
				int i = 0;
				while (i < numberRecordPoints)
	//			while (recordedNetMPositive[i][j] < MonteCarloHysteresisPanel.saturationM)
				{
					appliedH     = i * DEFAULT_RECORD_STEP_SIZE;
					recordedNetMNegative[i][j] = magneticCube.recordToM(-appliedH);
					recordedNetMPositive[i][j] = magneticCube.recordToM(appliedH);
					System.out.print(".");
					i = i + 1;
				} // while recordedNetM - END
			*/
			} // for (j < recordedNetM.length - END
			generateAverageMCurve();
			generateMinMCurve();
			generateMaxMCurve();
			
		}

		//******************** generateMaxMCurve() ********************
		private void generateMaxMCurve() 
		{
			for (int i = 0; i < mhCurveSet[0].getLength(); i++) 
			{
				float tempM = mhCurveSet[0].getDipole(i).getM();
				for (int j = 0; j < mhCurveSet.length; j++) 
				{
					tempM = (tempM > mhCurveSet[j].getDipole(i).getM()) ? tempM : mhCurveSet[j].getDipole(i).getM(); 
				}
				maxMCurve.getDipole(i).setM(tempM);	
			}
		}

		//******************** generateMinMCurve() ********************
		private void generateMinMCurve() 
		{
			for (int i = 0; i < mhCurveSet[0].getLength(); i++) 
			{
				float tempM = mhCurveSet[0].getDipole(i).getM();
				for (int j = 0; j < mhCurveSet.length; j++) 
				{
					tempM = (tempM < mhCurveSet[j].getDipole(i).getM()) ? tempM : mhCurveSet[j].getDipole(i).getM(); 
				}
				minMCurve.getDipole(i).setM(tempM);	
			}
		}

		//******************** generateAverageM() ********************
		/**
		 * 
		 */
		private void generateAverageMCurve() 
		{
			// iterate over each record point
			for (int i = 0; i < mhCurveSet[0].getLength(); i++) 
			{
				float tempM = 0;
				for (int j = 0; j < mhCurveSet.length; j++) 
				{
					tempM = tempM + mhCurveSet[j].getDipole(i).getM(); 
				}
				averageMCurve.getDipole(i).setM(tempM/mhCurveSet.length);	
			}
		}

//******************** writeCurveToFile() ********************
public void writeCurvesToFile(File destinationDirectory, File destinationFile) 
{
   String fullFileName;
//		float appliedH;
//		String userDirectory;
//		String fileSeparator;
//		String columnHeader;
		

		// Generate the full file path/name to put output csv in 
		// same directory as the app (ie: userDirectory)
//		userDirectory = System.getProperty("user.dir");
//		fileSeparator = System.getProperty("file.separator");
//		fullFileName = destinationDirectory 
//				+ fileSeparator 
//				+ "MHCurve_" 
//				+ Monte_Carlo_Hysteresis_Application.VERSION_SUFFIX 
//				+ "_" 
//				+ System.currentTimeMillis()  
//				+ ".csv";
		fullFileName = (destinationFile.toString().toLowerCase().endsWith(".csv")) 
		      ? destinationFile.toString() 
		      : (destinationFile.toString() + ".csv");
      System.out.println(TAG+" - writeCurvesToFile(): "
            +"\t - fullFileName: "+fullFileName
            );
		WriteToFile.open(fullFileName);
		
		// write curve parameters to csv
		writeParametersToFile();

		// write header row to file
		writeHeaderRowToFile();

		for (int i = 0; i < mhCurveSet[0].getLength(); i++) 
		{
			System.out.print(mhCurveSet[0].getDipole(i).getH());
			WriteToFile.append( String.valueOf(mhCurveSet[0].getDipole(i).getH()) );
			for (int j = 0; j < curveCount; j++) 
			{
				// write data to file
				System.out.print  (", " + mhCurveSet[j].getDipole(i).getM());
				WriteToFile.append(", " + mhCurveSet[j].getDipole(i).getM());
			} 
			
			float chi = averageMCurve.getDipole(i).getM()/mhCurveSet[0].getDipole(i).getH(); //chi is susceptibility (= M/H)
			
			WriteToFile.append(", " + averageMCurve.getDipole(i).getM());
			WriteToFile.append(", " + minMCurve.getDipole(i).getM());
			WriteToFile.append(", " + maxMCurve.getDipole(i).getM());
			WriteToFile.append(", " + chi);
			WriteToFile.appendNewLine();

			System.out.print  (", " + averageMCurve.getDipole(i).getM());
			System.out.print  (", " + minMCurve.getDipole(i).getM());
			System.out.print  (", " + maxMCurve.getDipole(i).getM());
			System.out.print(", " + chi);
			System.out.println();
			
		}
		
/*		
		//			while (recordedNetMPositive[i][numberRecordPoints] < MonteCarloHysteresisPanel.saturationM)
		for (int recordPoints = -numberRecordPoints+1; recordPoints < 0; recordPoints++)
		{
			float averageM = 0f;
			appliedH     = recordPoints * DEFAULT_RECORD_STEP_SIZE;
			System.out.print(appliedH);
			WriteToFile.append(String.valueOf(appliedH));
			for (int j = 0; j < curveCount; j++) 
			{
				// write data to file
				System.out.print(", " + recordedNetMNegative[-recordPoints][j]);
				WriteToFile.append(", " + recordedNetMNegative[-recordPoints][j]);
				averageM = averageM + recordedNetMNegative[-recordPoints][j];
			} 
			averageM = averageM/curveCount;
			WriteToFile.append(", " + averageM);
			WriteToFile.append(", " + averageM/appliedH);
			WriteToFile.appendNewLine();

			System.out.print(", " + averageM);
			System.out.println();
		}				
		for (int recordPoints = 0; recordPoints < numberRecordPoints; recordPoints++)
		{
			float averageM = 0f;
			appliedH     = recordPoints * DEFAULT_RECORD_STEP_SIZE;
			System.out.print(appliedH);
			WriteToFile.append(String.valueOf(appliedH));
			for (int j = 0; j < curveCount; j++) 
			{
				// write data to file
				System.out.print(", " + recordedNetMPositive[recordPoints][j]);  
				WriteToFile.append(", " + recordedNetMPositive[recordPoints][j]);
				averageM = averageM + recordedNetMPositive[recordPoints][j];
				} 
			averageM = averageM/curveCount;
			WriteToFile.append(", " + averageM);
			WriteToFile.append(", " + averageM/appliedH);
			WriteToFile.appendNewLine();

			System.out.print(", " + averageM);
			System.out.println();

		} // END - while recordedNetM 
			*/
		WriteToFile.close();
	}

//******************** writeHeaderRowToFile() ********************
/**
 * Write header row to file.
 * e.g.: H, M0, M1,..., Mn, M-ave, M-min, M-max, chi
 */
private void writeHeaderRowToFile() 
{
	String columnHeader;
	columnHeader = "H";
	for (int i = 0; i < curveCount; i++) 
	{
		columnHeader = columnHeader + ",M" + i;
	}
	columnHeader = columnHeader + ",M-ave,M-min,M-max,chi";
	
	WriteToFile.write(columnHeader);
	WriteToFile.appendNewLine();
	
	System.out.println();
	System.out.println(columnHeader);
}

//******************** writeParametersToFile() ********************
/**
 * 
 */
private void writeParametersToFile() 
{
	
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm' 'z");
//	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT)' ");
	String stringDate = df.format(Calendar.getInstance().getTime());
	
	WriteToFile.write("Version," + Monte_Carlo_Hysteresis_Application.VERSION_NUMBER);
	WriteToFile.appendNewLine();
	WriteToFile.write("Username," + System.getProperty("user.name"));
	WriteToFile.appendNewLine();
//	WriteToFile.write("Date/Time," + Calendar.getInstance().getTime());
	WriteToFile.write("Date/Time," + stringDate);
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.DIMENSIONS_X_AXIS_LABEL + "," + mCubeEdgeX);
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.DIMENSIONS_Y_AXIS_LABEL + ","  + mCubeEdgeY);
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.DIMENSIONS_Z_AXIS_LABEL + ","  + mCubeEdgeZ);
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.DIPOLE_RADIUS_LABEL + "," + magneticCube.getDipoleRadius());
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.PACKING_FRACTION_LABEL + "," + magneticCube.getPackingFraction());
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.RECORDING_PASSES_LABEL + "," + curveCount);
	WriteToFile.appendNewLine();
}

//******************** updatePackingFraction ********************
public void updatePackingFraction(float packingFraction) 
{
		magneticCube.setPackingFraction(packingFraction);
}


} //END ******************** class - CurveFamily ********************

