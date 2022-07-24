package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.MagneticMediaListener
import com.jeffmcknight.magneticmontecarlo.model.DipoleAverages
import com.jeffmcknight.magneticmontecarlo.model.Hfield
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
import javax.vecmath.Point2d

/**
 *
 */
class MonteCarloHysteresisPanel(private val viewModel: ViewModel, coroutineScope: CoroutineScope) : JPanel(), ActionListener {

    private var mMagneticMedia: MagneticMedia? = null

    /**
     * A B-H curve trace with data from a set of recorded [MagneticMedia]
     */
    val curveFamily
        get() = viewModel.curveFamilyFlo.value

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
                viewModel.appliedField = (it.item as Hfield)
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
    private var traceHue = 0f
    private var mActiveChart: ChartType
    private var mDipoleUpdateListener: MagneticMediaListener? = null
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
        coroutineScope.launch {
            viewModel.curveFamilyFlo.collect {
                addMhPoints(it)
            }
        }
        coroutineScope.launch {
            viewModel.dipoleAveragesFlo.collect { dipoleTraces ->
                dipoleTraces.forEach { showAsTrace(it) }
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

    // *************** buildRunConfigPanel() ***************
    /**
     * Build JPanel on left side of JFrame that contains
     * labeled JComboBoxes to set simulation parameters
     */
    private fun buildRunConfigPanel() {
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
        val recordCountPanel = buildComboBoxPanel(1, RECORDING_PASSES_LABEL, mRecordCountBox)
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
        return when (fastAveragePeriod) {
            0 -> "Cumulative Average"
            -1 -> "Total (Normalized to " + SATURATION_M + ")"
            else -> "Moving Average over $fastAveragePeriod dipoles"
        }
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

    /**
     * Show [CurveFamily] data points if we have already run a simulation.
     */
    private fun showMhCurveChart() {
        mActiveChart = ChartType.MH_CURVE
        mAppliedFieldRangeBox.name = APPLIED_FIELD_RANGE_LABEL
        bhChart.axisX.axisTitle.title = "H [nWb]"
        bhChart.removeAllTraces()
        addMhPoints(viewModel.curveFamilyFlo.value)
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

    /**
     * Implements ActionListener callback method
     * @param e
     */
    override fun actionPerformed(e: ActionEvent) {
        val actionCommand: String = e.actionCommand
        runSimulation(actionCommand)
    }

    /**
     * Run simulation when run button/menu/hot-key is clicked/selected/typed
     */
    fun runSimulation(actionCommand: String) {
        if (actionCommand == RUN_SIMULATION) {
            when (mActiveChart) {
                ChartType.DIPOLE_AVERAGES -> viewModel.recordMultiple()
                ChartType.MH_CURVE -> viewModel.recordBhCurve()
                ChartType.MH_CURVE_POINT -> viewModel.recordSingle()
            }
        }
    }

    private fun showChart(panel: JPanel) {
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

    /**
     * Create a new [Trace2DSimple] and use it to add points from [chartCurves] to the chart.
     * TODO: find a better way to update the trace color (maybe a list of colors or something?)
     * @param chartCurves a set of B-H points that define a linear-ish recording.
     */
    private fun addMhPoints(chartCurves: CurveFamily) {
        // Increment the count and update the color
        // to display multiple traces on the same chart.
        mCurveTraceCount += 1
        val traceColor = Color.getHSBColor(traceHue, 1f, 0.85f)
        traceHue += 0.22f
        val traceName = buildTraceName(CURVE_CHART_TITLE, mCurveTraceCount, -1, chartCurves.magneticCube)
        val pointList = chartCurves.getAverageMCurve().recordPoints.map { Point2d(it.h.toDouble(), it.m.toDouble()) }
        showAsTrace(TraceSpec(traceName, traceColor, pointList))
    }

    /**
     * Show the [TraceSpec.pointList] on [bhChart] as a [Trace2DSimple]
     */
    private fun showAsTrace(traceSpec: TraceSpec) {
        Trace2DSimple()
            .apply {
                name = traceSpec.name
                color = traceSpec.color
                setTracePainter(TracePainterDisc())
            }.also { trace ->
                // Set trace properties (name, color, point shape to disc)
                // Add the trace to the chart. This has to be done before adding points (deadlock prevention):
                bhChart.addTrace(trace)
                // Add the recording points to the trace
                traceSpec.pointList.forEach { point -> trace.addPoint(point.x, point.y) }
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

private fun ItemEvent.isSelected(): Boolean {
    return stateChange == ItemEvent.SELECTED
}

/**
 * Everything we need to show a trace on a chart.
 * @param name the name to display on the chart
 * @param the color of he points to display on the chart
 * @param pointList the points to display on the chart
 */
data class TraceSpec(val name: String, val color: Color, val pointList: List<Point2d>)