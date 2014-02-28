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
   public static final String VERSION_NUMBER = "0.8.4";
   public static final String VERSION_SUFFIX = "0-8-4";
   public static final String TAG = Monte_Carlo_Hysteresis_Application.class.getSimpleName();
   private static final long serialVersionUID = 5700991704955056064L;
   public static final String TITLE_BAR_TEXT = "Monte Carlo Hysteresis " + VERSION_NUMBER;
   public static final String OS_NAME_PROPERTY = "os.name";
   public static final String SYSTEM_NAME_LINUX = "Linux";
   public static final String SYSTEM_NAME_OS_X = "Mac OS X";
   public static final String SYSTEM_NAME_WINDOWS = "Windows";
   public static final String FILE_MENU_NAME = "File";
   public static final String EXPORT_MENU_ITEM_NAME = "Export";
   public static final String QUIT_MENU_ITEM_NAME = "Quit";
   public static final String SAVE_MENU_ITEM_NAME = "Save";
   public static final String MENU_ITEM = "menu item";
   public static final String EXPORT_MENU_ITEM_DESCRIPTION = EXPORT_MENU_ITEM_NAME+" "+MENU_ITEM;
   public static final String QUIT_MENU_ITEM_DESCRIPTION = QUIT_MENU_ITEM_NAME+" "+MENU_ITEM;;
   public static final String SAVE_MENU_ITEM_DESCRIPTION = SAVE_MENU_ITEM_NAME+" "+MENU_ITEM;

   private int mPrimaryModifierKey;
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
	 * Constructor - zero argument
	 * @throws HeadlessException
	 */
   public Monte_Carlo_Hysteresis_Application() throws HeadlessException 
   {
	   mOperatingSystem = typeOfOperatingSystem(System.getProperty(OS_NAME_PROPERTY));
	   mPrimaryModifierKey = assignPrimaryModifierKey(mOperatingSystem);
	   buildFileMenu(); 
	   buildPanel();
   }

	/**
	 * 
	 */
	private int assignPrimaryModifierKey(OperatingSystem operatingSystem) {
		switch (operatingSystem) {
		   case MacOSX:
			   return ActionEvent.META_MASK;
		   case Windows:
			   return ActionEvent.CTRL_MASK;
		   default:
			   return ActionEvent.CTRL_MASK;
		   }
	}

   // *************** () ***************
   /**
    * @param osName TODO
 * @return 
    */
   public OperatingSystem typeOfOperatingSystem(String osName)
   {
	   OperatingSystem operatingSystem;
      if (osName.equals(SYSTEM_NAME_OS_X))
    	  operatingSystem = OperatingSystem.MacOSX;
      else if (osName.startsWith(SYSTEM_NAME_WINDOWS))
    	  operatingSystem = OperatingSystem.Windows;
      else if (osName.startsWith(SYSTEM_NAME_LINUX))
    	  operatingSystem = OperatingSystem.Linux;
      else 
    	  operatingSystem = OperatingSystem.Unknown;
         
	   System.out.println(OS_NAME_PROPERTY+": "+operatingSystem);
	   return operatingSystem;
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
	   mSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
	         KeyEvent.VK_S, mPrimaryModifierKey));
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
             KeyEvent.VK_E, mPrimaryModifierKey));
     mExportMenuItem.getAccessibleContext().setAccessibleDescription(EXPORT_MENU_ITEM_DESCRIPTION);
     mFileMenu.add(mExportMenuItem);
     
     // Separate the Export menu item from the Quit item
     mFileMenu.addSeparator();

     //the Quit JMenuItem
    mQuitMenuItem = new JMenuItem(QUIT_MENU_ITEM_NAME, KeyEvent.VK_Q);
    mQuitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_Q, mPrimaryModifierKey));
    mQuitMenuItem.getAccessibleContext().setAccessibleDescription(QUIT_MENU_ITEM_DESCRIPTION);
    mFileMenu.add(mQuitMenuItem);

    this.setJMenuBar(mMenuBar);
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

