package com.jeffmcknight.magneticmontecarlo.model

/**
 * @param dipoles the list of average dipole values
 * @param count the total number of recordings made to obtain the [dipoles]
 */
data class DipoleAverages(val dipoles: List<Float>, val count: Int)
