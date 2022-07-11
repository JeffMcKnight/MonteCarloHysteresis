package com.jeffmcknight.magneticmontecarlo;

import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.jeffmcknight.magneticmontecarlo.CurveFamily.CurveFamilyListener;
import com.jeffmcknight.magneticmontecarlo.MagneticMedia.MagneticMediaListener;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class MonteCarloHysteresisPanel extends JPanel implements ActionListener {
    private static final String TAG = MonteCarloHysteresisPanel.class.getSimpleName();
    private static final long serialVersionUID = 5824180412325621552L;
   public static final int DEFAULT_BORDER_SPACE = 30;
   public static final int DEFAULT_APPLIED_FIELD_ITEM = 0;
   public static final float SATURATION_M    = 100.0f;
   public static final float DEFAULT_INDEX_A  = 1.0f;
   public static final double MOVING_AVERAGE_WINDOW = 0.1;
   public static final String CURVE_CHART_TITLE = "Curve #";
   public static final String DIPOLE_CHART_TITLE = "Dipole Set: ";
   public static final String DIMENSIONS_X_AXIS_LABEL = "Lattice dimensions (X-axis):  ";
   public static final String DIMENSIONS_Y_AXIS_LABEL = "Lattice dimensions (Y-axis):  ";
   public static final String DIMENSIONS_Z_AXIS_LABEL = "Lattice dimensions (Z-axis):  ";
   public static final String DIPOLE_RADIUS_LABEL = "Dipole radius [um]:               ";
   public static final String PACKING_FRACTION_LABEL = "Packing Fraction:                  ";
   public static final String RECORDING_PASSES_LABEL = "Number of Recording Passes:  ";
   public static final String APPLIED_FIELD_RANGE_LABEL = "Maximum Applied Field (H)";
   public static final String APPLIED_FIELD_LABEL = "Applied Field (H)";
   public static final String CURVE_BUTTON_TEXT = "Show M-H Curve";
   public static final String DIPOLE_BUTTON_TEXT = "Show Dipole";
   private static final String RUN_SIMULATION = "run_simulation";
   final static String[] LATTICE_ITEMS = {"1","2","3","4","5","6","7","8","9","10"};
   final static String[] PACKING_FRACTION_OPTIONS = {"1.0","0.9","0.8","0.7","0.6","0.5","0.4","0.3","0.2","0.1"};
   final static String[] DIPOLE_RADIUS_OPTIONS = {"0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0","1.1","1.2","1.3","1.4","1.5","1.6","1.7","1.8","1.9","2.0"};
   final static String[] H_FIELD_RANGE_ITEMS = {"10","20","30","40","50","60","70","80","90","100","110","120","130","140","150","160","170","180","190","200"};
   static int   intNumberCurves   = 1;
   private MagneticMedia mMagneticMedia;
   private CurveFamily mhCurves;

   static JFrame frame;
   private JComboBox<String> mXComboBox;
   private JComboBox<String> mYComboBox;
   private JComboBox<String> mZComboBox;
   private JComboBox<String> dipoleRadiusList;
   private JComboBox<String> mPackingFractionBox;
   private JComboBox<String> mRecordCountBox;
   private JComboBox<String> mAppliedFieldRangeBox;
   private ButtonGroup mRadioButtonGroup;
   private JRadioButton mCurveRadioButton;
   private JRadioButton mDipoleRadioButton;

   private Chart2D mhChart;
    // Create a frame.
   private int mCurveTraceCount;
   private int mDipoleTraceCount;
   private JFrame chartFrame;
   private JPanel mChartPanel;
   private JPanel mControlsPanel;
   private Color traceColor = new Color(255,0,0);
   private float traceHue = 0f;
   private ITrace2D mTrace;
   private ChartType mActiveChart;
   private MagneticMediaListener mDipoleUpdateListener;
   private MagneticMediaListener mChartUpdateListener;
   private CurveFamilyListener mCurveFamilyListener;
   private MovingAverageTrace2D mCumulativeAverageTrace;
   private MovingAverageTrace2D mTwoPointAverageTrace;
   private MovingAverageTrace2D mScaledWindowTrace;
   private MovingAverageTrace2D mScaledTotalTrace;
    private final ViewModel viewModel = new ViewModel();

    public MonteCarloHysteresisPanel()
    {
    	mCurveTraceCount = 0;
    	mDipoleTraceCount = 0;
    	CurveFamily.getDefaultRecordPoints();
    	mActiveChart = ChartType.MH_CURVE;
        // Create a chart:
        mhChart = new Chart2D();
        // Create a frame.
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        mControlsPanel = new JPanel();
        mControlsPanel.setLayout(new BoxLayout(mControlsPanel, BoxLayout.PAGE_AXIS));
        this.add(mControlsPanel);

        mChartPanel = new JPanel();
        mChartPanel.setLayout(new BoxLayout(mChartPanel, BoxLayout.X_AXIS));
        this.add(mChartPanel);

        buildRunConfigPanel();
        showChart(mChartPanel);
        implementDipoleChartListener();
        implementCurveChartListener();
        implementCurveFamilyListener();
        } // END constructor

    /**
     * Redraws dipole chart when the MagneticMedia notifies that it has updated itself
     */
	private void implementDipoleChartListener() {
		mDipoleUpdateListener = new MagneticMediaListener() {
    		@Override
    		public void onRecordingDone(MagneticMedia magneticMedia) {
    			showDipoleTraces(magneticMedia);
    		}

			@Override
			public void onDipoleFixated(@Nullable DipoleSphere3f dipoleSphere3f) {
				System.out.println(TAG
						+ "\t -- notifyDipoleStuck()"
						+ "\t -- dipoleSphere3f.getM(): " + dipoleSphere3f.getM()
						);
			}
    	};
	}

    /**
     * Redraws MH CurveFamily chart when the CurveFamily object notifies that it has updated itself
     */
	private void implementCurveFamilyListener() {
		mCurveFamilyListener = new CurveFamilyListener() {
			@Override
			public void notifyCurvesDone(CurveFamily curveFamily) {
				addMhPoints(curveFamily, mTrace);
			}
		};
	}

    /**
     *
     */
    private void implementCurveChartListener() {
    	mChartUpdateListener = new MagneticMediaListener() {
    		@Override
    		public void onRecordingDone(MagneticMedia magneticMedia) {
    			// TODO - stub
    		}

			@Override
			public void onDipoleFixated(@Nullable DipoleSphere3f dipoleSphere3f) {
				// TODO Auto-generated method stub

			}
    	};
    }

// *************** buildRunConfigPanel() ***************
   /**
    * Build JPanel on left side of JFrame that contains
    * labeled JComboBoxes to set simulation parameters
    */
   public void buildRunConfigPanel()
   {
        int initialComboIndex = 7;

        // Create combo boxes for lattice parameters
        mXComboBox = new JComboBox<String>(LATTICE_ITEMS);
        mYComboBox = new JComboBox<String>(LATTICE_ITEMS);
        mZComboBox = new JComboBox<String>(LATTICE_ITEMS);
        dipoleRadiusList = new JComboBox(DIPOLE_RADIUS_OPTIONS);
        mPackingFractionBox = new JComboBox(PACKING_FRACTION_OPTIONS);
        mRecordCountBox = new JComboBox(LATTICE_ITEMS);
        mAppliedFieldRangeBox = new JComboBox(H_FIELD_RANGE_ITEMS);

        mCurveRadioButton = new JRadioButton();
        mCurveRadioButton.setText(CURVE_BUTTON_TEXT);
        mCurveRadioButton.setSelected(true);

        mDipoleRadioButton = new JRadioButton();
        mDipoleRadioButton.setText(DIPOLE_BUTTON_TEXT);

        mRadioButtonGroup = new ButtonGroup();
        mRadioButtonGroup.add(mCurveRadioButton);
        mRadioButtonGroup.add(mDipoleRadioButton);

        // Create combo box panels for lattice dimensions
        JPanel xComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_X_AXIS_LABEL, mXComboBox);
        JPanel yComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_Y_AXIS_LABEL, mYComboBox);
        JPanel zComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_Z_AXIS_LABEL, mZComboBox);
        JPanel dipoleRadiusPanel = buildComboBoxPanel(3, DIPOLE_RADIUS_LABEL, dipoleRadiusList);
        JPanel packingFractionPanel = buildComboBoxPanel(1, PACKING_FRACTION_LABEL, mPackingFractionBox);
        JPanel recordCountPanel = buildComboBoxPanel(0, RECORDING_PASSES_LABEL, mRecordCountBox);
        JPanel mAppliedFieldRangePanel = buildComboBoxPanel(DEFAULT_APPLIED_FIELD_ITEM, APPLIED_FIELD_RANGE_LABEL, mAppliedFieldRangeBox);
        JPanel mRadioButtonPanel = buildButtonPanel(mRadioButtonGroup);

        // Add combo box panels for lattice dimensions
        mControlsPanel.add(xComboBoxPanel);
        mControlsPanel.add(yComboBoxPanel);
        mControlsPanel.add(zComboBoxPanel);
        // Add separator line
        mControlsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        // Add combo box panels for dipole radius, packing fraction, etc
        mControlsPanel.add(dipoleRadiusPanel);
        mControlsPanel.add(packingFractionPanel);
        mControlsPanel.add(recordCountPanel);
        mControlsPanel.add(mAppliedFieldRangePanel);

        // Add vertical space between combo buttons and radio buttons
        mControlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        mControlsPanel.add(mCurveRadioButton);
        mControlsPanel.add(mDipoleRadioButton);
        mCurveRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMhCurveChart();
			}
		});
        mDipoleRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showDipoleTraces(mMagneticMedia);
			}
		});

        // Add vertical space between radio buttons and run JButton
        mControlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add run JButton
        JButton buttonRun;
        buttonRun = new JButton("Run");
        buttonRun.setVerticalTextPosition(AbstractButton.CENTER);
        buttonRun.setHorizontalTextPosition(AbstractButton.CENTER); //aka LEFT, for left-to-right locales
        buttonRun.setMnemonic(KeyEvent.VK_R);
        buttonRun.setActionCommand(RUN_SIMULATION);
        buttonRun.addActionListener(this);
        buttonRun.setToolTipText("Click to start simulation");
        JPanel buttonRunPanel = new JPanel();				//create panel for button
        buttonRunPanel.setLayout(new BoxLayout(buttonRunPanel, BoxLayout.X_AXIS));
        buttonRunPanel.add(buttonRun);  					// add button to panel
        buttonRunPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonRun.setAlignmentX(Component.RIGHT_ALIGNMENT);
        mControlsPanel.add(buttonRunPanel);

        mControlsPanel.add(Box.createVerticalStrut(300));

        // Add border padding around entire panel
        setBorder(BorderFactory.createEmptyBorder(
        		DEFAULT_BORDER_SPACE,
        		DEFAULT_BORDER_SPACE,
        		DEFAULT_BORDER_SPACE,
        		DEFAULT_BORDER_SPACE));

        mControlsPanel.setMaximumSize(new Dimension(200,600));
   }

