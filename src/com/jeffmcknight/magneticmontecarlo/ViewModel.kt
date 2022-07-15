package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.create
import com.jeffmcknight.magneticmontecarlo.MagneticMedia.MagneticMediaListener
import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow

class ViewModel(private val coroutineScope: CoroutineScope) {

    /**
     * Emits the last [MagneticMedia] that was recorded
     * TODO: I think we'll want this to be a StateFlow once everything is routing through it
     */
    val recordingDoneFlo = MutableSharedFlow<MagneticMedia>()

    val updateListener = object: MagneticMediaListener {
        /**
         * Emit to recordingDoneFlo when the recording completes
         * FIXME: which [CoroutineScope] should we use here?  probably not runBlocking
         */
        override fun onRecordingDone(magneticMedia: MagneticMedia) {
        }

        override fun onDipoleFixated(dipoleSphere3f: DipoleSphere3f) {
            TODO("Not yet implemented")
        }

    }

    fun record(h: Float, geometry: MediaGeometry) {
        val list: List<MagneticMedia> = List(3) { MagneticMedia.create(geometry, null) }
        list.forEach { media: MagneticMedia -> media.recordWithAcBias(h)}

    }

    fun recordSingle(maxAppliedField: Float, geometry: MediaGeometry) {
        coroutineScope.launch {
            val magneticMedia = create(geometry, null)
            magneticMedia.recordWithAcBias(maxAppliedField)
            recordingDoneFlo.emit(magneticMedia)
        }


    }
}