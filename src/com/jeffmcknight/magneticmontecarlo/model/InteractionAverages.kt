package com.jeffmcknight.magneticmontecarlo.model

import java.awt.Color

data class InteractionAverages(
    val averageInteractionFields: List<InteractionField>,
    val count: Int,
    val appliedField: AppliedField,
    val color: Color
)