protected void showDipoleTraces(MagneticMedia magneticMedia) {
	mActiveChart = ChartType.MH_CURVE_POINT;
	mAppliedFieldRangeBox.setName(APPLIED_FIELD_LABEL);
	mhChart.removeAllTraces();
    mhChart.getAxisX().getAxisTitle().setTitle("n [Dipole count]");

    mCumulativeAverageTrace = new MovingAverageTrace2D(0, Color.PINK);
    mhChart.addTrace(mCumulativeAverageTrace.getTrace());
    mTwoPointAverageTrace = new MovingAverageTrace2D(2, Color.BLUE.brighter());
    mhChart.addTrace(mTwoPointAverageTrace.getTrace());
    mScaledTotalTrace = new MovingAverageTrace2D(-1, Color.GREEN.darker());
    mhChart.addTrace(mScaledTotalTrace.getTrace());

    if (null!=mMagneticMedia){
    	mCumulativeAverageTrace.buildTraceName(DIPOLE_CHART_TITLE, mMagneticMedia);
    	mCumulativeAverageTrace.generateMovingAverage(mMagneticMedia);

    	mTwoPointAverageTrace.buildTraceName(DIPOLE_CHART_TITLE, mMagneticMedia);
    	mTwoPointAverageTrace.generateMovingAverage(mMagneticMedia);

    	// Set moving average to a fraction of the total number of dipoles.
		int averagePeriod = (int) (MOVING_AVERAGE_WINDOW * magneticMedia.size());
	    mScaledWindowTrace = new MovingAverageTrace2D(averagePeriod, Color.RED);
	    mhChart.addTrace(mScaledWindowTrace.getTrace());
	    mScaledWindowTrace.buildTraceName(DIPOLE_CHART_TITLE, mMagneticMedia);
	    mScaledWindowTrace.generateMovingAverage(mMagneticMedia);

	    mScaledTotalTrace.buildTraceName(DIPOLE_CHART_TITLE, mMagneticMedia);
	    mScaledTotalTrace.generateScaledTotal(mMagneticMedia);
	}
}

