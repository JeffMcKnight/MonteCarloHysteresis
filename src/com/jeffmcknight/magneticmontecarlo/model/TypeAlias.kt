package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.DipoleSphere3f

typealias AppliedField = Float
/**
 * The field at a [DipoleSphere3f] in a [MagneticMedia] that is the result of another [DipoleSphere3f]
 * which has already been fixated during the recording process.  By implication, the other [DipoleSphere3f]
 * has a higher coercivity than the reference [DipoleSphere3f]
 */
typealias InteractionField = Float
typealias RecordedField = Float

