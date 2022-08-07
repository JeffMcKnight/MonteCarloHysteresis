package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.MagneticMedia

data class InteractionAccumulator(
    val runningTotals: MutableMap<AppliedField, InteractionTotal>,
    val geometry: MediaGeometry
) {
    companion object {
        val EMPTY = InteractionAccumulator(mutableMapOf(), MagneticMedia.empty.geometry)
    }

}

/**
 * A running total of interaction field values from of a set of [RecordingResult]s.  Use to hold intermediate results. When it comes
 * time to show averaged dipole values to the user, divide the values in [interactionFieldTotals] by the [count]

 * @param interactionFieldTotals a list of the running totals of the interaction field for each dipole
 * @param count the number of recordings totalled in [interactionFieldTotals]
 */
data class InteractionTotal(val interactionFieldTotals: List<InteractionField>, val count: Int)
