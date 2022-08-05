package com.jeffmcknight.magneticmontecarlo.model

import java.awt.Color

/**
 * @param dipoles the list of average dipole values
 * @param count the total number of recordings made to obtain the [dipoles]
 */
data class DipoleAverages(
    val dipoles: List<RecordedField>,
    val count: Int,
    val appliedField: AppliedField,
    val color: Color
)
