package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.ChartType.*
import com.jeffmcknight.magneticmontecarlo.model.AppliedField
import info.monitorenter.gui.chart.Chart2D
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
 * UI that displays the data as charts.
 *
 * TODO: add chart for interaction field standard deviation
 * TODO: move radio button handling logic to ViewModel (I think we're doing processing for charts that we're not showing)
 * TODO: add stop button to stop recordings in progress
 * TODO: add clear button to clear charted data
 * TODO: reduce chart jank (throttle Flow emissions?)
 *
 */
class MonteCarloHysteresisPanel(private val viewModel: ViewModel, coroutineScope: CoroutineScope) : JPanel(), ActionListener {

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
                viewModel.appliedField = (it.item as AppliedField)
            }
        }
    }

    /**
     * The Chart that we draw all our traces onto.
     * TODO: rename or create another chart to display data from [ViewModel.dipoleAverageFlo] and
     * [ViewModel.recordSingleFlo]
     */
    private val bhChart = Chart2D()

    // Create a frame.
    private var mCurveTraceCount = 0
    private val mChartPanel: JPanel
    private val mControlsPanel: JPanel
    private var traceHue = 0f
    private var mActiveChart: ChartType

    init {
        CurveFamily.getDefaultRecordPoints()
        mActiveChart = MH_CURVE
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

        // FIXME: is this the right scope/way to collect items emitted by recordingDoneFlo ?
        // FIXME: make a sealed class and do this routing in the ViewModel
        coroutineScope.launch {
            viewModel.recordSingleFlo.collect { magneticMedia ->
                when (mActiveChart) {
                    MH_CURVE_POINT -> {
                        bhChart.removeAllTraces()
                        showDipoleTraces(magneticMedia)
                    }
                    DIPOLE_AVERAGES, INTERACTION_AVERAGES, MH_CURVE -> {}
                }
            }
        }
        /**
         * TODO: can we just update traces rather than removing and replacing all of them
         */
        coroutineScope.launch {
            viewModel.dipoleAverageFlo.collect { traceDataList ->
                when (mActiveChart) {
                    DIPOLE_AVERAGES -> {
                        bhChart.removeAllTraces()
                        val titleXAxis = "n [Dipole rank by coercivity]"
                        traceDataList.forEach { averages ->
                            val traceName = "Averaged Dipoles\t-- Applied Field: ${averages.appliedField}\t-- Recording Passes: ${averages.count}"
                            val traceColor = averages.color
                            val pointList = averages.dipoles.mapIndexed { index: Int, h: Float -> Point2d(index.toDouble(), h.toDouble()) }
                            showAsTrace(TraceSpec(traceName, titleXAxis, traceColor, pointList, "Recorded Flux [nWb/m]"))
                        }
                    }
                    INTERACTION_AVERAGES, MH_CURVE_POINT, MH_CURVE -> {}
                }
            }
        }
        coroutineScope.launch {
            viewModel.curveFamilyFlo.collect {
                addMhPoints(it)
            }
        }
//        coroutineScope.launch {
//            viewModel.dipoleAveragesFlo.collect { dipoleTraces ->
//                dipoleTraces.forEach { showAsTrace(it) }
//            }
//        }
        coroutineScope.launch {
            viewModel.interactionAverageTraceFlo.collect { traces: List<TraceSpec> ->
                when (mActiveChart) {
                    INTERACTION_AVERAGES -> {
                        bhChart.removeAllTraces()
                        traces.forEach { showAsTrace(it) }
                    }
                    DIPOLE_AVERAGES, MH_CURVE_POINT, MH_CURVE -> {}
                }

            }
        }
    }

    /**
     * Build JPanel on left side of JFrame that contains
     * labeled JComboBoxes to set simulation parameters
     */
    private fun buildRunConfigPanel() {
        val initialComboIndex = 8

        // Create combo boxes for lattice parameters
        val curveRadioButton = JRadioButton().apply {
            text = CURVE_BUTTON_TEXT
            isSelected = true
            addActionListener { showMhCurveChart() }
        }

        val dipoleRadioButton = JRadioButton().apply {
            text = DIPOLE_BUTTON_TEXT
            addActionListener {
                bhChart.removeAllTraces()
                mActiveChart = MH_CURVE_POINT
            }
        }

        val averagedDipoleRadioButton = JRadioButton().apply {
            text = AVERAGED_DIPOLE_BUTTON_TEXT
            addActionListener {
                bhChart.removeAllTraces()
                mActiveChart = DIPOLE_AVERAGES
            }
        }

        val averagedInteractionsRadioButton = JRadioButton().apply {
            text = AVERAGED_INTERACTIONS_BUTTON_TEXT
            addActionListener {
                bhChart.removeAllTraces()
                mActiveChart = INTERACTION_AVERAGES
            }
        }

        // Create the group of radio buttons to control the type of chart to show.  This does not affect the layout; it
        // just causes the selected button to be deselected when another button in the group is selected.
        ButtonGroup().apply {
            add(curveRadioButton)
            add(dipoleRadioButton)
            add(averagedDipoleRadioButton)
            add(averagedInteractionsRadioButton)
        }

        // Create combo box panels for lattice dimensions
        val xComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_X_AXIS_LABEL, mXComboBox)
        val yComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_Y_AXIS_LABEL, mYComboBox)
        val zComboBoxPanel = buildComboBoxPanel(initialComboIndex, DIMENSIONS_Z_AXIS_LABEL, mZComboBox)
        val dipoleRadiusPanel = buildComboBoxPanel(3, DIPOLE_RADIUS_LABEL, dipoleRadiusList)
        val packingFractionPanel = buildComboBoxPanel(1, PACKING_FRACTION_LABEL, mPackingFractionBox)
        val recordCountPanel = buildComboBoxPanel(INITIAL_RECORD_COUNT_INDEX, RECORDING_PASSES_LABEL, mRecordCountBox)
        val mAppliedFieldRangePanel = buildComboBoxPanel(DEFAULT_APPLIED_FIELD_ITEM, APPLIED_FIELD_RANGE_LABEL, mAppliedFieldRangeBox)

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
            add(curveRadioButton)
            add(dipoleRadioButton)
            add(averagedDipoleRadioButton)
            add(averagedInteractionsRadioButton)
        }

        // Add vertical space between radio buttons and run JButton
        mControlsPanel.add(Box.createRigidArea(Dimension(0, 20)))

        // Add run JButton
        val buttonRun: JButton = JButton("Run").apply {
            verticalTextPosition = AbstractButton.CENTER
            horizontalTextPosition = AbstractButton.CENTER //aka LEFT, for left-to-right locales
            mnemonic = KeyEvent.VK_R
            actionCommand = RUN_SIMULATION
            addActionListener(this@MonteCarloHysteresisPanel)
            toolTipText = "Click to start simulation"
            alignmentX = RIGHT_ALIGNMENT
        }
        //create panel for button
        val buttonRunPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(buttonRun) // add button to panel
            alignmentX = LEFT_ALIGNMENT
        }
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

    private fun showDipoleTraces(magneticMedia: MagneticMedia) {
        mAppliedFieldRangeBox.name = APPLIED_FIELD_LABEL
        bhChart.removeAllTraces()
        bhChart.axisX.axisTitle.title = "n [Dipole count]"

        val cumulativeAverageTrace = MovingAverageTrace2D(0, Color.PINK).also {
            bhChart.addTrace(it.trace)
        }
        val twoPointAverageTrace = MovingAverageTrace2D(2, Color.BLUE.brighter()).also {
            bhChart.addTrace(it.trace)
        }
        val scaledTotalTrace = MovingAverageTrace2D(-1, Color.GREEN.darker()).also {
            bhChart.addTrace(it.trace)
        }

        magneticMedia.let {
            cumulativeAverageTrace.buildTraceName(DIPOLE_CHART_TITLE, it)
            cumulativeAverageTrace.generateMovingAverage(it)
            twoPointAverageTrace.buildTraceName(DIPOLE_CHART_TITLE, it)
            twoPointAverageTrace.generateMovingAverage(it)

            // Set moving average to a fraction of the total number of dipoles.
            val averagePeriod = (MOVING_AVERAGE_WINDOW * it.size).toInt()
            val scaledWindowTrace = MovingAverageTrace2D(averagePeriod, Color.RED)
            bhChart.addTrace(scaledWindowTrace.trace)
            scaledWindowTrace.buildTraceName(DIPOLE_CHART_TITLE, it)
            scaledWindowTrace.generateMovingAverage(it)
            scaledTotalTrace.buildTraceName(DIPOLE_CHART_TITLE, it)
            scaledTotalTrace.generateScaledTotal(it)
        }
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
        mActiveChart = MH_CURVE
        mAppliedFieldRangeBox.name = APPLIED_FIELD_RANGE_LABEL
        bhChart.axisX.axisTitle.title = "H [nWb]"
        bhChart.removeAllTraces()
        addMhPoints(viewModel.curveFamilyFlo.value)
    }

    // *************** buildComboBoxPanel() ***************
    /**
     * @param initialComboIndex
     * @param label TODO
     * @param comboBox TODO
     * @return TODO
     */
    private fun buildComboBoxPanel(initialComboIndex: Int, label: String?, comboBox: JComboBox<*>): JPanel {
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
                DIPOLE_AVERAGES, INTERACTION_AVERAGES -> viewModel.recordMultiple()
                MH_CURVE -> viewModel.recordBhCurve()
                MH_CURVE_POINT -> viewModel.recordSingle()
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
        val titleXAxis = "H, applied [nWb]"
        val pointList = chartCurves.getAverageMCurve().recordPoints.map { Point2d(it.h.toDouble(), it.m.toDouble()) }
        showAsTrace(TraceSpec(traceName, titleXAxis, traceColor, pointList,"Recorded Flux [nWb/m]"))
    }

    /**
     * Show the data in [traceSpec] on [bhChart] as a [Trace2DSimple]
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
                bhChart.axisX.axisTitle.title = traceSpec.titleXAxis
                bhChart.axisY.axisTitle.title = traceSpec.titleYAxis
                // Add the recording points to the trace
                traceSpec.pointList.forEach { point -> trace.addPoint(point.x, point.y) }
            }
    }


    companion object {
        private val TAG = MonteCarloHysteresisPanel::class.java.simpleName
        private const val serialVersionUID = 5824180412325621552L
        const val INITIAL_RECORD_COUNT_INDEX = 0
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
        const val AVERAGED_INTERACTIONS_BUTTON_TEXT = "Show Averaged Interaction Fields"
        const val CURVE_BUTTON_TEXT = "Show M-H Curve"
        const val DIPOLE_BUTTON_TEXT = "Show Dipole"
        internal const val RUN_SIMULATION = "run_simulation"
        val LATTICE_SIZES = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        val RECORDING_PASS_COUNT = arrayOf(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192)
        val PACKING_FRACTION_OPTIONS = arrayOf(1.0F, 0.9F, 0.8F, 0.7F, 0.6F, 0.5F, 0.4F, 0.3F, 0.2F, 0.1F)
        val DIPOLE_RADIUS_OPTIONS = arrayOf(0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F, 0.9F, 1.0F, 1.1F, 1.2F, 1.3F, 1.4F, 1.5F, 1.6F, 1.7F, 1.8F, 1.9F, 2.0F)
        val H_FIELD_RANGE_ITEMS = arrayOf(0F, 10F, 20F, 30F, 40F, 50F, 60F, 70F, 80F, 90F, 100F, 110F, 120F, 130F, 140F, 150F, 160F, 170F, 180F, 190F, 200F)
    }
}

private fun ItemEvent.isSelected(): Boolean {
    return stateChange == ItemEvent.SELECTED
}

/**
 * Everything we need to show a trace on a chart.
 * @param name the name to display on the chart
 * @param color the color of he points to display on the chart
 * @param pointList the points to display on the chart
 */
data class TraceSpec(
    val name: String,
    val titleXAxis: String,
    val color: Color,
    val pointList: List<Point2d>,
    val titleYAxis: String
)