/**
 * @param fastAveragePeriod
 * @param magneticCube TODO
 * @return
 */
private ITrace2D buildTrace(int fastAveragePeriod, MagneticMedia magneticCube, Color traceColor) {
	ITrace2D trace;
    // Increment the count and update the color
    // to display multiple traces on the same chart.
	mDipoleTraceCount = mDipoleTraceCount  + 1;
    String traceName = buildTraceName(DIPOLE_CHART_TITLE, mDipoleTraceCount, fastAveragePeriod, magneticCube);

    trace = new Trace2DSimple();
    // Set trace properties (name, color, point shape to disc)
    trace.setName(traceName);
    trace.setColor(traceColor);
    trace.setTracePainter(new TracePainterDisc());
	return trace;
}

private String chartDescription(int fastAveragePeriod) {
	String string;
	switch (fastAveragePeriod) {
	case 0:
		string = "Cumulative Average";
		break;
	case -1:
		string = "Total (Normalized to " + SATURATION_M + ")";
		break;
	default:
		string = "Moving Average over " + fastAveragePeriod + " dipoles";
		break;
	}
	return string;
}

/**
 * @param title
 * @param count TODO
 * @param fastAveragePeriod
 * @param magneticMedia TODO
 * @return
 */
private String buildTraceName(String title,
		int count,
		int fastAveragePeriod,
		MagneticMedia magneticMedia) {
	Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    String traceName = new StringBuilder(title)
    .append(String.valueOf(count))
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
    .append(chartDescription(fastAveragePeriod))
    .toString();
	return traceName;
}

