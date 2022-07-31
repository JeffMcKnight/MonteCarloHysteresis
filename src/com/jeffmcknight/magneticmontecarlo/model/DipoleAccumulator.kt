package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.DipoleSphere3f
import com.jeffmcknight.magneticmontecarlo.MagneticMedia

/**
 * Use to hold intermediate results for multiple recordings with similar [AppliedField]s.  We keep a map of
 * [RunningTotal]s keyed on the [AppliedField] so we can show the data from recordings with different [AppliedField]s
 * on the same chart.
 *
 * See [RunningTotal]
 *
 * @param runningTotal a running total of dipole values from of a set of [RecordingResult]s
 * @param geometry the geometry of the magnetic media that has been recorded
 * @param appliedField the DC H field applied to the magnetic media that has been recorded
 */
data class DipoleAccumulator(
    val runningTotal: RunningTotal,
    val geometry: MediaGeometry,
    val appliedField: AppliedField
) {
    companion object {
        val EMPTY = createDipoleAccumulator(MagneticMedia.empty, 0, MagneticMedia.empty.geometry, 0.0f)
    }
}

fun createDipoleAccumulator(dipoleList: List<DipoleSphere3f>, count: Int, geometry: MediaGeometry, appliedField: Float): DipoleAccumulator {
    return DipoleAccumulator(RunningTotal(dipoleList, count), geometry, appliedField)
}

/**
 * A running total of dipole values from of a set of [RecordingResult]s.  Use to hold intermediate results. When it comes
 * time to show averaged dipole values to the user, divide the values in [dipoleTotals] by the [count]

 * @param dipoleTotals a list of the running totals of the recorded field for each dipole
 * @param count the number of recordings totalled in [dipoleTotals]
 */
data class RunningTotal(val dipoleTotals: List<DipoleSphere3f>, val count: Int)
