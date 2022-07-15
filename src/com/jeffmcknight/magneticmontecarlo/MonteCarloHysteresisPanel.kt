package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.CurveFamily.CurveFamilyListener
import com.jeffmcknight.magneticmontecarlo.MagneticMedia.MagneticMediaListener
import com.jeffmcknight.magneticmontecarlo.model.DipoleAverages
import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import info.monitorenter.gui.chart.Chart2D
import info.monitorenter.gui.chart.ITrace2D
import info.monitorenter.gui.chart.traces.Trace2DSimple
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.*

/**
 *
 */
class MonteCarloHysteresisPanel(private val viewModel: ViewModel, coroutineScope: CoroutineScope) : JPanel(), ActionListener {

    private var mMagneticMedia: MagneticMedia? = null

    // ******************** getmhCurves() ********************
    // @return mhCurves
    var mhCurves: CurveFamily? = null
        private set
    private val mXComboBox = JComboBox(LATTICE_SIZES).apply {
        addItemListener {
            if (it.isSelected()) {
                viewModel.setLatticeDimenX(it.item as Int)
            }
        }
    }
    private val mYComboBox = JComboBox(LATTICE_SIZES).apply {
        addItemListener {
            if (it.isSelected()) {
                viewModel.setLatticeDimenY(it.item as Int)
            }
        }
    }
    private val mZComboBox = JComboBox(LATTICE_SIZES).apply {
        addItemListener {
            if (it.isSelected()) {
                viewModel.setLatticeDimenZ(it.item as Int)
            }
        }
    }
    private val dipoleRadiusList = JComboBox(DIPOLE_RADIUS_OPTIONS).apply {
        addItemListener {
            if (it.isSelected()) {
                viewModel.setDipoleRadius(it.item as Float)
            }
        }
    }
    private val mPackingFractionBox = JComboBox(PACKING_FRACTION_OPTIONS).apply {
        addItemListener {
            if (it.isSelected()) {
                viewModel.setPackingFraction(it.item as Float)
            }
        }
    }
    private val mRecordCountBox = JComboBox(RECORDING_PASS_COUNT).apply {
        addItemListener {
            if (it.isSelected()) {
                viewModel.recordCount = (it.item as Int)
            }
        }
    }

    /**
     * The field applied to the [MagneticMedia]
     */
    private val mAppliedFieldRangeBox = JComboBox(H_FIELD_RANGE_ITEMS).apply {
        addItemListener {
            if (it.isSelected()) {
                viewModel.appliedField = (it.item as Float)
            }
        }
    }
    private val mRadioButtonGroup = ButtonGroup()
    private val mAveragedDipoleRadioButton = JRadioButton()
    private val mCurveRadioButton = JRadioButton()
    private val mDipoleRadioButton = JRadioButton()

    // ******************** getmhChart() ********************
    // @return mhChart
    private val bhChart = Chart2D()

    // Create a frame.
    private var mCurveTraceCount = 0
    private var mDipoleTraceCount = 0
    private val chartFrame: JFrame? = null
    private val mChartPanel: JPanel
    private val mControlsPanel: JPanel
    private var traceColor = Color(255, 0, 0)
    private var traceHue = 0f
    private val mTrace: ITrace2D? = null
    private var mActiveChart: ChartType
    private var mDipoleUpdateListener: MagneticMediaListener? = null
    private var mChartUpdateListener: MagneticMediaListener? = null
    private var mCurveFamilyListener: CurveFamilyListener? = null
    private var mCumulativeAverageTrace: MovingAverageTrace2D? = null
    private var mTwoPointAverageTrace: MovingAverageTrace2D? = null
    private var mScaledWindowTrace: MovingAverageTrace2D? = null
    private var mScaledTotalTrace: MovingAverageTrace2D? = null

