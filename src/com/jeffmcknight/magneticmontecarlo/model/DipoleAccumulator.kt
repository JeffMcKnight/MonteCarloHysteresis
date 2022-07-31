package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.DipoleSphere3f

/**
 * Use to hold intermediate results. When it comes time to show averaged dipole values to the user, divide the values
 * in [dipoleList] by the [count]
 * @param dipoleList a list of the accumulated dipole flux values
 * @param count the number of dipole sets that have been accumulated
 * @param geometry the geometry of the magnetic media that has been recorded
 * @param appliedField the DC H field applied to the magnetic media that has been recorded
 */
data class DipoleAccumulator(
    val dipoleList: List<DipoleSphere3f>,
    val count: Int,
    val geometry: MediaGeometry,
    val appliedField: AppliedField
) {

}
