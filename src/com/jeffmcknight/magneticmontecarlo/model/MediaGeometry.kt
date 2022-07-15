package com.jeffmcknight.magneticmontecarlo.model

/**
 * The physical properties of a magnetic media with a box-like shape
 * TODO: convert to a sealed class, and make subclasses for specific geometries (box, ellipsoid, etc)
 */
data class MediaGeometry(
        val xCount: Int = 1,
        val yCount: Int = 1,
        val zCount: Int = 1,
        val packingFraction: Float = 1F,
        val dipoleRadius: Float =1F
        )
