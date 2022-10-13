package com.jeffmcknight.magneticmontecarlo.model

import com.jeffmcknight.magneticmontecarlo.DipoleSphere3f

/**
 * The magnetic field applied to a [MagneticMedia] during a recording simulation.
 */
typealias AppliedField = Float

/** The flux of an individual dipole, or the aggregate flux of a collection of dipoles.
 * TODO: maybe we should have a typealias for aggregate flux?
 */
typealias Flux = Float

/**
 * The field at a [DipoleSphere3f] in a [MagneticMedia] that is the result of another [DipoleSphere3f]
 * which has already been fixated during the recording process.  By implication, the other [DipoleSphere3f]
 * has a higher coercivity than the reference [DipoleSphere3f]
 */
typealias InteractionField = Float

