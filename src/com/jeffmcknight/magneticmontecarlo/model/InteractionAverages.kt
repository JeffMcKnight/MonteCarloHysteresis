package com.jeffmcknight.magneticmontecarlo.model

data class InteractionAverages(
    val averageInteractionFields: List<InteractionField>,
    val count: Int,
    val appliedField: AppliedField
)
