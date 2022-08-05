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


class ViewModel(private val coroutineScope: CoroutineScope, private val repo: Repository) {

    private var mediaGeometry = MediaGeometry()
    val dipoleAveragesFlo = MutableStateFlow<List<TraceSpec>>(emptyList())
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
     * Sums up all the dipole value emitted since the last [MediaGeometry] change.  We aggregate the lists of
     * [RecordedField] and organize by the [AppliedField] (using it as the key to a mutable map).
     * We use this intermediate result to determine the average dipole value over the all accumulated recordings.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dipoleAccumulatorFlo: Flow<DipoleAccumulator> = recordingDoneFlo
        .scan(EMPTY) { accum: DipoleAccumulator, next: RecordingResult ->
            // Get the list of [RecordedField]s for each dipole in the most recent simulation
            val nextRecordedFieldList: List<RecordedField> = next.magneticMedia.map { it.m }
            if (accum.geometry == next.magneticMedia.geometry) {
                run {
                    val runningTotal: RunningTotal? = accum.runningTotals[next.appliedField]
                    val updatedTotals: List<RecordedField> = runningTotal?.let {
                        // Add the [RecordedField] from the most recent simulation to the running total
                        it.dipoleTotalList.zip(nextRecordedFieldList) { a: RecordedField, b: RecordedField -> a + b }
                    } ?: nextRecordedFieldList
                    val updatedCount = (runningTotal?.count ?: 0) + 1
                    (accum.runningTotals + mapOf(next.appliedField to RunningTotal(updatedTotals, updatedCount)))
                        .toMutableMap()
                }
                    .let { totalsMap: MutableMap<AppliedField, RunningTotal> ->
                        DipoleAccumulator(totalsMap, accum.geometry)
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

    fun recordSingle() {
        repo.record(mediaGeometry, appliedField)
    }

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
    return colorList[((this / 5) % 10 ).toInt()]
}

/** Copied from javafx.scene.paint.Color */
val BROWN: Color = Color(0.64705884f, 0.16470589f, 0.16470589f)
/** Copied from javafx.scene.paint.Color */
val VIOLET = Color(0.93333334f, 0.50980395f, 0.93333334f)
val colorList: List<Color> = listOf(BLACK, BROWN, RED, ORANGE, YELLOW.darker(), GREEN.darker(), BLUE, VIOLET, GRAY, PINK)