    init {
        CurveFamily.getDefaultRecordPoints()
        mActiveChart = ChartType.MH_CURVE
        // Create a frame.
        this.layout = BoxLayout(this, BoxLayout.X_AXIS)
        mControlsPanel = JPanel()
        mControlsPanel.layout = BoxLayout(mControlsPanel, BoxLayout.PAGE_AXIS)
        this.add(mControlsPanel)
        mChartPanel = JPanel()
        mChartPanel.layout = BoxLayout(mChartPanel, BoxLayout.X_AXIS)
        this.add(mChartPanel)
        buildRunConfigPanel()
        showChart(mChartPanel)
        implementDipoleChartListener()
        implementCurveChartListener()
        implementCurveFamilyListener()

        //FIXME: is this the right scope/way to collect items emitted by recordingDoneFlo ?
        coroutineScope.launch {
            viewModel.recordSingleFlo.collect {
                showDipoleTraces(it)
            }
        }
        coroutineScope.launch {
            viewModel.dipoleAverageFlo.collect {
                showDipoleAverages(it)
            }
        }
    }

    private fun showDipoleAverages(dipoleAverages: DipoleAverages) {
        mAppliedFieldRangeBox.name = "mAppliedFieldRangeBox"
        val dipoleAveragesTrace = Trace2DSimple().apply {
            name = "Averages Dipoles; Ordered by Coercivity.  Recording Passes: ${dipoleAverages.count}"
            color = Color.GREEN.darker()
            setTracePainter(TracePainterDisc())
        }
        with(bhChart) {
            removeAllTraces()
            addTrace(dipoleAveragesTrace)
            axisX.axisTitle.title = "n [Dipole rank by coercivity]"
        }
        dipoleAverages.dipoles.forEachIndexed { index, dipoleH -> dipoleAveragesTrace.addPoint(index.toDouble(), dipoleH.toDouble()) }
    }

    /**
     * Redraws dipole chart when the MagneticMedia notifies that it has updated itself
     */
    private fun implementDipoleChartListener() {
        mDipoleUpdateListener = object : MagneticMediaListener {
            override fun onRecordingDone(magneticMedia: MagneticMedia) {
                showDipoleTraces(magneticMedia)
            }

            override fun onDipoleFixated(dipoleSphere3f: DipoleSphere3f) {
                println("$TAG\t -- notifyDipoleStuck()\t -- dipoleSphere3f.getM(): ${dipoleSphere3f.m}"
                )
            }
        }
    }

    /**
     * Redraws MH CurveFamily chart when the CurveFamily object notifies that it has updated itself
     */
    private fun implementCurveFamilyListener() {
        mCurveFamilyListener = CurveFamilyListener { curveFamily -> addMhPoints(curveFamily, mTrace) }
    }

