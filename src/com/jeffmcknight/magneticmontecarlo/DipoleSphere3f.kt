package com.jeffmcknight.magneticmontecarlo

import javax.vecmath.Point3f
import javax.vecmath.Vector3f

/**
 * Models the dipole particle
 * TODO: remove unneeded constructors
 * TODO: wrap a [Point3f] instead of subclassing it
 */
open class DipoleSphere3f : Point3f {
    //**********Getters and Setters **********
    var m // dipole moment
            : Float
    var radius // dipole radius
            : Float

    //		return (float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)); 
    var volume // dipole volume
            = 0f
        get() =//		return (float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)); 
            field

    //**********Constructors**********
    constructor() : super() {
        m = 0.0f
        radius = floatDefaultDipoleR
        //		this.setVolume((float) (Math.pow(DipoleSphere3f.floatDefaultDipoleR, 3)));
        volume = (4.0f / 3.0f * Math.PI * Math.pow(radius.toDouble(), 3.0)).toFloat()
        //		this.volume = (float) ((4.0f/3.0f) * Math.PI * Math.pow(this.radius, 3));
    }

    constructor(x: Float, y: Float, z: Float) : super(x, y, z) {
        m = 0.0f
        radius = floatDefaultDipoleR
        volume = (4.0f / 3.0f * Math.PI * Math.pow(radius.toDouble(), 3.0)).toFloat()
    }

    constructor(x: Float, y: Float, z: Float, r: Float, m: Float) : super(x, y, z) {
        this.m = m
        radius = r
        volume = (4.0f / 3.0f * Math.PI * Math.pow(radius.toDouble(), 3.0)).toFloat()
    }

    constructor(point3fNew: Point3f?) : super(point3fNew) {
        m = 0.0f
        radius = floatDefaultDipoleR
        volume = (4.0f / 3.0f * Math.PI * Math.pow(radius.toDouble(), 3.0)).toFloat()
    }

    /**
     * Calculate H at "this" particle due to `dipoleFixed`
     * H = MV(3cos^2(theta)-1)/r^3
     * @ theta =  0 	: 3cos^2(theta)-1 =  2.0
     * @ theta = 30 	: 3cos^2(theta)-1 =  1.25
     * @ theta = 35.26	: 3cos^2(theta)-1 =  1.0
     * @ theta = 45 	: 3cos^2(theta)-1 =  0.5 [corner to opposite corner, adjacent side]
     * @ theta = 54.74 - 3cos^2(theta)-1 =  0.0 [corner to opposite corner, opposite side]
     * @ theta = 60 	- 3cos^2(theta)-1 = -0.25
     * @ theta = 90 	: 3cos^2(theta)-1 = -1.0
     * @param dipoleFixed
     * @return
     */
    fun getHInteraction(dipoleFixed: DipoleSphere3f): Float {
        val vectorFocus = Vector3f() //	relative vector between dipoles
        val r = distance(dipoleFixed).toDouble() //	distance between dipoles
        val theta: Double //	angle between ^Z and relative dipole vector

//		relVector.sub(dipole0.get(), dipole1.get());	// get vector pointing to interaction dipole
        vectorFocus.sub(this, dipoleFixed) // get vector pointing to interaction dipole
        theta = vectorFocus.angle(Companion.z).toDouble()
        //		return (float)(dipole1.getM()* dipole1.getVolume() * 3.0f * Math.pow(Math.cos(theta),2)/Math.pow(r,3));	// H = MV(3cos^2(theta))/r^3
        return (dipoleFixed.m * dipoleFixed.volume * (3.0f * Math.pow(Math.cos(theta), 2.0) - 1.0f) / Math.pow(
            r,
            3.0
        )).toFloat()
    }

    companion object {
        private const val floatPackingFraction = 0.5f // 1.0 is 100% packing fraction
        private const val floatDefaultDipoleR = 0.5f * floatPackingFraction * MonteCarloHysteresisPanel.DEFAULT_INDEX_A
        private val z = Vector3f(0.0f, 0.0f, 1.0f) //	define ^Z
        private const val serialVersionUID = 5659235650411637264L
    }
}