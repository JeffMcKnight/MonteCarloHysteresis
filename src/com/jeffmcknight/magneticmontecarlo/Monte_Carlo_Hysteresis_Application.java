/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

import info.monitorenter.gui.chart.demos.MinimalStaticChart;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;

// ********** class - Monte_Carlo_Hysteresis_Application **********
/**
 * @author jeffmcknight
 *
 */
public class Monte_Carlo_Hysteresis_Application extends JFrame 
{
	public static final String VERSION_NUMBER = "0.6.1";
	public static final String VERSION_SUFFIX = "0-6-1";
	public static final String TITLE_BAR_TEXT = "Monte Carlo Hysteresis " + VERSION_NUMBER;

	/**
	 * @throws HeadlessException
	 */
	public Monte_Carlo_Hysteresis_Application() throws HeadlessException 
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public Monte_Carlo_Hysteresis_Application(GraphicsConfiguration arg0) 
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public Monte_Carlo_Hysteresis_Application(String arg0)
			throws HeadlessException 
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public Monte_Carlo_Hysteresis_Application(String arg0,
			GraphicsConfiguration arg1) 
	{
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	// ******************** main() ********************
	public static void main(String [] args) 
	{
		Monte_Carlo_Hysteresis_Application app = new Monte_Carlo_Hysteresis_Application();
		app.createGUI();
	}

	// ******************** createGUI() ********************
	private void createGUI() 
	{
		//Create and set up the content pane.
		MonteCarloHysteresisPanel newContentPane = new MonteCarloHysteresisPanel();
		newContentPane.setOpaque(true); 
		setContentPane(newContentPane);
		setTitle(TITLE_BAR_TEXT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}  
	
} // END ********** class - Monte_Carlo_Hysteresis_Application **********

