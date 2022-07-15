package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.create
import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.empty
import com.jeffmcknight.magneticmontecarlo.model.DipoleAccumulator
import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ViewModel(private val coroutineScope: CoroutineScope) {

    private var mediaGeometry = MediaGeometry()
    val recordSingleFlo: MutableSharedFlow<MagneticMedia> = MutableSharedFlow()

    /**
     * Emits the last [MagneticMedia] that was recorded
     * TODO: I think we'll want this to be a StateFlow once everything is routing through it
     */
    private val recordingDoneFlo: MutableSharedFlow<MagneticMedia> = MutableSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dipoleAccumulatorFlo: Flow<DipoleAccumulator> = recordingDoneFlo
        .scan(DipoleAccumulator(empty, 0, empty.geometry)) { prev: DipoleAccumulator, next: MagneticMedia ->
        if (prev.geometry == next.geometry  ) {
            prev.dipoleList.zip(next) { a: DipoleSphere3f, b: DipoleSphere3f ->
                a.apply { m += b.m }}.let { dipoleList ->
                DipoleAccumulator(dipoleList, prev.count + 1, prev.geometry) }
        } else {
            next.toDipoleAccumulator()
        }
    }

    val dipoleAverageFlo: Flow<List<Float>> = dipoleAccumulatorFlo.map{ accum -> accum.dipoleList.map { dipole -> dipole.m / accum.count } }

    fun recordSingle(maxAppliedField: Float, geometry: MediaGeometry) {
        coroutineScope.launch {
            val magneticMedia = create(geometry, null)
            magneticMedia.recordWithAcBias(maxAppliedField)
            recordSingleFlo.emit(magneticMedia)
        }
    }

    fun setLatticeDimenX(dimen: Int) {
        mediaGeometry = mediaGeometry.copy(xCount = dimen)
    }

    fun setLatticeDimenY(dimen: Int) {
        mediaGeometry = mediaGeometry.copy(yCount = dimen)
    }

    fun setLatticeDimenZ(dimen: Int) {
        mediaGeometry = mediaGeometry.copy(zCount = dimen)
    }

    fun recordPoint(maxAppliedField: Float, geometry: MediaGeometry) {
        coroutineScope.launch {
            val magneticMedia = create(geometry, null)
            magneticMedia.recordWithAcBias(maxAppliedField)
            recordingDoneFlo.emit(magneticMedia)
        }
    }
}