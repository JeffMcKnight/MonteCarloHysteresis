package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MagneticMedia.Companion.empty
import com.jeffmcknight.magneticmontecarlo.model.*
import com.jeffmcknight.magneticmontecarlo.model.DipoleAccumulator.Companion.EMPTY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ViewModel(private val coroutineScope: CoroutineScope, private val repo: Repository) {

    private var mediaGeometry = MediaGeometry()
    val dipoleAveragesFlo = MutableStateFlow<List<TraceSpec>>(emptyList())
    val curveFamilyFlo = MutableStateFlow(CurveFamily(0, empty.geometry, 0F))
    val recordSingleFlo: Flow<MagneticMedia> = recordingDoneFlo.map { it.magneticMedia }

    var recordCount: Int = 1
    /**
     * The H field applied during recording
     * TODO: convert to MutableStateFlow
     */
    var appliedField: AppliedField = 0.0f

    /**
     * Emits a Pair with the last [MagneticMedia] that was recorded, and the H field that was applied during the recording
     * TODO: make a data class instead of using Pair
     */
    private val recordingDoneFlo: MutableSharedFlow<RecordingResult>
        get() = repo.recordingDoneFlo

    /**
     * Sums up all the dipole value emitted since the last [MediaGeometry] or [DipoleAccumulator.appliedField] change.
     * We use this intermediate result to determine the average dipole value over the all accumulated recordings.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dipoleAccumulatorFlo: Flow<DipoleAccumulator> = recordingDoneFlo
        .scan(EMPTY) { accum: DipoleAccumulator, next: RecordingResult ->
            if (accum.geometry == next.magneticMedia.geometry && accum.appliedField == next.appliedField) {
                accum.runningTotal.dipoleTotals
                    .zip(next.magneticMedia) { a: DipoleSphere3f, b: DipoleSphere3f -> a.apply { m += b.m } }
                    .let { dipoleList -> createDipoleAccumulator(dipoleList, accum.runningTotal.count + 1, accum.geometry, appliedField) }
            } else {
                createDipoleAccumulator(next.magneticMedia, 1, next.magneticMedia.geometry, next.appliedField)
            }
    }

    val dipoleAverageFlo: Flow<DipoleAverages> = dipoleAccumulatorFlo.map { accumulator ->
        val floatList = accumulator.runningTotal.dipoleTotals.map { dipole -> dipole.m / accumulator.runningTotal.count }
        DipoleAverages(floatList, accumulator.runningTotal.count)
    }

    fun recordSingle() {
        repo.record(mediaGeometry, appliedField)
    }

    fun recordMultiple() {
        repeat(recordCount) {
            repo.record(mediaGeometry, appliedField)
        }
    }

    /**
     * Record a set of B-H points for a specific [MediaGeometry] to show the linear and/or saturation regions of the
     * recording.
     */
    fun recordBhCurve() {
        coroutineScope.launch {
            CurveFamily(recordCount, mediaGeometry, appliedField).also {
                it.recordMHCurves()
                curveFamilyFlo.emit(it)
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

}