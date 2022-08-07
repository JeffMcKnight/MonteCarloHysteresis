package com.jeffmcknight.magneticmontecarlo.model

/**
 * @param dipoles the list of average dipole values
 * @param count the total number of recordings made to obtain the [dipoles]
 * @param appliedField the external field applied by the recording head to make the recording
 */
data class DipoleAverages(
    val dipoles: List<RecordedField>,
    val count: Int,
    val appliedField: AppliedField
)
