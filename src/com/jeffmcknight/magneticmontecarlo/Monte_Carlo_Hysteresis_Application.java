/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

import info.monitorenter.gui.chart.io.FileFilterExtensions;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// ********** class - Monte_Carlo_Hysteresis_Application **********
/**
 * @author jeffmcknight
 *
 */
public class Monte_Carlo_Hysteresis_Application extends JFrame 
{
   /**
    * 
    */
   public static final String VERSION_NUMBER = "0.8.0";
   public static final String VERSION_SUFFIX = "0-8-0";
   public static final String TAG = Monte_Carlo_Hysteresis_Application.class.getSimpleName();
   private static final long serialVersionUID = 5700991704955056064L;
   public static final String TITLE_BAR_TEXT = "Monte Carlo Hysteresis " + VERSION_NUMBER;
   private static final String OS_NAME_PROPERTY = "os.name";
   private static final String SYSTEM_NAME_LINUX = "Linux";
   private static final String SYSTEM_NAME_OS_X = "Mac OS X";
   private static final String SYSTEM_NAME_WINDOWS = "Windows";
   private static final String FILE_MENU_NAME = "File";
   private static final String EXPORT_MENU_ITEM_NAME = "Export";
   private static final String QUIT_MENU_ITEM_NAME = "Quit";
   private static final String SAVE_MENU_ITEM_NAME = "Save";
   private static final String MENU_ITEM = "menu item";
   private static final String EXPORT_MENU_ITEM_DESCRIPTION = EXPORT_MENU_ITEM_NAME+" "+MENU_ITEM;
   private static final String QUIT_MENU_ITEM_DESCRIPTION = QUIT_MENU_ITEM_NAME+" "+MENU_ITEM;;
   private static final String SAVE_MENU_ITEM_DESCRIPTION = SAVE_MENU_ITEM_NAME+" "+MENU_ITEM;

   private OperatingSystem mOperatingSystem;
   private JMenuBar mMenuBar;
	private JMenu mFileMenu;
   private JMenuItem mExportMenuItem;
   private JMenuItem mQuitMenuItem;
   private JMenuItem mSaveMenuItem;
   JFileChooser mFileChooser;
   private MonteCarloHysteresisPanel mContentPane;
   
   enum OperatingSystem{
      MacOSX, Windows, Linux, Unknown
   }

	/**
	 * @throws HeadlessException
	 */
	public Monte_Carlo_Hysteresis_Application() throws HeadlessException 
	{
	   setOs(System.getProperty(OS_NAME_PROPERTY));
	   buildFileMenu(); 
	   buildPanel();
	}

   // *************** () ***************
   /**
    * @param osName TODO
    */
   public void setOs(String osName)
   {
      if (osName.equals(SYSTEM_NAME_OS_X))
         mOperatingSystem = OperatingSystem.MacOSX;
      else if (osName.equals(SYSTEM_NAME_WINDOWS))
         mOperatingSystem = OperatingSystem.Windows;
      else if (osName.equals(SYSTEM_NAME_LINUX))
         mOperatingSystem = OperatingSystem.Linux;
      else 
         mOperatingSystem = OperatingSystem.Unknown;
         
	   System.out.println(OS_NAME_PROPERTY+": "+mOperatingSystem);
//	   System.out.println((mOsName=)?:);
   }

   // *************** buildFileMenu() ***************
   /**
    */
   public void buildFileMenu()
   {
      
      mMenuBar = new JMenuBar();

	   //Build the File menu.
	   mFileMenu = new JMenu(FILE_MENU_NAME);
	   mFileMenu.setMnemonic(KeyEvent.VK_F);
	   mFileMenu.getAccessibleContext().setAccessibleDescription("The file menu");
	   mMenuBar.add(mFileMenu);

	   //the Save JMenuItem
	   mSaveMenuItem = new JMenuItem(SAVE_MENU_ITEM_NAME, KeyEvent.VK_S);
//	   mSaveMenuItem.setEnabled(false);
	   mSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
	         KeyEvent.VK_S, ActionEvent.META_MASK));
	   mSaveMenuItem.getAccessibleContext().setAccessibleDescription(SAVE_MENU_ITEM_DESCRIPTION);
	   mSaveMenuItem.addActionListener(new ActionListener()
      {
         
         @Override
         public void actionPerformed(ActionEvent e)
         {
            System.out.println(TAG+" - actionPerformed()");
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setToolTipText("Save curve data to .csv file.");
            String[] extensionsWithoutDot = {"csv"};
            fileChooser.setFileFilter(new FileFilterExtensions(extensionsWithoutDot));
            int returnVal = fileChooser.showSaveDialog(null);
            switch (returnVal)
            {
            case JFileChooser.APPROVE_OPTION:
               File currentDirectory = fileChooser.getCurrentDirectory();
               File currentFile = fileChooser.getSelectedFile();
               System.out.println(TAG+" - actionPerformed(): " +
               		"\t - returnVal" + returnVal
                     +"\t - currentDirectory: "+currentDirectory
                     +"\t - currentFile: "+currentFile
               		);
               if (null != mContentPane.getMhCurves()){
                  mContentPane.getMhCurves().writeCurvesToFile(currentDirectory, currentFile);
               }
               break;
            case JFileChooser.CANCEL_OPTION:
               System.out.println(TAG+" - actionPerformed(): "+returnVal);
               
               break;
            case JFileChooser.ERROR_OPTION:
               System.out.println(TAG+" - actionPerformed(): "+returnVal);
               
               break;

            default:
               break;
            }
         }
      });
	   mFileMenu.add(mSaveMenuItem);
	   
      //the Export JMenuItem
     mExportMenuItem = new JMenuItem(EXPORT_MENU_ITEM_NAME, KeyEvent.VK_E);
     mExportMenuItem.setAccelerator(KeyStroke.getKeyStroke(
             KeyEvent.VK_E, ActionEvent.META_MASK));
     mExportMenuItem.getAccessibleContext().setAccessibleDescription(EXPORT_MENU_ITEM_DESCRIPTION);
     mFileMenu.add(mExportMenuItem);
     
     // Separate the Export menu item from the Quit item
     mFileMenu.addSeparator();

     //the Quit JMenuItem
    mQuitMenuItem = new JMenuItem(QUIT_MENU_ITEM_NAME, KeyEvent.VK_Q);
    mQuitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_Q, ActionEvent.META_MASK));
    mQuitMenuItem.getAccessibleContext().setAccessibleDescription(QUIT_MENU_ITEM_DESCRIPTION);
    mFileMenu.add(mQuitMenuItem);

    this.setJMenuBar(mMenuBar);
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

	// ******************** buildPanel() ********************
	private void buildPanel() 
	{
		mContentPane = new MonteCarloHysteresisPanel();
		mContentPane.setOpaque(true); 
		setContentPane(mContentPane);
		setTitle(TITLE_BAR_TEXT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(1000, 600));
//		mContentPane.setMinimumSize(new Dimension(800, 600));
		pack();
		setVisible(true);
	}  
	
   // ******************** main() ********************
   public static void main(String [] args) 
   {
      Monte_Carlo_Hysteresis_Application app = new Monte_Carlo_Hysteresis_Application();
   }

} // END ********** class - Monte_Carlo_Hysteresis_Application **********

