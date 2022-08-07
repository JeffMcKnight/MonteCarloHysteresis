package com.jeffmcknight.magneticmontecarlo.interactor

import com.jeffmcknight.magneticmontecarlo.model.*
import com.jeffmcknight.magneticmontecarlo.DipoleSphere3f
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

/**
 * Analyzes the final flux of each [DipoleSphere3f] after recording is complete.  We collect the dipoles for each
 * recording pass, which are implicitly sorted by dipole coercivity, descending.
 * TODO: unit tests!
 */
class RecordedFieldInteractor(repo: Repository) {

    /**
     * Sums up all the dipole value emitted since the last [MediaGeometry] change.  We aggregate the lists of
     * [RecordedField] and organize by the [AppliedField] (using it as the key to a mutable map).
     * We use this intermediate result to determine the average dipole value over the all accumulated recordings.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dipoleAccumulatorFlo: Flow<DipoleAccumulator> = repo.recordingDoneFlo
        .scan(DipoleAccumulator.EMPTY) { acc: DipoleAccumulator, next: RecordingResult ->
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
    val dipoleAverageFlo: Flow<List<DipoleAverages>> =
        dipoleAccumulatorFlo.map { accumulator: DipoleAccumulator ->
            accumulator.runningTotals.map { entry: Map.Entry<AppliedField, RunningTotal> ->
                DipoleAverages(
                    entry.value.dipoleTotalList.map { recordedField -> recordedField / entry.value.count },
                    entry.value.count,
                    entry.key
                )
            }
        }

}