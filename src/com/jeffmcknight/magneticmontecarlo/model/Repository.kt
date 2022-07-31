package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.MagneticMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class Repository(private val coroutineScope: CoroutineScope) {

    val recordingDoneFlo = MutableSharedFlow<RecordingResult>()

    fun record(geometry: MediaGeometry, appliedField: AppliedField) {
        coroutineScope.launch {
            val magneticMedia = MagneticMedia.create(geometry, null)
            magneticMedia.recordWithAcBias(appliedField)
            recordingDoneFlo.emit(RecordingResult(magneticMedia, appliedField))
        }
    }

}
