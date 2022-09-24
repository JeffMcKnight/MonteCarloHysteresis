package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.MagneticMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class Repository(private val coroutineScope: CoroutineScope) {

    val recordingDoneFlo = MutableSharedFlow<RecordingResult>()

    fun record(geometry: MediaGeometry, appliedField: AppliedField) {
        coroutineScope.launch {
            val magneticMedia = MagneticMedia.create(geometry)
            magneticMedia.recordWithAcBias(appliedField)
            recordingDoneFlo.emit(RecordingResult(magneticMedia, appliedField))
        }
    }

    /**
     * Find the [AppliedField] where a [MagneticMedia] specified by the [geometry] reaches its saturation
     * [RecordedField]. By default, we find this value to within 1%, but we can specify the [tolerance]
     *
     * @param [geometry]
     * @param [tolerance] the accuracy to which we want to calculate the saturation point
     * @return the [AppliedField] at which the [MagneticMedia] reaches the saturation point
     */
    fun saturationField(geometry: MediaGeometry, tolerance: Float = 0.01F): AppliedField {
        val magneticMedia = MagneticMedia.create(geometry)
        var bounds: ClosedRange<RecordedField> = 0F..Float.MAX_VALUE
        while (!bounds.isWithinTolerance(tolerance)) {
            val recordedFlux = magneticMedia.recordWithAcBias(bounds.average())
            bounds = if (recordedFlux < magneticMedia.saturationFlux) {
                recordedFlux..bounds.endInclusive
            } else {
                bounds.start..recordedFlux
            }
        }
        return bounds.average()
    }

}

private fun ClosedRange<Float>.isWithinTolerance(tolerance: Float): Boolean {
    return (endInclusive - start) / (average()) < tolerance
}

private fun ClosedRange<Float>.average() = (endInclusive / 2) + (start / 2)
