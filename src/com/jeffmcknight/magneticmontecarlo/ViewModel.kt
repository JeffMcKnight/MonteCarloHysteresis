package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.empty
import com.jeffmcknight.magneticmontecarlo.ViewModel.Companion.SHOULDER_THRESHOLD_PERCENTAGE
import com.jeffmcknight.magneticmontecarlo.interactor.InteractionFieldInteractor
import com.jeffmcknight.magneticmontecarlo.interactor.RecordedFieldInteractor
import com.jeffmcknight.magneticmontecarlo.ktExtension.indexOfFirstOrNull
import com.jeffmcknight.magneticmontecarlo.model.AppliedField
import com.jeffmcknight.magneticmontecarlo.model.DipoleAccumulator
import com.jeffmcknight.magneticmontecarlo.model.DipoleAverages
import com.jeffmcknight.magneticmontecarlo.model.Flux
import com.jeffmcknight.magneticmontecarlo.model.InteractionAverages
import com.jeffmcknight.magneticmontecarlo.model.InteractionField
import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import com.jeffmcknight.magneticmontecarlo.model.Repository
import com.jeffmcknight.magneticmontecarlo.model.RunningTotal
import info.monitorenter.gui.chart.ITracePoint2D
import info.monitorenter.gui.chart.TracePoint2D
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.BLUE
import java.awt.Color.GRAY
import java.awt.Color.GREEN
import java.awt.Color.ORANGE
import java.awt.Color.PINK
import java.awt.Color.RED
import java.awt.Color.YELLOW
import kotlin.math.abs

/**
 * TODO: unit tests!
 */
class ViewModel(
    private val coroutineScope: CoroutineScope,
    private val repo: Repository,
    recordedFieldInteractor: RecordedFieldInteractor,
    interactionFieldInteractor: InteractionFieldInteractor
) {

    /**
     * Converts the aggregated [Flux] for each dipole over multiple recordings to the average [Flux]
     * for each dipole. Each entry in the map represents a set of recordings with a specific
     * [AppliedField].
     */
    private val averageFluxesFlo: Flow<Map<AppliedField, List<Flux>>> =
        recordedFieldInteractor.dipoleAccumulatorFlo.map { it.toAverageFluxesMap() }

    /**
     * Convert the lists of average dipole [Flux]es to a list that contains a single [TraceSpec]
     * with the shoulder index for each [AppliedField]. That is to say, for each [TracePoint2D] in
     * the [TraceSpec], [TracePoint2D.x] represents the [AppliedField], and [TracePoint2D.y]
     * represents the index of the dipole at the shoulder point.
     */
    val shoulderTraceFlo: Flow<List<TraceSpec>> = averageFluxesFlo
        .map { it.toShoulderPoints() }
        .map { pointList -> listOf(TraceSpec("Shoulder Index Chart", pointList)) }

    /**
     * The number of points to generate for the [TraceSpec] emitted by the [shoulderTraceFlo]
     */
    var shoulderPointCount = 0
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
     * Run [recordCount] sets of recording simulations, each containing [shoulderPointCount]
     * different [AppliedField]s.
     */
    fun recordShoulderCurve() {
        repeat(recordCount) {
            repeat(shoulderPointCount) { appliedFieldCount ->
                repo.record(
                    mediaGeometry,
                    toAppliedField(appliedField, appliedFieldCount, shoulderPointCount)
                )
            }
        }
    }

    private fun toAppliedField(
        maxAppliedField: AppliedField,
        appliedFieldCount: Int,
        times: Int
    ): AppliedField {
        return maxAppliedField * appliedFieldCount / times
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

    companion object {
        const val SHOULDER_THRESHOLD_PERCENTAGE = 0.01F
    }
}

/**
 * TODO: convert the Flux to the index of the shoulder dipole
 * 1. convert Map<AppliedField, List<Flux>> to Map<AppliedField, CoercivityRank>
 * 2. convert Map<AppliedField, CoercivityRank> to List<ITracePoint2D>
 * 3. wrap List<ITracePoint2D> in a [TraceSpec]
 * 4. add that single [TraceSpec] to a list
 */
private fun Map<AppliedField, List<Flux>>.toShoulderPoints(): List<ITracePoint2D> {
    return this.map { fluxEntry: Map.Entry<AppliedField, List<Flux>> ->
        fluxEntry.toShoulderPoint()
    }
}

/**
 * FIXME: the target type should be Map<AppliedField, List<Flux>>
 */
private fun Map.Entry<AppliedField, List<Flux>>.toShoulderPoint(): ITracePoint2D {
    return TracePoint2D(key.toDouble(), value.toShoulderIndex().toDouble())
}

/**
 * Returns the index of the first item whose value is below the shoulder edge.
 */
private fun List<Flux>.toShoulderIndex(): Int {
    return indexOfFirstOrNull { exceedsThreshold(it) } ?: (size - 1)
}

/**
 * Checks whether [flux] is at the edge of the shoulder.  We check whether the [flux] is
 * offset from the max value by more than [SHOULDER_THRESHOLD_PERCENTAGE] of the total range (the
 * difference between the max and the min).
 * TODO: maybe there's an official statistical way to find the shoulder?
 * TODO: do we want give the user control over [SHOULDER_THRESHOLD_PERCENTAGE]?
 */
private fun List<Flux>.exceedsThreshold(flux: Flux): Boolean {
    val max = maxByOrNull { abs(it) } ?: return false
    val min = minByOrNull { abs(it) } ?: return false
    return ((max - flux) / (max - min)) > SHOULDER_THRESHOLD_PERCENTAGE
}

/**
 * Converts the accumulated dipole flux totals from multiple recordings to a map of average flux
 * values.
 * TODO: use this for [dipoleAverageFlo] as well as [averageFluxesFlow]
 */
private fun DipoleAccumulator.toAverageFluxesMap(): Map<AppliedField, List<Flux>> {
    return this.runningTotals.mapValues { entry: Map.Entry<AppliedField, RunningTotal> ->
        entry.value.dipoleTotalList.map { totalFlux -> totalFlux / entry.value.count }
    }
}

/** Copied from javafx.scene.paint.Color */
val BROWN: Color = Color(0.64705884f, 0.16470589f, 0.16470589f)

/** Copied from javafx.scene.paint.Color */
val VIOLET = Color(0.93333334f, 0.50980395f, 0.93333334f)
val traceColors: List<Color> =
    listOf(BLACK, BROWN, RED, ORANGE, YELLOW.darker(), GREEN.darker(), BLUE, VIOLET, GRAY, PINK)

fun DipoleAverages.toTraceSpec(): TraceSpec {
    return TraceSpec(
        this.toName(),
        dipoles.mapIndexed { i: Int, h: Flux -> TracePoint2D(i.toDouble(), h.toDouble()) }
    )
}

/**
 * Creates names for the traces that show the values of individual dipoles, averaged over multiple
 * recordings.
 */
private fun DipoleAverages.toName(): String {
    return "Averaged Dipoles\t-- Applied Field: ${appliedField}\t-- Recording Passes: $count"
}
