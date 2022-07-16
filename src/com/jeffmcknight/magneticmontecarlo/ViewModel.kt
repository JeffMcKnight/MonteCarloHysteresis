package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.create
import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.empty
import com.jeffmcknight.magneticmontecarlo.model.DipoleAccumulator
import com.jeffmcknight.magneticmontecarlo.model.DipoleAverages
import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ViewModel(private val coroutineScope: CoroutineScope) {

    var recordCount: Int = 1

    /**
     * The H field applied during recording
     * TODO: convert to MutableStateFlow
     */
    var appliedField: Float = 0.0f
    private var mediaGeometry = MediaGeometry()
    val recordSingleFlo: MutableSharedFlow<MagneticMedia> = MutableSharedFlow()

    /**
     * Emits a Pair with the last [MagneticMedia] that was recorded, and the H field that was applied during the recording
     * TODO: make a data class instead of using Pair
     */
    private val recordingDoneFlo: MutableSharedFlow<Pair<MagneticMedia, Float>> = MutableSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dipoleAccumulatorFlo: Flow<DipoleAccumulator> = recordingDoneFlo
        .scan(DipoleAccumulator(empty, 0, empty.geometry, 0.0f)) { prev: DipoleAccumulator, next: Pair<MagneticMedia, Float> ->
        if (prev.geometry == next.first.geometry && prev.fieldB == next.second ) {
            prev.dipoleList.zip(next.first) { a: DipoleSphere3f, b: DipoleSphere3f ->
                a.apply { m += b.m }}.let { dipoleList ->
                DipoleAccumulator(dipoleList, prev.count + 1, prev.geometry, appliedField) }
        } else {
            DipoleAccumulator(next.first, 1, next.first.geometry, next.second)
        }
    }

    val dipoleAverageFlo: Flow<DipoleAverages> = dipoleAccumulatorFlo.map{ accum ->
        val floatList = accum.dipoleList.map { dipole -> dipole.m / accum.count }
        DipoleAverages(floatList, accum.count)
    }

    fun recordSingle() {
        coroutineScope.launch {
            val magneticMedia = create(mediaGeometry, null)
            magneticMedia.recordWithAcBias(appliedField)
            recordSingleFlo.emit(magneticMedia)
        }
    }

    fun recordMultiple() {
        for (i in 1..recordCount) {
            coroutineScope.launch {
                val magneticMedia = create(mediaGeometry, null)
                magneticMedia.recordWithAcBias(appliedField)
                recordingDoneFlo.emit(Pair(magneticMedia, appliedField))
            }
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

    fun setDipoleRadius(radius: Float) {
        mediaGeometry = mediaGeometry.copy(dipoleRadius = radius)
    }

    fun setPackingFraction(fraction: Float) {
        mediaGeometry = mediaGeometry.copy(packingFraction = fraction)
    }

    /**
     * Record a set of B-H points for a specific [MediaGeometry] to show the linear and/or saturation regions of the
     * recording.
     * TODO: Flow-ify
     */
    fun recordBhCurve(curveFamilyListener: CurveFamily.CurveFamilyListener): CurveFamily {
        return CurveFamily(recordCount, mediaGeometry, appliedField, curveFamilyListener).apply { recordMHCurves() }
    }
}