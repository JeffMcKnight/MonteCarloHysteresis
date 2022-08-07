package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.CurveFamily
import com.jeffmcknight.magneticmontecarlo.MagneticMedia
import com.jeffmcknight.magneticmontecarlo.TraceSpec

/**
 * The various types of data that we want to display on charts
 */
sealed class ChartData {
    data class BHCurve (val curve: CurveFamily): ChartData()
    data class InteractionChart (val interactionTraces: List<TraceSpec>): ChartData()
    data class RecordedFluxChart (val recordedFieldTraces: List<TraceSpec>): ChartData()
    data class SingleRecording (val magneticMedia: MagneticMedia): ChartData()
}
