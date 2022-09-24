package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.ChartType.*
import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.empty
import com.jeffmcknight.magneticmontecarlo.interactor.InteractionFieldInteractor
import com.jeffmcknight.magneticmontecarlo.interactor.RecordedFieldInteractor
import com.jeffmcknight.magneticmontecarlo.interactor.SaturationInteractor
import com.jeffmcknight.magneticmontecarlo.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.Color.*
import javax.vecmath.Point2d
import kotlin.math.absoluteValue


/**
 * TODO: unit tests!
 */
class ViewModel(
    private val coroutineScope: CoroutineScope,
    private val repo: Repository,
    saturationInteractor: SaturationInteractor,
    recordedFieldInteractor: RecordedFieldInteractor,
    interactionFieldInteractor: InteractionFieldInteractor
) {

    val curveFamilyFlo = MutableStateFlow(CurveFamily(0, empty.geometry, 0F))
    val recordSingleFlo: Flow<MagneticMedia> = repo.recordingDoneFlo.map { it.magneticMedia }
    var recordCount: Int = 1

    /**
     * The H field applied during recording
     * TODO: convert to MutableStateFlow
     */
    var appliedField: AppliedField = 0.0f
    var activeChart: ChartType = MH_CURVE

    private var mediaGeometry = MediaGeometry()

    /**
     * Add UI-specific data
     */
    private val interactionAverageTraceFlo: Flow<List<TraceSpec>> =
        interactionFieldInteractor.interactionAverageFlo.map { traceDataList: List<InteractionAverages> ->
            traceDataList.mapIndexed { index: Int, averages: InteractionAverages ->
                val titleXAxis = "n [Dipole rank by coercivity]"
                val traceName =
                    "Averaged Interaction at Dipoles\t-- Applied Field: ${averages.appliedField}\t-- Recording Passes: ${averages.count}"
                val traceColor = index.toColor()
                val pointList = averages.averageInteractionFields.mapIndexed { idx: Int, h: InteractionField ->
                    Point2d(idx.toDouble(), h.toDouble())
                }
                TraceSpec(traceName, titleXAxis, traceColor, pointList, "Interaction Field [nWb/m]")
            }
        }

    /**
     * Add UI-specific data
     */
    private val recordedAverageTraceFlo: Flow<List<TraceSpec>> =
        recordedFieldInteractor.dipoleAverageFlo.map { traceDataList: List<DipoleAverages> ->
            traceDataList.mapIndexed { index: Int, averages: DipoleAverages ->
                val titleXAxis = "n [Dipole rank by coercivity]"
                val traceName = "Averaged Dipoles\t-- Applied Field: ${averages.appliedField}\t-- Recording Passes: ${averages.count}"
                val traceColor = index.toColor()
                val pointList = averages.dipoles.mapIndexed { idx: Int, h: RecordedField ->
                    Point2d(idx.toDouble(), h.toDouble())
                }
                TraceSpec(traceName, titleXAxis, traceColor, pointList, "Recorded Flux [nWb/m]")
            }
        }

    /**
     * Merge all the [Flow]s together and filter by the [activeChart] in order to respect the radio button selection.
     * TODO: should we filter further upstream so we're not doing so many calculations every time the
     *          Interactors emit? Or maybe it's worth the extra compute so we can switch between charts
     *          while the simulations are running?
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val chartFlo: Flow<ChartData> = merge(
        recordedAverageTraceFlo.filter { activeChart == DIPOLE_AVERAGES }.map { ChartData.RecordedFluxChart(it) },
        interactionAverageTraceFlo.filter { activeChart == INTERACTION_AVERAGES }.map { ChartData.InteractionChart(it) },
        recordSingleFlo.filter { activeChart == MH_CURVE_POINT }.map { ChartData.SingleRecording(it) },
        curveFamilyFlo.filter { activeChart == MH_CURVE }.map { ChartData.BHCurve(it) }
    )

    /**
     * Run a recording simulation as specified by [activeChart]
     */
    fun runSimulation() {
        when (activeChart) {
            DIPOLE_AVERAGES, INTERACTION_AVERAGES -> recordMultiple()
            MH_CURVE -> recordBhCurve()
            MH_CURVE_POINT -> recordSingle()
        }
    }

    /**
     * Perform a single recording upon a [MagneticMedia]
     */
    private fun recordSingle() {
        repo.record(mediaGeometry, appliedField)
    }

    /**
     * Perform [recordCount] recordings.
     */
    private fun recordMultiple() {
        repeat(recordCount) {
            repo.record(mediaGeometry, appliedField)
        }
    }

    /**
     * Record a set of B-H points for a specific [MediaGeometry] to show the linear and/or saturation regions of the
     * recording.
     * TODO: refactor to use repo
     */
    private fun recordBhCurve() {
        coroutineScope.launch {
            CurveFamily(recordCount, mediaGeometry, appliedField).also {
                it.recordMHCurves()
                curveFamilyFlo.emit(it)
            }
        }
    }

    fun setLatticeDimenX(dimen: Int) {
        mediaGeometry = mediaGeometry.copy(xCount = dimen)
    }

    fun setLatticeDimenY(dimen: Int) {
        mediaGeometry = mediaGeometry.copy(yCount = dimen)
    }

    fun setLatticeDimenZ(dimen: Int) {
        mediaGeometry = mediaGeometry.copy(zCount = dimen)
    }

    fun setDipoleRadius(radius: Float) {
        mediaGeometry = mediaGeometry.copy(dipoleRadius = radius)
    }

    fun setPackingFraction(fraction: Float) {
        mediaGeometry = mediaGeometry.copy(packingFraction = fraction)
    }

}

/**
 * Select a [Color] from the [colorList]
 */
private fun Int.toColor(): Color {
    return colorList[(this.absoluteValue % colorList.size )]
}

/** Copied from javafx.scene.paint.Color */
val BROWN: Color = Color(0.64705884f, 0.16470589f, 0.16470589f)
/** Copied from javafx.scene.paint.Color */
val VIOLET = Color(0.93333334f, 0.50980395f, 0.93333334f)
val colorList: List<Color> = listOf(BLACK, BROWN, RED, ORANGE, YELLOW.darker(), GREEN.darker(), BLUE, VIOLET, GRAY, PINK)
