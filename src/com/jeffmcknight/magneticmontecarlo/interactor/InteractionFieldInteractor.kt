package com.jeffmcknight.magneticmontecarlo.interactor

import com.jeffmcknight.magneticmontecarlo.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

/**
 * Analyzes the fields that the dipoles impose on each other during recording
 * TODO: unit tests!
 */
class InteractionFieldInteractor(repo: Repository) {
    /**
     * Emits a [InteractionResult]
     */
    private val interactionFieldsFlo: Flow<InteractionResult> = repo.recordingDoneFlo
            .map { InteractionResult(it.magneticMedia.netInteractionFields.toList(), it.appliedField, it.magneticMedia.geometry) }

    /**
     * Accumulates the total interaction field at each dipole so we can calculate the average field
     * and corresponding standard deviation at each dipole.
     * TODO: refactor to use common code from [dipoleAccumulatorFlo]
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val interactionAccumulatorFlo: Flow<InteractionAccumulator> = interactionFieldsFlo
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
     * Emits a list of [InteractionAverages]
     */
    val interactionAverageFlo: Flow<List<InteractionAverages>> =
        interactionAccumulatorFlo
            .map { acc: InteractionAccumulator ->
                acc.runningTotals.map { entry: Map.Entry<AppliedField, InteractionTotal> ->
                    InteractionAverages(
                        entry.value.interactionFieldTotals.map { recordedField -> recordedField / entry.value.count },
                        entry.value.count,
                        entry.key
                    )
                }
            }
}