package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.MagneticMedia

/**
 * @param magneticMedia the [MagneticMedia] used to perform the recording simulation
 * @param appliedField the DC H field applied during the recording
 */
data class RecordingResult(val magneticMedia: MagneticMedia, val appliedField: AppliedField)
