package com.jeffmcknight.magneticmontecarlo.interactor

import com.jeffmcknight.magneticmontecarlo.interactor.SaturationEvent.RecordingEvent
import com.jeffmcknight.magneticmontecarlo.interactor.SaturationEvent.StartEvent
import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import com.jeffmcknight.magneticmontecarlo.model.RecordingResult
import com.jeffmcknight.magneticmontecarlo.model.Repository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class SaturationInteractor(repo: Repository) {

    val recordingEventFlo = repo.recordingDoneFlo.toRecordingEvent()
    val controlEventFlo = MutableSharedFlow<StartEvent>()

    suspend fun findEdge(mediaGeometry: MediaGeometry) {
        controlEventFlo.emit(StartEvent(mediaGeometry))
    }

    /**
     * Emits the [SaturationState] of the state machine
     */
    val edgeFlow: Flow<SaturationState> = merge(controlEventFlo, recordingEventFlo)
        .scan(SaturationState.UninitializedState) { state: SaturationState, event: SaturationEvent ->
            return@scan when (event) {
                is RecordingEvent -> nextState(event, state)
                is StartEvent -> SaturationState.UnboundedState(event.geometry)
            }
        }

    private fun nextState(event: RecordingEvent, state: SaturationState): SaturationState {
        val isSaturated = with(event.recordingResult.magneticMedia) {
            calculateNetM() == saturationFlux }
//        if (isSaturated) {
//            if (event.recordingResult.appliedField < state.) {}
//        }
        return SaturationState.UninitializedState
    }
}

sealed class SaturationState {
    data class BoundedState(
        val lowerBound: Float, val upperBound: Float, val geometry: MediaGeometry
    ) : SaturationState()

    data class LowerBoundedState(val bound: Float, val geometry: MediaGeometry) : SaturationState()
    data class UpperBoundedState(val bound: Float, val geometry: MediaGeometry) : SaturationState()
    data class UnboundedState(val geometry: MediaGeometry) : SaturationState()
    object UninitializedState : SaturationState()
}

sealed class SaturationEvent {
    data class StartEvent(val geometry: MediaGeometry) : SaturationEvent()
    data class RecordingEvent(val recordingResult: RecordingResult) : SaturationEvent()
}

private fun Flow<RecordingResult>.toRecordingEvent(): Flow<RecordingEvent> {
    return map { RecordingEvent(it) }
}