protected void showMhCurveChart() {
	mActiveChart = ChartType.MH_CURVE;
	mAppliedFieldRangeBox.setName(APPLIED_FIELD_RANGE_LABEL);
    mhChart.getAxisX().getAxisTitle().setTitle("H [nWb]");
	mhChart.removeAllTraces();
	// Show data points only if we have already run a simulation.
	if (null!=mhCurves){
		addMhPoints(mhCurves, mTrace);
	}
}

// *************** buildButtonPanel() ***************
   private JPanel buildButtonPanel(ButtonGroup buttonGroup) {
	   JPanel panel = new JPanel();
	   return panel;
}

// *************** buildComboBoxPanel() ***************
   /**
    * @param initialComboIndex
    * @param label TODO
    * @param comboBox TODO
    * @return TODO
    */
   public JPanel buildComboBoxPanel(int initialComboIndex, String label, JComboBox comboBox)
   {
      JLabel comboBoxLabel = new JLabel(label);
      JPanel panel = new JPanel();

      comboBox.setEditable(true);
      comboBox.addActionListener(this);
      comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      comboBox.setSelectedIndex(initialComboIndex);

      panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
      panel.add(comboBoxLabel);
      panel.add(comboBox);
      panel.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.setMaximumSize(new Dimension(300, 0));

      return panel;
   }

    /**
     * Implements ActionListener callback method
     * @param e
     */
    public void actionPerformed(ActionEvent e)
    {
    	// Capture input from all combo boxes
        int xAxisCount = Integer.parseInt((String) mXComboBox.getSelectedItem());
        System.out.println("xAxisCount: " + xAxisCount);
        int yAxisCount = Integer.parseInt((String) mYComboBox.getSelectedItem());
        System.out.println("yAxisCount: " + yAxisCount);
        int zAxisCount = Integer.parseInt((String) mZComboBox.getSelectedItem());
        System.out.println("zAxisCount: " + zAxisCount);
        float dipoleRadius = Float.parseFloat((String) dipoleRadiusList.getSelectedItem());
        System.out.println("dipoleRadius: " + dipoleRadius);
        float packingFraction = Float.parseFloat((String) mPackingFractionBox.getSelectedItem());
        System.out.println("packingFraction: " + packingFraction);
        int recordCount = Integer.parseInt((String) mRecordCountBox.getSelectedItem());
        System.out.println("recordCount: " + recordCount);
        float maxAppliedField = Float.parseFloat((String) mAppliedFieldRangeBox.getSelectedItem());
        System.out.println("maxAppliedField: " + maxAppliedField);

        // Run simulation if run button is clicked
		if ( e.getActionCommand().equals(RUN_SIMULATION) )
		{
            MediaGeometry geometry = new MediaGeometry(xAxisCount, yAxisCount, zAxisCount, packingFraction, dipoleRadius);
            switch (mActiveChart) {
                case DIPOLE_AVERAGES:
                    viewModel.record(1.0F, geometry);
			case MH_CURVE:
				mhCurves = new CurveFamily(
						recordCount,
						xAxisCount,
						yAxisCount,
						zAxisCount,
						packingFraction,
						dipoleRadius,
						maxAppliedField,
						mChartUpdateListener,
						mCurveFamilyListener);
				mhCurves.recordMHCurves();
				break;
			case MH_CURVE_POINT:
				mMagneticMedia = MagneticMedia.Companion.create(geometry, mDipoleUpdateListener);
				mMagneticMedia.recordWithAcBias(maxAppliedField);
				break;
			default:
				break;
			}
		}

    } // END ******************** actionPerformed() ********************


    public void showChart(JPanel panel)
    {
       // Set chart axis titles
       mhChart.getAxisX().getAxisTitle().setTitle("H [nWb]");
       mhChart.getAxisY().getAxisTitle().setTitle("M");

       // Show chart grids for both x and y axis
       mhChart.getAxisX().setPaintGrid(true);
       mhChart.getAxisY().setPaintGrid(true);

       // Make it visible:
       // add the chart to the frame:
       panel.add(mhChart);
       panel.setMinimumSize(new Dimension(800, 600));
       panel.setLocation(0, 0);
       panel.setVisible(true);
    }

   // *************** addAllPoints() ***************
   /**
    * @param chartCurves
    * @param trace
    */
   public void addMhPoints(CurveFamily chartCurves, ITrace2D trace)
   {
      // Increment the count and update the color
      // to display multiple traces on the same chart.
      mCurveTraceCount = mCurveTraceCount  + 1;
      traceColor = Color.getHSBColor(traceHue, 1f, 0.85f);
      traceHue = (traceHue + 0.22f);
      String traceName = buildTraceName(CURVE_CHART_TITLE, mCurveTraceCount, -1, mhCurves.getMagneticCube());

      trace = new Trace2DSimple();
      // Set trace properties (name, color, point shape to disc)
      trace.setName(traceName);
      trace.setColor(traceColor);
      trace.setTracePainter(new TracePainterDisc());
      // Add the trace to the chart. This has to be done before adding points (deadlock prevention):
      mhChart.addTrace(trace);

      for(int i=0; i<chartCurves.getAverageMCurve().getLength(); i++)
    	{
    		trace.addPoint(chartCurves.getAverageMCurve().getDipole(i).getH(),chartCurves.getAverageMCurve().getDipole(i).getM());
    	}
   }

   // ******************** getmhChart() ********************
   // @return mhChart
   Chart2D getMhChart()
   {
      return mhChart;
   }

   // ******************** getmhCurves() ********************
   // @return mhCurves
   CurveFamily getMhCurves()
   {
      return mhCurves;
   }

}
