/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

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
	private static final int DEFAULT_RECORD_POINTS = 50;
	public static final int   DEFAULT_RECORD_STEP_SIZE = 2;
	private static final float DEFAULT_DIPOLE_RADIUS = 0.5f;
	private static final float DEFAULT_LATTICE_CONSTANT = 1.0f;
	private static final float DEFAULT_PACKING_FRACTION = 1.0f;
	public static final float SATURATION_M    = 100.0f;
	public static final float MAXIMUM_H    = 75.0f;

	public int   curveCount;
	public int   numberRecordPoints;
	public int   cubeEdgeX;
	public int   cubeEdgeY;
	public int   cubeEdgeZ;
//	private float defaultDipoleRadius = 0.000001f;
	private float latticeConst = DEFAULT_LATTICE_CONSTANT;
	
	MagneticMedia magneticCube;
	HysteresisCurve[] mhCurveSet;
	HysteresisCurve averageMCurve;
	HysteresisCurve minMCurve;
	HysteresisCurve maxMCurve;
	
	
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
		cubeEdgeX       = 10;
		cubeEdgeY       = 10;
		cubeEdgeZ       = 10;
		magneticCube = new MagneticMedia(cubeEdgeX, cubeEdgeY, cubeEdgeZ, DEFAULT_PACKING_FRACTION, DEFAULT_DIPOLE_RADIUS);
		mhCurveSet = new  HysteresisCurve[count];
		averageMCurve = new HysteresisCurve(MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
	}

	public CurveFamily(int count, int xDim, int yDim, int zDim, float packingFraction, float dipoleRadius) 
	{
		numberRecordPoints   = (int) (2*(MAXIMUM_H/DEFAULT_RECORD_STEP_SIZE) + 1);
		curveCount = count;
		cubeEdgeX       = xDim;
		cubeEdgeY       = yDim;
		cubeEdgeZ       = zDim;
		latticeConst = 2f * dipoleRadius / packingFraction ; 
		magneticCube = new MagneticMedia(cubeEdgeX, cubeEdgeY, cubeEdgeZ, packingFraction, dipoleRadius);
		averageMCurve = new HysteresisCurve(MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
		minMCurve     = new HysteresisCurve(MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
		maxMCurve     = new HysteresisCurve(MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
		mhCurveSet = new  HysteresisCurve[curveCount];
		for (int i = 0; i < mhCurveSet.length; i++) 
		{
			mhCurveSet[i] = new HysteresisCurve(MAXIMUM_H, DEFAULT_RECORD_STEP_SIZE);
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
		this.cubeEdgeX = cubeDim;
	}

	//******************** setCubeEdge() ********************
	public void setCubeEdgeY(int cubeDim) 
	{
		this.cubeEdgeY = cubeDim;
	}

	//******************** setCubeEdge() ********************
	public void setCubeEdgeZ(int cubeDim) 
	{
		this.cubeEdgeZ = cubeDim;
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
public void writeCurvesToFile(float[][] recordedNetMNegative,
			float[][] recordedNetMPositive) 
{
		float appliedH;
		String userDirectory;
		String fileSeparator;
		String fullFileName;
		String columnHeader;
		

		// Generate the full file path/name to put output csv in 
		// same directory as the app (ie: userDirectory)
		userDirectory = System.getProperty("user.dir");
		fileSeparator = System.getProperty("file.separator");
		fullFileName = userDirectory 
				+ fileSeparator 
				+ "MHCurve_" 
				+ Monte_Carlo_Hysteresis_Application.VERSION_SUFFIX 
				+ "_" 
				+ System.currentTimeMillis()  
				+ ".csv";
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
	WriteToFile.write(MonteCarloHysteresisPanel.DIMENSIONS_X_AXIS_LABEL + "," + cubeEdgeX);
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.DIMENSIONS_Y_AXIS_LABEL + ","  + cubeEdgeY);
	WriteToFile.appendNewLine();
	WriteToFile.write(MonteCarloHysteresisPanel.DIMENSIONS_Z_AXIS_LABEL + ","  + cubeEdgeZ);
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

