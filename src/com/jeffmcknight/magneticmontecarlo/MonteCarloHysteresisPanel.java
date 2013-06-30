/*
 */ 

package com.jeffmcknight.magneticmontecarlo;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.errorbars.ErrorBarPainter;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jeffmcknight.magneticmontecarlo.CurveFamily;

import java.util.*;
import java.text.SimpleDateFormat;

//******************** class - MonteCarloHysteresisPanel ********************
public class MonteCarloHysteresisPanel extends JPanel implements ActionListener 
{
	private static final int DEFAULT_BORDER_SPACE = 30;
	public static final String DIMENSIONS_X_AXIS_LABEL = "Lattice dimensions (X-axis):  ";
	public static final String DIMENSIONS_Y_AXIS_LABEL = "Lattice dimensions (Y-axis):  ";
	public static final String DIMENSIONS_Z_AXIS_LABEL = "Lattice dimensions (Z-axis):  ";
	public static final String DIPOLE_RADIUS_LABEL = "Dipole radius [um]:               ";
	public static final String PACKING_FRACTION_LABEL = "Packing Fraction:                  ";
	public static final String RECORDING_PASSES_LABEL = "Number of Recording Passes:  ";
	public static final float saturationM    = 100.0f;
	public static final float defaultIndexA  = 1.0f;
	static int   intNumberCurves   = 1;
	public int   numberRecordPoints;
	private CurveFamily mhCurves;

	static JFrame frame;
    JLabel result;
    String currentPattern;
	private JComboBox latticeXList;
	private JComboBox latticeYList;
	private JComboBox latticeZList;
	private JComboBox dipoleRadiusList;
	private JComboBox packingFractionList;
	private JComboBox recordCountList;
	
	Chart2D mhChart;
    // Create a frame.
    JFrame chartFrame;
	private Color traceColor = new Color(255,0,0);
	private float traceHue = 0f;
	private String stringColor;
	private int traceNumber;

