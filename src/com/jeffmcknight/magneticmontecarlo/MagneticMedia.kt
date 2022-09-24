package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.MonteCarloHysteresisPanel.Companion.SATURATION_M
import com.jeffmcknight.magneticmontecarlo.model.AppliedField
import com.jeffmcknight.magneticmontecarlo.model.InteractionField
import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import com.jeffmcknight.magneticmontecarlo.model.RecordedField
import javax.vecmath.Point3f
import kotlin.random.Random

/**
 * Class for Single Domain Particle Assembly
 * Container for dipole elements
 */
class MagneticMedia : ArrayList<DipoleSphere3f> {
    val saturationFlux: RecordedField
        get() = m

    /**
     * Intermediate results. This is a list of the [InteractionField]s that we calculated at each
     * [DipoleSphere3f] as we determined whether that dipole would fixate up or down.
     */
    val netInteractionFields by lazy { MutableList<InteractionField>(size) { Float.NaN } }
    val geometry: MediaGeometry
        get() {
            return MediaGeometry(xCount, yCount, zCount, packingFraction, dipoleRadius)
        }

    /**  Magnetic Remnanace for SDP assembly */
    var m: Float
    /** Lattice constant, i.e.: spacing between particles on lattice grid */
    var a: Float
    /** cell count in x direction */
    var xCount: Int
        private set
    /** cell count in y direction */
    var yCount: Int
        private set
    /** cell count in z direction */
    var zCount: Int
        private set

    var dipoleRadius = 0f

    var packingFraction = 0f

    constructor(x: Int = 0, y: Int = 0, z: Int = 0) : super(x * y * z) {
        a = MonteCarloHysteresisPanel.DEFAULT_INDEX_A
        m = 0.0f
        xCount = x
        yCount = y
        zCount = z
        populateSequential()
        //		this.RandomizeLattice();
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param packingFraction
     * @param dipoleRadius
     */
    constructor(
        x: Int,
        y: Int,
        z: Int,
        packingFraction: Float,
        dipoleRadius: Float
    ) : super(x * y * z) {
        a = 2f * dipoleRadius / packingFraction
        m = 0.0f
        xCount = x
        yCount = y
        zCount = z
        this.dipoleRadius = dipoleRadius
        this.packingFraction = packingFraction
        populateSequential()
    }

    /**
     *
     */
    private fun populateSequential() {
        var gridIndex = 0 // gridIndex is the initial index number of the dipole being added to the ArrayList
        for (k in 0 until zCount) {
            for (j in 0 until yCount) {
                for (i in 0 until xCount) {
                    val newDipole = DipoleSphere3f() // newDipole is a temp Dipole object to be added to ArrayList
                    //					gridIndex = i + (j * (this.xCellCount)) + (k * (this.xCellCount) * (this.yCellCount));
                    newDipole[i * a, j * a] = k * a
                    newDipole.radius = dipoleRadius
                    this.add(gridIndex, newDipole)
                    gridIndex = gridIndex + 1
                }
            }
        }
    }

    //**********addDipoleAt()**********
    // Add dipole at the specified coords to the end of the current particle assembly
    fun addDipoleAt(point3fDipoleCoord: Point3f?) {
        val dipolefTemp = DipoleSphere3f(point3fDipoleCoord) // newDipole is a temp Dipole object to be added to ArrayList
        this.add(dipolefTemp)
    }

    //**********randomizeLattice()**********
    // Randomize dipoles of the current particle assembly
    fun randomizeLattice() {
        var randomNum: Int
        var dipoleTemp: DipoleSphere3f
        for (i in 0 until cellCount) {
            randomNum = i + (Math.random() * (cellCount - i)).toInt() // genereate random number
            dipoleTemp = this[randomNum] // save Dipolef at randomNum to a temp Dipolef; put breakpoint here
            this[randomNum] = this[i] // move dipole at i to randomNum 
            this[i] = dipoleTemp // move dipole at randomNum to i
        }
    }

    /**
     * TODO: implement with coroutines for better performance
     * @param hApplied
     * @return
     */
    fun recordWithAcBias(hApplied: AppliedField): RecordedField {
        var m = 0.0f
        for (i in 0 until cellCount) {
            // Fixate a single dipole up or down.
            this[i].m = fixateDipole(i, hApplied)
            m += this[i].m
        }
        return m / cellCount
    }

    /**
     * Fixate the magnetic orientation for a single particle.
     *
     * We randomly chose +/- [SATURATION_M] if [netBField] == 0; the most common case is when we calculate the first
     * dipole for an applied field of 0.0F.
     * TODO: calculate the proper return value rather than use the arbitrarily chosen [SATURATION_M]
     *
     * @return the recorded magnetic field for the [DipoleSphere3f] at index [i]
     */
    private fun fixateDipole(i: Int, appliedHField: Float): Float {
        val netMField = calculateNetMField(i)
        val netBField = netMField + appliedHField
        netInteractionFields[i] = netMField
        return when {
            netBField > 0.0f -> SATURATION_M
            netBField < 0.0f -> -SATURATION_M
            else -> if (Random.nextBoolean()) SATURATION_M else -SATURATION_M
        }
    }

    /**
     * Iterate through all the dipoles with index **less than** [i] and add up all of their interaction fields (H)
     * relative to the dipole with index [i]
     * @param i - the index of the dipole where we calculate M, the net magnetic remanence field
     * @return - the net magnetic remanence field, M, at dipole with index "i"
     */
    private fun calculateNetMField(i: Int): InteractionField {
        var netMField: InteractionField = 0f
//        runBlocking { // this: CoroutineScope
//            launch { // launch a new coroutine and continue
//                delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
//                println("World!") // print after delay
//            }
//            println("Hello") // main coroutine continues while a previous one is delayed
//        }
//        for (j in 0 until i) {
//            netMField += this[i]!!.getHInteraction(this[j])
//        }
        val dipole: DipoleSphere3f = this[i]
        this.subList(0, (i - 1).coerceAtLeast(0))
                .forEach { otherDipole ->
//                    runBlocking<Unit> {
//                        netMField += calculateH(dipole, otherDipole)
//                    }
                    netMField += dipole.getHInteraction(otherDipole)
                }
        return netMField
    }

    //** Calculate the net magnetism, M, for the entire particle assembly **
    fun calculateNetM(): Float {
        m = 0.0f
        for (i in this.indices) {
            m += this[i].m
        }
        return m / this.size.toFloat()
    }

    val cellCount: Int
        get() = this.size

    companion object {
        val empty = MagneticMedia()
        fun create(geometry: MediaGeometry): MagneticMedia {
            return with(geometry) {
                MagneticMedia(xCount, yCount, zCount, packingFraction, dipoleRadius).also {
                    it.randomizeLattice()
                }
            }
        }

        private const val serialVersionUID = 1L
        private val TAG = MagneticMedia::class.java.simpleName
    }
}
