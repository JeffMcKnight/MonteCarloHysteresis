package com.jeffmcknight.magneticmontecarlo

import com.jeffmcknight.magneticmontecarlo.model.MediaGeometry
import javax.vecmath.Point3f

//import java.util.*;
//import java.util.List;
// **********Class for Single Domain Particle Assembly
// **********Container for dipole elements 
//**********xDim,yDim,zDim indicate dipole count in each direction
class MagneticMedia : ArrayList<DipoleSphere3f> {
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

    // ********** Setters and Getters **********
    var dipoleRadius = 0f

    //******************** setPackingFraction() ********************
    //******************** getPackingFraction() ********************
    var packingFraction = 0f

    //	private int particlesCount;			// Number of dipoles particles in lattice
    //	private Tuple3i gridDimensions;		// set lattice dimensions
    //	private float hDC;					// Applied DC magnetic field
    //	@SuppressWarnings("rawtypes")
    //	private ArrayList arrayList;			// Set of dipole particles in SDP assembly
    private var mUpdateListener: MagneticMediaListener? = null

    interface MagneticMediaListener {
        fun onRecordingDone(magneticMedia: MagneticMedia)
        fun onDipoleFixated(dipoleSphere3f: DipoleSphere3f)
    }

    //**********Constructors**********
    constructor(x: Int, y: Int, z: Int) : super(x * y * z) {
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
     * @param updateListener
     */
    constructor(
            x: Int,
            y: Int,
            z: Int,
            packingFraction: Float,
            dipoleRadius: Float,
            updateListener: MagneticMediaListener?) : super(x * y * z) {
        a = 2f * dipoleRadius / packingFraction
        m = 0.0f
        xCount = x
        yCount = y
        zCount = z
        this.dipoleRadius = dipoleRadius
        this.packingFraction = packingFraction
        populateSequential()
        mUpdateListener = updateListener
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
    fun addDipoleAt(x: Int, y: Int, z: Int) {
        val dipolefTemp = DipoleSphere3f(x * a, y * a, z * a) // newDipole is a temp Dipole object to be added to ArrayList
        this.add(dipolefTemp)
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
    fun recordWithAcBias(hApplied: Float): Float {
        var m = 0.0f
        for (i in 0 until cellCount) {
            // Fixate a single dipole up or down.
            this[i].m = fixateDipole(i, hApplied)
            // Notify listener
            mUpdateListener?.onDipoleFixated(this[i])
            m += this[i].m
        }
        println("$TAG\t.recordWithAcBias()\t -- mUpdateListener: $mUpdateListener")
        // TODO: Use this Listener for both MHCurve and individual dipole charts so it will never be null.
        mUpdateListener?.onRecordingDone(this)
        return m / cellCount
    }

    /**
     * Fixate the magnetic orientation for a single particle
     */
    private fun fixateDipole(i: Int, appliedHField: Float): Float {
        val fixatedM: Float
        val netMField = calculateNetMField(i)
        val netBField = netMField + appliedHField
        fixatedM = if (netBField > 0.0f) {
            MonteCarloHysteresisPanel.SATURATION_M
        } else {
            -MonteCarloHysteresisPanel.SATURATION_M
        }
        return fixatedM
    }

    /**
     * Iterate through all the dipoles with index **less than** [i] and add up all of their interaction fields (H)
     * relative to the dipole with index [i]
     * @param i - the index of the dipole where we calculate M, the net magnetic remanence field
     * @return - the net magnetic remanence field, M, at dipole with index "i"
     */
    private fun calculateNetMField(i: Int): Float {
        var netMField = 0f
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

    /**
     * TODO: implement using ThreadMessager and ChunkMaker
     * @param i - the index of the dipole where we calculate M, the net magnetic remanence field
     * @return - the net magnetic remanence field, M, at dipole with index "i"
     */
    private fun calculateNetMFieldMultiThreaded(i: Int): Float {
        var netMField = 0f
        for (j in 0 until i) {
            netMField += this[i].getHInteraction(this[j])
        }
        return netMField
    }

    private suspend fun calculateH(dipole: DipoleSphere3f, otherDipole: DipoleSphere3f?) =
            dipole.getHInteraction(otherDipole)

    //**********calculateNetM()**********
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
        fun create(geometry: MediaGeometry, listener: MagneticMediaListener?): MagneticMedia {
            return with(geometry) {
                MagneticMedia(xCount, yCount, zCount, packingFraction, dipoleRadius, listener).also {
                    it.randomizeLattice()
                }
            }
        }

        private const val serialVersionUID = 1L
        private val TAG = MagneticMedia::class.java.simpleName
    }
}