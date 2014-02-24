/*
 */ 

package com.jeffmcknight.magneticmontecarlo;

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

//******************** class - MonteCarloHysteresisPanel ********************
public class MonteCarloHysteresisPanel extends JPanel implements ActionListener 
{
   /**
    * 
    */
   private static final long serialVersionUID = 5824180412325621552L;
   public static final int DEFAULT_BORDER_SPACE = 30;
   public static final int DEFAULT_APPLIED_FIELD_ITEM = 4;
   public static final float SATURATION_M    = 100.0f;
   public static final float DEFAULT_INDEX_A  = 1.0f;
   public static final String CURVE_NAME = "Curve #";
   public static final String DIMENSIONS_X_AXIS_LABEL = "Lattice dimensions (X-axis):  ";
   public static final String DIMENSIONS_Y_AXIS_LABEL = "Lattice dimensions (Y-axis):  ";
   public static final String DIMENSIONS_Z_AXIS_LABEL = "Lattice dimensions (Z-axis):  ";
   public static final String DIPOLE_RADIUS_LABEL = "Dipole radius [um]:               ";
   public static final String PACKING_FRACTION_LABEL = "Packing Fraction:                  ";
   public static final String RECORDING_PASSES_LABEL = "Number of Recording Passes:  ";
   public static final String APPLIED_FIELD_RANGE_LABEL = "Maximum Applied Field (H)";
   public static final String CURVE_BUTTON_TEXT = "Show M-H Curve";
   public static final String DIPOLE_BUTTON_TEXT = "Show Dipole";
   private static final String RUN_SIMULATION = "run_simulation";
   final static String[] LATTICE_ITEMS = {"1","2","3","4","5","6","7","8","9","10"};
   final static String[] PACKING_FRACTION_OPTIONS = {"1.0","0.9","0.8","0.7","0.6","0.5","0.4","0.3","0.2","0.1"};
   final static String[] DIPOLE_RADIUS_OPTIONS = {"0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0","1.1","1.2","1.3","1.4","1.5","1.6","1.7","1.8","1.9","2.0"};
   final static String[] H_FIELD_RANGE_ITEMS = {"10","20","30","40","50","60","70","80","90","100"};
   static int   intNumberCurves   = 1;
   public int   numberRecordPoints;
   private CurveFamily mhCurves;

   static JFrame frame;
   private JComboBox mXComboBox;
   private JComboBox mYComboBox;
   private JComboBox mZComboBox;
   private JComboBox dipoleRadiusList;
   private JComboBox mPackingFractionBox;
   private JComboBox mRecordCountBox;
   private JComboBox mAppliedFieldRangeBox;
   private JRadioButton mCurveRadioButton;
   private ButtonGroup mRadioButtonGroup;
   private JRadioButton mDipoleRadioButton;

   private Chart2D mhChart;
    // Create a frame.
   JFrame chartFrame;
   private JPanel mChartPanel;
   private JPanel mControlsPanel;
   private Color traceColor = new Color(255,0,0);
   private float traceHue = 0f;
   private int traceNumber;
   private ITrace2D mTrace;
   
   public interface CurveUpdateListener{
      
   }

    public MonteCarloHysteresisPanel() 
    {
    	numberRecordPoints = CurveFamily.getDefaultRecordPoints();
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
    } // END constructor

   // *************** buildRunConfigPanel() ***************
   /**
    * Build JPanel on left side of JFrame that contains 
    * labeled JComboBoxes to set simulation parameters 
    */
   public void buildRunConfigPanel()
   {
        int initialComboIndex = 6;

        // Create combo boxes for lattice parameters
        mXComboBox = new JComboBox(LATTICE_ITEMS);
        mYComboBox = new JComboBox(LATTICE_ITEMS);
        mZComboBox = new JComboBox(LATTICE_ITEMS);
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
        // Add combo box panels for dipole radius, packing fraction, et
        mControlsPanel.add(dipoleRadiusPanel);
        mControlsPanel.add(packingFractionPanel);
        mControlsPanel.add(recordCountPanel);
        mControlsPanel.add(mAppliedFieldRangePanel);
        
        // Add vertical space between combo buttons and radio buttons
        mControlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));	
        
        mControlsPanel.add(mCurveRadioButton);
        mControlsPanel.add(mDipoleRadioButton);

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

    //******************** actionPerformed() ********************
   // Implements ActionListener callback method
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
			mhCurves = new CurveFamily(recordCount, xAxisCount, yAxisCount, zAxisCount, packingFraction, dipoleRadius, maxAppliedField);
			
			float recordedNetMNegative[][] = new float[numberRecordPoints][recordCount];
			float recordedNetMPositive[][] = new float[numberRecordPoints][recordCount];

			mhCurves.recordMHCurves(recordedNetMNegative, recordedNetMPositive);
//			mhCurves.writeCurvesToFile();
			
	      addAllPoints(mhCurves, mTrace);
		}
        
    } // END ******************** actionPerformed() ********************


  //******************** createAndShowGUI() ********************
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should only be invoked from the
     * event-dispatching thread.
    * @param panel TODO
     */
/*    
    private static void createAndShowGUI() 
    {
        //Create and set up the window.
        JFrame frame = new JFrame(BERTRAM_MONTE_CARLO);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new MonteCarloHysteresisPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
*/
    public void showChart(JPanel panel)
    {
       // Set chart axis titles
       mhChart.getAxisX().getAxisTitle().setTitle("H");
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

   // *************** () ***************
   /**
    * @param chartCurves
    * @param trace
    */
   public void addAllPoints(CurveFamily chartCurves, ITrace2D trace)
   {
      // Increment the count and update the color 
      // to display multiple traces on the same chart.
      traceNumber = traceNumber  + 1;
      traceColor = Color.getHSBColor(traceHue, 1f, 0.85f);
      traceHue = (traceHue + 0.22f);
      String traceName = new StringBuilder(CURVE_NAME + traceNumber)
      .append(" : ")
      .append(mhCurves.getCubeEdgeX())
      .append("x")
      .append(mhCurves.getCubeEdgeY())
      .append("x")
      .append(mhCurves.getCubeEdgeZ())
      .append(" - Packing Fraction: ")
      .append(mhCurves.getPackingFraction())
      .append(" - Radius [um]: ")
      .append(mhCurves.getDipoleRadius())
      .toString();
      
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

    
/*
    public static void main(String[] args) 
    {
    	//Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                createAndShowGUI();
            }
        });
    }
*/
}
