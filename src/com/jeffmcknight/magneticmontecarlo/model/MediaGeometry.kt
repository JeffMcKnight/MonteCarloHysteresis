package com.jeffmcknight.magneticmontecarlo.model

/**
 * The physical properties of a magnetic media with a box-like shape
 * TODO: convert to a sealed class, and make subclasses for specific geometries (box, ellipsoid, etc)
 */
data class MediaGeometry(
        val xCount: Int,
        val yCount: Int,
        val zCount: Int,
        val packingFraction: Float,
        val dipoleRadius: Float
        )
