package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.empty
import com.jeffmcknight.magneticmontecarlo.model.*
import com.jeffmcknight.magneticmontecarlo.model.DipoleAccumulator.Companion.EMPTY
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
class ViewModel(private val coroutineScope: CoroutineScope, private val repo: Repository) {

    private var mediaGeometry = MediaGeometry()
    val curveFamilyFlo = MutableStateFlow(CurveFamily(0, empty.geometry, 0F))
    val recordSingleFlo: Flow<MagneticMedia> = recordingDoneFlo.map { it.magneticMedia }

    var recordCount: Int = 1
    /**
     * The H field applied during recording
     * TODO: convert to MutableStateFlow
     */
    var appliedField: AppliedField = 0.0f

    /**
     * Emits a Pair with the last [MagneticMedia] that was recorded, and the H field that was applied during the recording
     * TODO: make a data class instead of using Pair
     */
    private val recordingDoneFlo: MutableSharedFlow<RecordingResult>
        get() = repo.recordingDoneFlo

    /**
     * Emits a Pair with the last [MagneticMedia] that was recorded, and the H field that was applied during the recording
     * TODO: make a data class instead of using Pair
     */
    private val interactionFieldsFlo: Flow<InteractionResult>
        get() = repo.recordingDoneFlo
            .map { InteractionResult(it.magneticMedia.netInteractionFields.toList(), it.appliedField, it.magneticMedia.geometry) }

    /**
     * Accumulates the total interaction field at each dipole so we can calculate the average field and corresponding
     * standard deviation at each dipole.
     * TODO: refactor to use common code from [dipoleAccumulatorFlo]
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val interactionAccumulatorFlo: Flow<InteractionAccumulator> = interactionFieldsFlo
        .scan(InteractionAccumulator.EMPTY) { acc: InteractionAccumulator, next: InteractionResult ->
        // Get the list of [RecordedField]s for each dipole in the most recent simulation
        val nextInteractionFieldList: List<InteractionField> = next.interactionFields
        if (acc.geometry == next.geometry) {
            run {
                val runningTotal: InteractionTotal? = acc.runningTotals[next.appliedField]
                val updatedTotals: List<InteractionField> = runningTotal?.let {
                    // Add the [RecordedField] from the most recent simulation to the running total
                    it.interactionFieldTotals.zip(nextInteractionFieldList) { a: InteractionField, b: InteractionField -> a + b }
                } ?: nextInteractionFieldList
                val updatedCount = (runningTotal?.count ?: 0) + 1
                val interactionTotal = InteractionTotal(updatedTotals, updatedCount)
                return@run (acc.runningTotals + mapOf(next.appliedField to interactionTotal)).toMutableMap()
            }
                .let { totalsMap: MutableMap<AppliedField, InteractionTotal> -> InteractionAccumulator(totalsMap, acc.geometry) }
        } else {
            val interactionTotal = InteractionTotal(nextInteractionFieldList, 1)
            return@scan InteractionAccumulator(
                mutableMapOf(next.appliedField to interactionTotal),
                next.geometry
            )
        }
    }

    /**
     * Emits a list of [DipoleAverages]
     */
    private val interactionAverageFlo: Flow<List<InteractionAverages>> = interactionAccumulatorFlo.map { acc: InteractionAccumulator ->
        acc.runningTotals.map { entry: Map.Entry<AppliedField, InteractionTotal> ->
            InteractionAverages(
                entry.value.interactionFieldTotals.map { recordedField -> recordedField / entry.value.count },
                entry.value.count,
                entry.key,
                entry.key.toColor()
            )
        }
    }

    val interactionAverageTraceFlo: Flow<List<TraceSpec>> = interactionAverageFlo.map {traceDataList: List<InteractionAverages> ->
        traceDataList.map { averages: InteractionAverages ->
            val titleXAxis = "n [Dipole rank by coercivity]"
            val traceName =
                "Averaged Interaction at Dipoles\t-- Applied Field: ${averages.appliedField}\t-- Recording Passes: ${averages.count}"
            val traceColor = averages.color
            val pointList = averages.averageInteractionFields.mapIndexed { index: Int, h: InteractionField ->
                Point2d(index.toDouble(), h.toDouble())
            }
            TraceSpec(traceName, titleXAxis, traceColor, pointList, "Recorded Flux [nWb/m]")
        }

    }



    /**
     * Sums up all the dipole value emitted since the last [MediaGeometry] change.  We aggregate the lists of
     * [RecordedField] and organize by the [AppliedField] (using it as the key to a mutable map).
     * We use this intermediate result to determine the average dipole value over the all accumulated recordings.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dipoleAccumulatorFlo: Flow<DipoleAccumulator> = recordingDoneFlo
        .scan(EMPTY) { acc: DipoleAccumulator, next: RecordingResult ->
            // Get the list of [RecordedField]s for each dipole in the most recent simulation
            val nextRecordedFieldList: List<RecordedField> = next.magneticMedia.map { it.m }
            if (acc.geometry == next.magneticMedia.geometry) {
                run {
                    val runningTotal: RunningTotal? = acc.runningTotals[next.appliedField]
                    val updatedTotals: List<RecordedField> = runningTotal?.let {
                        // Add the [RecordedField] from the most recent simulation to the running total
                        it.dipoleTotalList.zip(nextRecordedFieldList) { a: RecordedField, b: RecordedField -> a + b }
                    } ?: nextRecordedFieldList
                    val updatedCount = (runningTotal?.count ?: 0) + 1
                    (acc.runningTotals + mapOf(next.appliedField to RunningTotal(updatedTotals, updatedCount)))
                        .toMutableMap()
                }
                    .let { totalsMap: MutableMap<AppliedField, RunningTotal> ->
                        DipoleAccumulator(totalsMap, acc.geometry)
                    }
            } else {
                DipoleAccumulator(
                    mutableMapOf(next.appliedField to RunningTotal(nextRecordedFieldList, 1)),
                    next.magneticMedia.geometry
                )
            }
        }

    /**
     * Emits a list of [DipoleAverages]
     */
    val dipoleAverageFlo: Flow<List<DipoleAverages>> = dipoleAccumulatorFlo.map { accumulator: DipoleAccumulator ->
        accumulator.runningTotals.map { entry: Map.Entry<AppliedField, RunningTotal> ->
            DipoleAverages(
                entry.value.dipoleTotalList.map { recordedField -> recordedField / entry.value.count },
                entry.value.count,
                entry.key,
                entry.key.toColor()
            )
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

/**
 * TODO: take the divisor from the max [AppliedField]
 */
private fun AppliedField.toColor(): Color {
    return colorList[((this.absoluteValue / 5) % 10 ).toInt()]
}

/** Copied from javafx.scene.paint.Color */
val BROWN: Color = Color(0.64705884f, 0.16470589f, 0.16470589f)
/** Copied from javafx.scene.paint.Color */
val VIOLET = Color(0.93333334f, 0.50980395f, 0.93333334f)
val colorList: List<Color> = listOf(BLACK, BROWN, RED, ORANGE, YELLOW.darker(), GREEN.darker(), BLUE, VIOLET, GRAY, PINK)
