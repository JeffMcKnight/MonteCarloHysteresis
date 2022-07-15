package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.DipoleSphere3f

data class DipoleAccumulator(val dipoleList: List<DipoleSphere3f>, val count: Int, val geometry: MediaGeometry) {

}
