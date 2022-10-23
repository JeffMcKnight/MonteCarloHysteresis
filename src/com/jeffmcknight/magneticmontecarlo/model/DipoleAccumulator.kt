package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.MagneticMedia

/**
 * Use to hold intermediate results for multiple recordings with similar [AppliedField]s.  We keep a map of
 * [RunningTotal]s keyed on the [AppliedField] so we can show the data from recordings with different [AppliedField]s
 * on the same chart.
 *
 * See [RunningTotal]
 *
 * @param runningTotals a running total of dipole values from of a set of [RecordingResult]s
 * @param geometry the geometry of the magnetic media that has been recorded
 * @param appliedField the DC H field applied to the magnetic media that has been recorded
 */
data class DipoleAccumulator(
    val runningTotals: MutableMap<AppliedField, RunningTotal>,
    val geometry: MediaGeometry
) {
    companion object {
        val EMPTY = DipoleAccumulator(mutableMapOf(), MagneticMedia.empty.geometry)
    }
}

/**
 * A running total of dipole values from of a set of [RecordingResult]s.  Use to hold intermediate results. When it comes
 * time to show averaged dipole values to the user, divide the values in [dipoleTotalList] by the [count]

 * @param dipoleTotalList a list of the running totals of the recorded field for each dipole
 * @param count the number of recordings totalled in [dipoleTotalList]
 */
data class RunningTotal(val dipoleTotalList: List<Flux>, val count: Int)
