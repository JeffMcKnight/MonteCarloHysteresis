package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.empty
import com.jeffmcknight.magneticmontecarlo.interactor.InteractionFieldInteractor
import com.jeffmcknight.magneticmontecarlo.interactor.RecordedFieldInteractor
import com.jeffmcknight.magneticmontecarlo.model.*
import info.monitorenter.gui.chart.TracePoint2D
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.Color.*
import kotlin.math.absoluteValue

/**
 * TODO: unit tests!
 */
class ViewModel(
    private val coroutineScope: CoroutineScope,
    private val repo: Repository,
    recordedFieldInteractor: RecordedFieldInteractor,
    interactionFieldInteractor: InteractionFieldInteractor
) {

    private var mediaGeometry = MediaGeometry()
    val curveFamilyFlo = MutableStateFlow(CurveFamily(0, empty.geometry, 0F))
    val recordSingleFlo: Flow<MagneticMedia> = repo.recordingDoneFlo.map { it.magneticMedia }

    var recordCount: Int = 1

    /**
     * The H field applied during recording
     * TODO: convert to MutableStateFlow
     */
    var appliedField: AppliedField = 0.0f

    /**
     * Add UI-specific data
     */
    val interactionAverageTraceFlo: Flow<List<TraceSpec>> =
        interactionFieldInteractor.interactionAverageFlo.map { traceDataList: List<InteractionAverages> ->
            traceDataList.map { averages: InteractionAverages ->
                val traceName =
                    "Averaged Interaction at Dipoles\t-- Applied Field: ${averages.appliedField}\t-- Recording Passes: ${averages.count}"
                val pointList =
                    averages.averageInteractionFields.mapIndexed { index: Int, h: InteractionField ->
                        TracePoint2D(index.toDouble(), h.toDouble())
                    }
                TraceSpec(traceName, pointList)
            }
        }

    /**
     * Emits a list of [DipoleAverages]; that is to say, a list of the average value of each dipole
     * averaged over a series of recording passes.
     */
    val dipoleAverageFlo: Flow<List<TraceSpec>> =
        recordedFieldInteractor.dipoleAccumulatorFlo.map { accumulator: DipoleAccumulator ->
            accumulator.runningTotals.map { entry: Map.Entry<AppliedField, RunningTotal> ->
                DipoleAverages(
                    entry.value.dipoleTotalList.map { recordedField -> recordedField / entry.value.count },
                    entry.value.count,
                    entry.key
                ).toTraceSpec()
            }
        }

    /**
     * Perform a single recording upon a [MagneticMedia]
     */
    fun recordSingle() {
        repo.record(mediaGeometry, appliedField)
    }

    /**
     * Perform [recordCount] recordings.
     */
    fun recordMultiple() {
        repeat(recordCount) {
            repo.record(mediaGeometry, appliedField)
        }
    }

    /**
     * Record a set of B-H points for a specific [MediaGeometry] to show the linear and/or saturation regions of the
     * recording.
     * TODO: refactor to use repo
     */
    fun recordBhCurve() {
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

/** Copied from javafx.scene.paint.Color */
val BROWN: Color = Color(0.64705884f, 0.16470589f, 0.16470589f)

/** Copied from javafx.scene.paint.Color */
val VIOLET = Color(0.93333334f, 0.50980395f, 0.93333334f)
val traceColors: List<Color> = listOf(BLACK, BROWN, RED, ORANGE, YELLOW.darker(), GREEN.darker(), BLUE, VIOLET, GRAY, PINK)
fun DipoleAverages.toTraceSpec(): TraceSpec {
    return TraceSpec(
        this.toName(),
        dipoles.mapIndexed { i: Int, h: RecordedField -> TracePoint2D(i.toDouble(), h.toDouble()) }
    )
}

/**
 * Creates names for the traces that show the values of individual dipoles, averaged over multiple
 * recordings.
 */
private fun DipoleAverages.toName(): String {
    return "Averaged Dipoles\t-- Applied Field: ${appliedField}\t-- Recording Passes: $count"
}
