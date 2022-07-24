package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.MagneticMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class Repository(private val coroutineScope: CoroutineScope) {

    val recordingDoneFlo = MutableSharedFlow<Pair<MagneticMedia, Hfield>>()

    fun record(geometry: MediaGeometry, hField: Hfield) {
        coroutineScope.launch {
            val magneticMedia = MagneticMedia.create(geometry, null)
            magneticMedia.recordWithAcBias(hField)
            recordingDoneFlo.emit(Pair(magneticMedia, hField))
        }
    }

}
