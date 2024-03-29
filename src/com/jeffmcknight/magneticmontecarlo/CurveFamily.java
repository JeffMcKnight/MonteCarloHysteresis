/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry;
import org.jetbrains.annotations.NotNull;

/**
 * @author JeffMckKight
 *
 */
public class CurveFamily
{
   public static final String TAG = CurveFamily.class.getSimpleName();
   public static final int DEFAULT_RECORD_POINTS = 15;
	public static final float DEFAULT_MINIMUM_H = 0;

	private final int   curveCount;
	private final int   mCubeEdgeX;
	private final int   mCubeEdgeY;
	private final int   mCubeEdgeZ;

	private final MagneticMedia magneticCube;
	HysteresisCurve[] mhCurveSet;
	HysteresisCurve averageMCurve;
	HysteresisCurve minMCurve;
	HysteresisCurve maxMCurve;

//	public static final float defaultIndexA  = 1.0f;
//	float recordedNetMNegative[][] = new float[numberRecordPoints][recordPasses];
//	float recordedNetMPositive[][] = new float[numberRecordPoints][recordPasses];

	//********** constructor - CurveFamily **********
	public CurveFamily(int count,
			int xDim, 
			int yDim, 
			int zDim, 
			float packingFraction, 
			float dipoleRadius, 
			float maximumH) {
		curveCount = count;
		mCubeEdgeX       = xDim;
		mCubeEdgeY       = yDim;
		mCubeEdgeZ       = zDim;
		magneticCube = new MagneticMedia(mCubeEdgeX, mCubeEdgeY, mCubeEdgeZ, packingFraction, dipoleRadius);
		averageMCurve = new HysteresisCurve(DEFAULT_MINIMUM_H, maximumH, maximumH/DEFAULT_RECORD_POINTS);
		minMCurve     = new HysteresisCurve(DEFAULT_MINIMUM_H, maximumH, maximumH/DEFAULT_RECORD_POINTS);
		maxMCurve     = new HysteresisCurve(DEFAULT_MINIMUM_H, maximumH, maximumH/DEFAULT_RECORD_POINTS);
		mhCurveSet = new  HysteresisCurve[curveCount];
		for (int i = 0; i < mhCurveSet.length; i++) {
			mhCurveSet[i] = new HysteresisCurve(DEFAULT_MINIMUM_H, maximumH, maximumH/DEFAULT_RECORD_POINTS);
		}
	}

	public CurveFamily(
			int recordCount,
			@NotNull MediaGeometry geometry,
			float maxAppliedField) {
		this(
				recordCount,
				geometry.getXCount(),
				geometry.getYCount(),
				geometry.getZCount(),
				geometry.getPackingFraction(),
				geometry.getDipoleRadius(),
				maxAppliedField

		);
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

   public MagneticMedia getMagneticCube() {
	return magneticCube;
}


		//******************** recordMHCurves ********************
		public void recordMHCurves() 
		{
	//		run recording passes for "recordPasses" times
			for (int i = 0; i < curveCount; i++) {
				System.out.print("\nPass " + i + " ");
				magneticCube.randomizeLattice();
				mhCurveSet[i].generateCurve(magneticCube);
			} 
			generateAverageMCurve();
			generateMinMCurve();
			generateMaxMCurve();
		}

		//******************** generateMaxMCurve() ********************
		private void generateMaxMCurve() {
			for (int i = 0; i < mhCurveSet[0].getLength(); i++) {
				float tempM = mhCurveSet[0].getDipole(i).getM();
				for (int j = 0; j < mhCurveSet.length; j++) {
					tempM = Math.max(tempM, mhCurveSet[j].getDipole(i).getM());
				}
				maxMCurve.getDipole(i).setM(tempM);	
			}
		}

		//******************** generateMinMCurve() ********************
		private void generateMinMCurve() 
		{
			for (int i = 0; i < mhCurveSet[0].getLength(); i++) {
				float tempM = mhCurveSet[0].getDipole(i).getM();
				for (int j = 0; j < mhCurveSet.length; j++) {
					tempM = Math.min(tempM, mhCurveSet[j].getDipole(i).getM());
				}
				minMCurve.getDipole(i).setM(tempM);	
			}
		}

		//******************** generateAverageM() ********************
		/**
		 * 
		 */
		private void generateAverageMCurve() {
			// iterate over each record point
			for (int i = 0; i < mhCurveSet[0].getLength(); i++) {
				float tempM = 0;
				for (int j = 0; j < mhCurveSet.length; j++) {
					tempM = tempM + mhCurveSet[j].getDipole(i).getM(); 
				}
				averageMCurve.getDipole(i).setM(tempM/mhCurveSet.length);	
			}
		}

//******************** writeCurveToFile() ********************
public void writeCurvesToFile(File destinationDirectory, File destinationFile) {
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
	
	WriteToFile.write("Version," + MonteCarloHysteresisApplication.VERSION_NUMBER);
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

}