    public MonteCarloHysteresisPanel() 
    {
    	numberRecordPoints = CurveFamily.getDefaultRecordPoints();
        // Create a chart:  
        mhChart = new Chart2D();
        // Create a frame.
        chartFrame = new JFrame("M-H Curve Families");

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        
        String[] intLatticeItems = {"1","2","3","4","5","6","7","8","9","10"};
        String[] floatLatticeItems = {"1.0","0.9","0.8","0.7","0.6","0.5","0.4","0.3","0.2","0.1"};
        String[] floatDipoleRadiusItems = {"0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0","1.1","1.2","1.3","1.4","1.5","1.6","1.7","1.8","1.9","2.0"};

        //Set up the UI for selecting a pattern.
//        JLabel versionTextLabel		= new JLabel("Monte Carlo Hysteresis Application, Version: " + Monte_Carlo_Hysteresis_Application.VERSION_NUMBER);
        JLabel latticeXLabel		= new JLabel(DIMENSIONS_X_AXIS_LABEL);
        JLabel latticeYLabel		= new JLabel(DIMENSIONS_Y_AXIS_LABEL);
        JLabel latticeZLabel		= new JLabel(DIMENSIONS_Z_AXIS_LABEL);
        JLabel dipoleRadiusLabel	= new JLabel(DIPOLE_RADIUS_LABEL);
        JLabel packingFractionLabel	= new JLabel(PACKING_FRACTION_LABEL);
        JLabel recordCountLabel		= new JLabel(RECORDING_PASSES_LABEL);

        latticeXList = new JComboBox(intLatticeItems);
        latticeXList.setEditable(true);
        latticeXList.addActionListener(this);

        latticeYList = new JComboBox(intLatticeItems);
        latticeYList.setEditable(true);
        latticeYList.addActionListener(this);

        latticeZList = new JComboBox(intLatticeItems);
        latticeZList.setEditable(true);
        latticeZList.addActionListener(this);

        dipoleRadiusList = new JComboBox(floatDipoleRadiusItems);
        dipoleRadiusList.setEditable(true);
        dipoleRadiusList.addActionListener(this);

        packingFractionList = new JComboBox(floatLatticeItems);
        packingFractionList.setEditable(true);
        packingFractionList.addActionListener(this);

        recordCountList = new JComboBox(intLatticeItems);
        recordCountList.setEditable(true);
        recordCountList.addActionListener(this);

  
        // ***** Lay out everything. *****

        // Add combo box for lattice X dimension
        JPanel xaxisPanel = new JPanel();
        xaxisPanel.setLayout(new BoxLayout(xaxisPanel, BoxLayout.LINE_AXIS));
        xaxisPanel.add(latticeXLabel);
        latticeXList.setAlignmentX(Component.LEFT_ALIGNMENT);
        latticeXList.setSelectedIndex(4);
        xaxisPanel.add(latticeXList);
        xaxisPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(xaxisPanel);
                
        // Add combo box for lattice Y dimension
        JPanel yaxisPanel = new JPanel();
        yaxisPanel.setLayout(new BoxLayout(yaxisPanel, BoxLayout.LINE_AXIS));
        yaxisPanel.add(latticeYLabel);
        latticeYList.setAlignmentX(Component.RIGHT_ALIGNMENT);
        latticeYList.setSelectedIndex(4);
        yaxisPanel.add(latticeYList);
        yaxisPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(yaxisPanel);
                
        // Add combo box for lattice Z dimension
        JPanel zaxisPanel = new JPanel();
        zaxisPanel.setLayout(new BoxLayout(zaxisPanel, BoxLayout.LINE_AXIS));
        zaxisPanel.add(latticeZLabel);
        latticeZList.setAlignmentX(Component.RIGHT_ALIGNMENT);
        latticeZList.setSelectedIndex(4);
        zaxisPanel.add(latticeZList);
        zaxisPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(zaxisPanel);
         
        // Add separator line 
        add(new JSeparator(SwingConstants.HORIZONTAL));
        
        // Add combo box for dipole radius
        JPanel dipoleRadiusPanel = new JPanel();
        dipoleRadiusPanel.setLayout(new BoxLayout(dipoleRadiusPanel, BoxLayout.LINE_AXIS));
        dipoleRadiusPanel.add(dipoleRadiusLabel);
        dipoleRadiusList.setAlignmentX(Component.RIGHT_ALIGNMENT);
        dipoleRadiusList.setSelectedIndex(2);
        dipoleRadiusPanel.add(dipoleRadiusList);
        dipoleRadiusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(dipoleRadiusPanel);

        // Add combo box for packing fraction
        JPanel packingFractionPanel = new JPanel();
        packingFractionPanel.setLayout(new BoxLayout(packingFractionPanel, BoxLayout.LINE_AXIS));
        packingFractionPanel.add(packingFractionLabel);
        packingFractionList.setAlignmentX(Component.RIGHT_ALIGNMENT);
        packingFractionList.setSelectedIndex(4);
        packingFractionPanel.add(packingFractionList);
        packingFractionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(packingFractionPanel);

        // Add combo box for number of curves per family
        JPanel recordCountPanel = new JPanel();
        recordCountPanel.setLayout(new BoxLayout(recordCountPanel, BoxLayout.LINE_AXIS));
        recordCountPanel.add(recordCountLabel);
        recordCountList.setAlignmentX(Component.RIGHT_ALIGNMENT);
        recordCountPanel.add(recordCountList);
        recordCountPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(recordCountPanel);
        
        // Add vertical space between combo buttons and run JButton
        add(Box.createRigidArea(new Dimension(0, 20)));	

        // Add run JButton
        JButton buttonRun;
        buttonRun= new JButton("Run");
        buttonRun.setVerticalTextPosition(AbstractButton.CENTER);
        buttonRun.setHorizontalTextPosition(AbstractButton.CENTER); //aka LEFT, for left-to-right locales
        buttonRun.setMnemonic(KeyEvent.VK_R);
        buttonRun.setActionCommand("run_simulation");
        buttonRun.addActionListener(this);
        buttonRun.setToolTipText("Click to start simulation");
        JPanel buttonRunPanel = new JPanel();				//create panel for button
        buttonRunPanel.setLayout(new BoxLayout(buttonRunPanel, BoxLayout.X_AXIS));
        buttonRunPanel.add(buttonRun);  					// add button to panel
        buttonRunPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonRun.setAlignmentX(Component.RIGHT_ALIGNMENT);
        add(buttonRunPanel);

        // Add border padding around entire panel
        setBorder(BorderFactory.createEmptyBorder(
        		DEFAULT_BORDER_SPACE,
        		DEFAULT_BORDER_SPACE,
        		DEFAULT_BORDER_SPACE,
        		DEFAULT_BORDER_SPACE));

    } // END constructor