    /**
     *
     */
    private fun implementCurveChartListener() {
        mChartUpdateListener = object : MagneticMediaListener {
            /**
             * Stub
             * TODO: do we need this?
             */
            override fun onRecordingDone(magneticMedia: MagneticMedia) { }

            /**
             * Stub
             * TODO: do we need this?
             */
            override fun onDipoleFixated(dipoleSphere3f: DipoleSphere3f) { }
        }
    }
    // *************** buildRunConfigPanel() ***************
    /**
     * Build JPanel on left side of JFrame that contains
     * labeled JComboBoxes to set simulation parameters
     */
    fun buildRunConfigPanel() {
        val initialComboIndex = 8

        // Create combo boxes for lattice parameters
        with(mCurveRadioButton) {
            text = CURVE_BUTTON_TEXT
            isSelected = true
            addActionListener { showMhCurveChart() }
        }

        with(mDipoleRadioButton) {
            text = DIPOLE_BUTTON_TEXT
            addActionListener {
                mActiveChart = ChartType.MH_CURVE_POINT
                showDipoleTraces(mMagneticMedia)
            }
        }
        with(mAveragedDipoleRadioButton) {
            text = AVERAGED_DIPOLE_BUTTON_TEXT
            mAveragedDipoleRadioButton.addActionListener {
                mActiveChart = ChartType.DIPOLE_AVERAGES
                showDipoleTraces(mMagneticMedia)
            }
        }

        // Create the group of radio buttons to control the type of chart to show
        with(mRadioButtonGroup) {
            add(mCurveRadioButton)
            add(mDipoleRadioButton)
            add(mAveragedDipoleRadioButton)
        }

        // Create combo box panels for lattice dimensions
        val xComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_X_AXIS_LABEL, mXComboBox)
        val yComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_Y_AXIS_LABEL, mYComboBox)
        val zComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_Z_AXIS_LABEL, mZComboBox)
        val dipoleRadiusPanel = buildComboBoxPanel(3, DIPOLE_RADIUS_LABEL, dipoleRadiusList)
        val packingFractionPanel = buildComboBoxPanel(1, PACKING_FRACTION_LABEL, mPackingFractionBox)
        val recordCountPanel = buildComboBoxPanel(8, RECORDING_PASSES_LABEL, mRecordCountBox)
        val mAppliedFieldRangePanel = buildComboBoxPanel(DEFAULT_APPLIED_FIELD_ITEM, APPLIED_FIELD_RANGE_LABEL, mAppliedFieldRangeBox)
        val mRadioButtonPanel = buildButtonPanel(mRadioButtonGroup)

        with(mControlsPanel) {
            // Add combo box panels for lattice dimensions
            add(xComboBoxPanel)
            add(yComboBoxPanel)
            add(zComboBoxPanel)
            // Add separator line
            add(JSeparator(SwingConstants.HORIZONTAL))
            // Add combo box panels for dipole radius, packing fraction, etc
            add(dipoleRadiusPanel)
            add(packingFractionPanel)
            add(recordCountPanel)
            add(mAppliedFieldRangePanel)

            // Add vertical space between combo buttons and radio buttons
            add(Box.createRigidArea(Dimension(0, 20)))
            add(mCurveRadioButton)
            add(mDipoleRadioButton)
            add(mAveragedDipoleRadioButton)
        }

        // Add vertical space between radio buttons and run JButton
        mControlsPanel.add(Box.createRigidArea(Dimension(0, 20)))

        // Add run JButton
        val buttonRun: JButton
        buttonRun = JButton("Run")
        buttonRun.verticalTextPosition = AbstractButton.CENTER
        buttonRun.horizontalTextPosition = AbstractButton.CENTER //aka LEFT, for left-to-right locales
        buttonRun.mnemonic = KeyEvent.VK_R
        buttonRun.actionCommand = RUN_SIMULATION
        buttonRun.addActionListener(this)
        buttonRun.toolTipText = "Click to start simulation"
        val buttonRunPanel = JPanel() //create panel for button
        buttonRunPanel.layout = BoxLayout(buttonRunPanel, BoxLayout.X_AXIS)
        buttonRunPanel.add(buttonRun) // add button to panel
        buttonRunPanel.alignmentX = LEFT_ALIGNMENT
        buttonRun.alignmentX = RIGHT_ALIGNMENT
        mControlsPanel.add(buttonRunPanel)
        mControlsPanel.add(Box.createVerticalStrut(300))

        // Add border padding around entire panel
        border = BorderFactory.createEmptyBorder(
                DEFAULT_BORDER_SPACE,
                DEFAULT_BORDER_SPACE,
                DEFAULT_BORDER_SPACE,
                DEFAULT_BORDER_SPACE)
        mControlsPanel.maximumSize = Dimension(200, 600)
    }

    private fun showDipoleTraces(magneticMedia: MagneticMedia?) {
        mAppliedFieldRangeBox.name = APPLIED_FIELD_LABEL
        bhChart.removeAllTraces()
        bhChart.axisX.axisTitle.title = "n [Dipole count]"
        mCumulativeAverageTrace = MovingAverageTrace2D(0, Color.PINK)
        bhChart.addTrace(mCumulativeAverageTrace!!.trace)
        mTwoPointAverageTrace = MovingAverageTrace2D(2, Color.BLUE.brighter())
        bhChart.addTrace(mTwoPointAverageTrace!!.trace)
        mScaledTotalTrace = MovingAverageTrace2D(-1, Color.GREEN.darker())
        bhChart.addTrace(mScaledTotalTrace!!.trace)

        magneticMedia?.let {
            mCumulativeAverageTrace!!.buildTraceName(DIPOLE_CHART_TITLE, it)
            mCumulativeAverageTrace!!.generateMovingAverage(it)
            mTwoPointAverageTrace!!.buildTraceName(DIPOLE_CHART_TITLE, it)
            mTwoPointAverageTrace!!.generateMovingAverage(it)

            // Set moving average to a fraction of the total number of dipoles.
            val averagePeriod = (MOVING_AVERAGE_WINDOW * it.size).toInt()
            mScaledWindowTrace = MovingAverageTrace2D(averagePeriod, Color.RED)
            bhChart.addTrace(mScaledWindowTrace!!.trace)
            mScaledWindowTrace!!.buildTraceName(DIPOLE_CHART_TITLE, it)
            mScaledWindowTrace!!.generateMovingAverage(it)
            mScaledTotalTrace!!.buildTraceName(DIPOLE_CHART_TITLE, it)
            mScaledTotalTrace!!.generateScaledTotal(it)
        }
    }

    /**
     * @param fastAveragePeriod
     * @param magneticCube TODO
     * @return
     */
    private fun buildTrace(fastAveragePeriod: Int, magneticCube: MagneticMedia, traceColor: Color): ITrace2D {
        val trace: ITrace2D
        // Increment the count and update the color
        // to display multiple traces on the same chart.
        mDipoleTraceCount = mDipoleTraceCount + 1
        val traceName = buildTraceName(DIPOLE_CHART_TITLE, mDipoleTraceCount, fastAveragePeriod, magneticCube)
        trace = Trace2DSimple()
        // Set trace properties (name, color, point shape to disc)
        trace.setName(traceName)
        trace.setColor(traceColor)
        trace.setTracePainter(TracePainterDisc())
        return trace
    }

    private fun chartDescription(fastAveragePeriod: Int): String {
        val string: String
        string = when (fastAveragePeriod) {
            0 -> "Cumulative Average"
            -1 -> "Total (Normalized to " + SATURATION_M + ")"
            else -> "Moving Average over $fastAveragePeriod dipoles"
        }
        return string
    }

    /**
     * @param title
     * @param count TODO
     * @param fastAveragePeriod
     * @param magneticMedia TODO
     * @return
     */
    private fun buildTraceName(title: String,
                               count: Int,
                               fastAveragePeriod: Int,
                               magneticMedia: MagneticMedia): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        return StringBuilder(title)
                .append(count.toString())
                .append(" : ")
                .append(magneticMedia.xCount)
                .append("x")
                .append(magneticMedia.yCount)
                .append("x")
                .append(magneticMedia.zCount)
                .append(" - Packing Fraction: ")
                .append(magneticMedia.packingFraction)
                .append(" - Radius [um]: ")
                .append(magneticMedia.dipoleRadius)
                .append(" - Date: ")
                .append(calendar[Calendar.YEAR])
                .append("-")
                .append(calendar[Calendar.MONTH])
                .append("-")
                .append(calendar[Calendar.DATE])
                .append(" T")
                .append(calendar[Calendar.HOUR_OF_DAY])
                .append(":")
                .append(calendar[Calendar.MINUTE])
                .append(":")
                .append(calendar[Calendar.SECOND])
                .append(" -- ")
                .append(chartDescription(fastAveragePeriod))
                .toString()
    }

    protected fun showMhCurveChart() {
        mActiveChart = ChartType.MH_CURVE
        mAppliedFieldRangeBox.name = APPLIED_FIELD_RANGE_LABEL
        bhChart.axisX.axisTitle.title = "H [nWb]"
        bhChart.removeAllTraces()
        // Show data points only if we have already run a simulation.
        if (null != mhCurves) {
            addMhPoints(mhCurves!!, mTrace)
        }
    }

    // *************** buildButtonPanel() ***************
    private fun buildButtonPanel(buttonGroup: ButtonGroup): JPanel {
        return JPanel()
    }
    // *************** buildComboBoxPanel() ***************
    /**
     * @param initialComboIndex
     * @param label TODO
     * @param comboBox TODO
     * @return TODO
     */
    fun buildComboBoxPanel(initialComboIndex: Int, label: String?, comboBox: JComboBox<*>): JPanel {
        val comboBoxLabel = JLabel(label)
        val panel = JPanel()
        comboBox.isEditable = true
        comboBox.addActionListener(this)
        comboBox.alignmentX = LEFT_ALIGNMENT
        comboBox.selectedIndex = initialComboIndex
        panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
        panel.add(comboBoxLabel)
        panel.add(comboBox)
        panel.alignmentX = LEFT_ALIGNMENT
        panel.maximumSize = Dimension(300, 0)
        return panel
    }

    fun getMediaGeometry(): MediaGeometry {
        // Capture input from all combo boxes
        val xAxisCount: Int = (mXComboBox.selectedItem as Int)
        println("xAxisCount: $xAxisCount")
        val yAxisCount: Int = (mYComboBox.selectedItem as Int)
        println("yAxisCount: $yAxisCount")
        val zAxisCount: Int = (mZComboBox.selectedItem as Int)
        println("zAxisCount: $zAxisCount")
        val dipoleRadius: Float = (dipoleRadiusList.selectedItem as Float)
        println("dipoleRadius: $dipoleRadius")
        val packingFraction: Float = (mPackingFractionBox.selectedItem as Float)
        println("packingFraction: $packingFraction")
        return MediaGeometry(xAxisCount, yAxisCount, zAxisCount, packingFraction, dipoleRadius)
    }

    /**
     * Implements ActionListener callback method
     * @param e
     */
    override fun actionPerformed(e: ActionEvent) {

        val actionCommand: String = e.actionCommand
        runSimulation(actionCommand)
    } // END ******************** actionPerformed() ********************

    public fun runSimulation(actionCommand: String) {
        val recordCount: Int = (mRecordCountBox.selectedItem as Int)
        println("recordCount: $recordCount")
        val geometry: MediaGeometry = getMediaGeometry()
        val maxAppliedField: Float = getAppliedField()
        println("maxAppliedField: $maxAppliedField")
        // Run simulation if run button is clicked
        if (actionCommand == RUN_SIMULATION) {
            when (mActiveChart) {
                ChartType.DIPOLE_AVERAGES -> viewModel.recordPoint()
                ChartType.MH_CURVE -> {
                    mhCurves = CurveFamily(
                            recordCount,
                            geometry,
                            maxAppliedField,
                            mChartUpdateListener,
                            mCurveFamilyListener)
                    mhCurves!!.recordMHCurves()
                }
                ChartType.MH_CURVE_POINT -> viewModel.recordSingle(maxAppliedField, geometry)
            }
        }
    }

    private fun getAppliedField() = (mAppliedFieldRangeBox.selectedItem as Float)

    fun showChart(panel: JPanel) {
        // Set chart axis titles
        bhChart.axisX.axisTitle.title = "H [nWb]"
        bhChart.axisY.axisTitle.title = "B"

        // Show chart grids for both x and y axis
        bhChart.axisX.isPaintGrid = true
        bhChart.axisY.isPaintGrid = true

        // Make it visible:
        // add the chart to the frame:
        panel.add(bhChart)
        panel.minimumSize = Dimension(800, 600)
        panel.setLocation(0, 0)
        panel.isVisible = true
    }
    // *************** addAllPoints() ***************
    /**
     * @param chartCurves
     * @param trace
     */
    fun addMhPoints(chartCurves: CurveFamily, trace: ITrace2D?) {
        // Increment the count and update the color
        // to display multiple traces on the same chart.
        mCurveTraceCount = mCurveTraceCount + 1
        traceColor = Color.getHSBColor(traceHue, 1f, 0.85f)
        traceHue = traceHue + 0.22f
        val traceName = buildTraceName(CURVE_CHART_TITLE, mCurveTraceCount, -1, mhCurves!!.magneticCube)
        var trace = trace
        trace = Trace2DSimple()
        // Set trace properties (name, color, point shape to disc)
        trace.setName(traceName)
        trace.setColor(traceColor)
        trace.setTracePainter(TracePainterDisc())
        // Add the trace to the chart. This has to be done before adding points (deadlock prevention):
        bhChart.addTrace(trace)
        for (i in 0 until chartCurves.getAverageMCurve().length) {
            trace.addPoint(chartCurves.getAverageMCurve().getDipole(i).getH().toDouble(), chartCurves.getAverageMCurve().getDipole(i).getM().toDouble())
        }
    }

    companion object {
        private val TAG = MonteCarloHysteresisPanel::class.java.simpleName
        private const val serialVersionUID = 5824180412325621552L
        const val DEFAULT_BORDER_SPACE = 30
        const val DEFAULT_APPLIED_FIELD_ITEM = 1
        const val SATURATION_M = 100.0f
        const val DEFAULT_INDEX_A = 1.0f
        const val MOVING_AVERAGE_WINDOW = 0.1
        const val CURVE_CHART_TITLE = "Curve #"
        const val DIPOLE_CHART_TITLE = "Dipole Set: "
        const val DIMENSIONS_X_AXIS_LABEL = "Lattice dimensions (X-axis):  "
        const val DIMENSIONS_Y_AXIS_LABEL = "Lattice dimensions (Y-axis):  "
        const val DIMENSIONS_Z_AXIS_LABEL = "Lattice dimensions (Z-axis):  "
        const val DIPOLE_RADIUS_LABEL = "Dipole radius [um]:               "
        const val PACKING_FRACTION_LABEL = "Packing Fraction:                  "
        const val RECORDING_PASSES_LABEL = "Number of Recording Passes:  "
        const val APPLIED_FIELD_RANGE_LABEL = "Maximum Applied Field (H)"
        const val APPLIED_FIELD_LABEL = "Applied Field (H)"
        const val AVERAGED_DIPOLE_BUTTON_TEXT = "Show Averaged Dipoles"
        const val CURVE_BUTTON_TEXT = "Show M-H Curve"
        const val DIPOLE_BUTTON_TEXT = "Show Dipole"
        internal const val RUN_SIMULATION = "run_simulation"
        val LATTICE_SIZES = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        val RECORDING_PASS_COUNT = arrayOf(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192)
        val PACKING_FRACTION_OPTIONS = arrayOf(1.0F, 0.9F, 0.8F, 0.7F, 0.6F, 0.5F, 0.4F, 0.3F, 0.2F, 0.1F)
        val DIPOLE_RADIUS_OPTIONS = arrayOf(0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F, 0.9F, 1.0F, 1.1F, 1.2F, 1.3F, 1.4F, 1.5F, 1.6F, 1.7F, 1.8F, 1.9F, 2.0F)
        val H_FIELD_RANGE_ITEMS = arrayOf(0F, 10F, 20F, 30F, 40F, 50F, 60F, 70F, 80F, 90F, 100F, 110F, 120F, 130F, 140F, 150F, 160F, 170F, 180F, 190F, 200F)
        var frame: JFrame? = null
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Any.castOrThrow(): T {
    return (this as? T) ?: throw ClassCastException()
}

private fun ItemEvent.isSelected(): Boolean {
    return stateChange == ItemEvent.SELECTED
}
