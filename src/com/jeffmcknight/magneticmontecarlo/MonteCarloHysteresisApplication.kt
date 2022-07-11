/**
 *
 */
package com.jeffmcknight.magneticmontecarlo

import info.monitorenter.gui.chart.io.FileFilterExtensions
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.HeadlessException
import javax.swing.*
import kotlin.system.exitProcess

// ********** class - Monte_Carlo_Hysteresis_Application **********
/**
 * The application launcher class.  The JVM launcher runs [main] to start the application.
 *
 * TODO: add a [JFileChooser] to select files to export [DipoleSphere3f] lists
 * TODO: handle [HeadlessException]s
 * @author jeffmcknight
 */
class MonteCarloHysteresisApplication : JFrame() {
    private val mPrimaryModifierKey: Int
    private val mOperatingSystem: OperatingSystem
    /** the File Menu */
    private val mFileMenu = JMenu(FILE_MENU_NAME)
    /**
     * the Export item in the File Menu
     * TODO: implement addActionListener
     */
    private val mExportMenuItem = JMenuItem(EXPORT_MENU_ITEM_NAME, KeyEvent.VK_E)
    /** the Save item in the File Menu */
    private val mSaveMenuItem = JMenuItem(SAVE_MENU_ITEM_NAME, KeyEvent.VK_S)
    /**
     * the Quit item in the File Menu
     * TODO: implement addActionListener
     */
    private val mQuitMenuItem = JMenuItem(QUIT_MENU_ITEM_NAME, KeyEvent.VK_Q)

    /** The [JPanel] that displays the simulation data */
    private val mContentPane = MonteCarloHysteresisPanel().apply { isOpaque = true }

    enum class OperatingSystem {
        MacOSX, Windows, Linux, Unknown
    }

    /**
     *
     */
    init {
        mOperatingSystem = typeOfOperatingSystem(System.getProperty(OS_NAME_PROPERTY))
        mPrimaryModifierKey = assignPrimaryModifierKey(mOperatingSystem)
        buildFileMenu()
        buildPanel()
    }

    /**
     *
     */
    private fun assignPrimaryModifierKey(operatingSystem: OperatingSystem): Int {
        return when (operatingSystem) {
            OperatingSystem.MacOSX -> ActionEvent.META_MASK
            OperatingSystem.Windows -> ActionEvent.CTRL_MASK
            else -> ActionEvent.CTRL_MASK
        }
    }
    // *************** () ***************
    /**
     * @param osName TODO
     * @return
     */
    private fun typeOfOperatingSystem(osName: String): OperatingSystem {
        val operatingSystem: OperatingSystem = when {
            osName == SYSTEM_NAME_OS_X -> OperatingSystem.MacOSX
            osName.startsWith(SYSTEM_NAME_WINDOWS) -> OperatingSystem.Windows
            osName.startsWith(SYSTEM_NAME_LINUX) -> OperatingSystem.Linux
            else -> OperatingSystem.Unknown
        }
        println("$OS_NAME_PROPERTY: $operatingSystem")
        return operatingSystem
    }
    // *************** buildFileMenu() ***************
    /**
     */
    private fun buildFileMenu() {
        with(mSaveMenuItem) {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, mPrimaryModifierKey)
            accessibleContext.accessibleDescription = SAVE_MENU_ITEM_DESCRIPTION
            addActionListener {
                println("$TAG - actionPerformed()")
                val fileChooser = JFileChooser().apply {
                    toolTipText = "Save curve data to .csv file."
                    fileFilter = FileFilterExtensions(csvExtension)
                }
                when (fileChooser.showSaveDialog(null)) {
                    JFileChooser.APPROVE_OPTION -> {
                        println("$TAG - actionPerformed(): APPROVE_OPTION\t - currentDirectory: ${fileChooser.currentDirectory}\t - currentFile: ${fileChooser.selectedFile}")
                        mContentPane.mhCurves?.writeCurvesToFile(fileChooser.currentDirectory, fileChooser.selectedFile)
                    }
                    JFileChooser.CANCEL_OPTION -> println("$TAG - actionPerformed(): CANCEL_OPTION")
                    JFileChooser.ERROR_OPTION -> println("$TAG - actionPerformed(): ERROR_OPTION")
                    else -> {}
                }
            }
        }
        with(mExportMenuItem) {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E, mPrimaryModifierKey)
            accessibleContext.accessibleDescription = EXPORT_MENU_ITEM_DESCRIPTION
        }
        with(mQuitMenuItem){
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_Q, mPrimaryModifierKey)
            accessibleContext.accessibleDescription = QUIT_MENU_ITEM_DESCRIPTION
            addActionListener { exitProcess(0) }
        }
        with(mFileMenu) {
            mnemonic = KeyEvent.VK_F
            accessibleContext.accessibleDescription = "File Menu"
            add(mSaveMenuItem)
            add(mExportMenuItem)
            // Separate the Export menu item from the Quit item
            addSeparator()
            add(mQuitMenuItem)
        }
        this.jMenuBar = JMenuBar().apply {
            add(mFileMenu)
        }
    }

    // ******************** buildPanel() ********************
    private fun buildPanel() {
        contentPane = mContentPane
        title = TITLE_BAR_TEXT
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1000, 600)
        //		mContentPane.setMinimumSize(new Dimension(800, 600));
        pack()
        isVisible = true
    }

    companion object {
        val TAG: String = MonteCarloHysteresisApplication::class.java.simpleName
        const val VERSION_NUMBER = "0.9.0"
        private const val serialVersionUID = 5700991704955056064L
        const val TITLE_BAR_TEXT = "Monte Carlo Hysteresis " + VERSION_NUMBER
        const val OS_NAME_PROPERTY = "os.name"
        const val SYSTEM_NAME_LINUX = "Linux"
        const val SYSTEM_NAME_OS_X = "Mac OS X"
        const val SYSTEM_NAME_WINDOWS = "Windows"
        const val FILE_MENU_NAME = "File"
        const val EXPORT_MENU_ITEM_NAME = "Export"
        const val QUIT_MENU_ITEM_NAME = "Quit"
        const val SAVE_MENU_ITEM_NAME = "Save"
        const val MENU_ITEM = "menu item"
        val csvExtension = arrayOf("csv")
        const val EXPORT_MENU_ITEM_DESCRIPTION = EXPORT_MENU_ITEM_NAME + " " + MENU_ITEM
        const val QUIT_MENU_ITEM_DESCRIPTION = QUIT_MENU_ITEM_NAME + " " + MENU_ITEM
        const val SAVE_MENU_ITEM_DESCRIPTION = SAVE_MENU_ITEM_NAME + " " + MENU_ITEM

        // ******************** main() ********************
        @JvmStatic
        fun main(args: Array<String>) {
            MonteCarloHysteresisApplication()
        }
    }
}