    //******************** actionPerformed() ********************
    public void actionPerformed(ActionEvent e) 
    {
    	// Capture input from all combo boxes
        int xAxisCount = Integer.parseInt((String) latticeXList.getSelectedItem());
        System.out.println("xAxisCount: " + xAxisCount);
        int yAxisCount = Integer.parseInt((String) latticeYList.getSelectedItem());
        System.out.println("yAxisCount: " + yAxisCount);
        int zAxisCount = Integer.parseInt((String) latticeZList.getSelectedItem());
        System.out.println("zAxisCount: " + zAxisCount);
        float dipoleRadius = Float.parseFloat((String) dipoleRadiusList.getSelectedItem());
        System.out.println("dipoleRadius: " + dipoleRadius);
        float packingFraction = Float.parseFloat((String) packingFractionList.getSelectedItem());
        System.out.println("packingFraction: " + packingFraction);
        int recordCount = Integer.parseInt((String) recordCountList.getSelectedItem());
        System.out.println("recordCount: " + recordCount);

        // Run simulation if run button is clicked
		if ( "run_simulation".equals(e.getActionCommand()) ) 
		{
			mhCurves = new CurveFamily(recordCount, xAxisCount, yAxisCount, zAxisCount, packingFraction, dipoleRadius);
			
			float recordedNetMNegative[][] = new float[numberRecordPoints][recordCount];
			float recordedNetMPositive[][] = new float[numberRecordPoints][recordCount];

			mhCurves.recordMHCurves(recordedNetMNegative, recordedNetMPositive);
			mhCurves.writeCurvesToFile(recordedNetMNegative, recordedNetMPositive);
			
			showChart(mhCurves);
		}
        
    } // END ******************** actionPerformed() ********************


  //******************** createAndShowGUI() ********************
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should only be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() 
    {
        //Create and set up the window.
        JFrame frame = new JFrame("Bertram Monte Carlo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new MonteCarloHysteresisPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void showChart(CurveFamily chartCurves)
    {
    	traceNumber = traceNumber  + 1;
    	traceColor = Color.getHSBColor(traceHue, 1f, 0.85f);
    	traceHue = (traceHue + 0.22f) ;
    	
    	// Create an ITrace: 
    	ITrace2D trace = new Trace2DSimple(); 
    	// Set trace properties (name, color, point shape to disc) 
    	trace.setName("M-H Curve #" + traceNumber );
    	trace.setColor(traceColor);
    	trace.setTracePainter(new TracePainterDisc());

    	
    	
    	
    	// Set chart axis titles
      	mhChart.getAxisX().getAxisTitle().setTitle("H");
      	mhChart.getAxisY().getAxisTitle().setTitle("M");

    	// Show chart grids for both x and y axis 
      	mhChart.getAxisX().setPaintGrid(true);
      	mhChart.getAxisY().setPaintGrid(true);

    	
    	// Add the trace to the chart. This has to be done before adding points (deadlock prevention): 
    	mhChart.addTrace(trace);    
    	// Add all points, as it is static: 
    	//      Random random = new Random();
    	for(int i=0; i<chartCurves.getAverageMCurve().getLength(); i++)
    	{
    		trace.addPoint(chartCurves.getAverageMCurve().getDipole(i).getH(),chartCurves.getAverageMCurve().getDipole(i).getM());
    	}
    	// Make it visible:
    	// add the chart to the frame: 
    	chartFrame.getContentPane().add(mhChart);
    	chartFrame.setSize(800,600);
    	chartFrame.setLocation(300, 0);
    	// Enable the termination button [cross on the upper right edge]: 
    	chartFrame.addWindowListener
    	(
    			new WindowAdapter()
    			{
    				public void windowClosing(WindowEvent e)
    				{
    					System.exit(0);
    				}
    			}
    			);
    	chartFrame.setVisible(true);
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
