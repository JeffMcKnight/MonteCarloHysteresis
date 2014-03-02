package com.jeffmcknight.magneticmontecarlo;

//import java.util.*;
import java.util.ArrayList;
//import java.util.List;

import javax.vecmath.Point3f;



// **********Class for Single Domain Particle Assembly
// **********Container for dipole elements 
//**********xDim,yDim,zDim indicate dipole count in each direction
public class MagneticMedia extends ArrayList<DipoleSphere3f> 		
{
	private static final long serialVersionUID = 1L; 
	private float mRemnanace;					// Magnetic Remnanace for SDP assembly
	private float mLatticeIndex;					// Lattice constant, i.e.: spacing between particles on lattice grid
	private int mXCount;				// cell count in x direction
	private int mYCount;				// cell count in y direction
	private int mZCount;				// cell count in z direction
	private float mDipoleRadius;
	private float mPackingFraction;
//	private int particlesCount;			// Number of dipoles particles in lattice
//	private Tuple3i gridDimensions;		// set lattice dimensions
//	private float hDC;					// Applied DC magnetic field
//	@SuppressWarnings("rawtypes")
//	private ArrayList arrayList;			// Set of dipole particles in SDP assembly
	
	
	
//**********Constructors**********
	public MagneticMedia() 
	{
		super(0);			// create empty arraylist
		this.mLatticeIndex = MonteCarloHysteresisPanel.DEFAULT_INDEX_A;
		this.mRemnanace = 0.0f;
		this.mXCount = 1;
		this.mYCount = 1;
		this.mZCount = 1;
		this.populateSequential();
//		this.RandomizeLattice();
	}
		
	public MagneticMedia(int intSize) 
	{
		super(intSize);			// create with "intSize" elements; assumes cube with 1 unit per side
		this.mLatticeIndex = MonteCarloHysteresisPanel.DEFAULT_INDEX_A;
		this.mRemnanace = 0.0f;
		this.mXCount = 1;
		this.mYCount = 1;
		this.mZCount = 1;
		this.populateSequential();
//		this.RandomizeLattice();
	}
		
	public MagneticMedia(int x, int y, int z) 
	{
		super(0);
		this.mLatticeIndex = MonteCarloHysteresisPanel.DEFAULT_INDEX_A;
		this.mRemnanace = 0.0f;
		this.mXCount = x;
		this.mYCount = y;
		this.mZCount = z;
		this.populateSequential();
//		this.RandomizeLattice();
	}
	
	public MagneticMedia(int x, int y, int z, float packingFraction, float dipoleRadius) 
	{
		super(0);
		this.mLatticeIndex = 2f * dipoleRadius / packingFraction ;
		this.mRemnanace = 0.0f;
		this.mXCount = x;
		this.mYCount = y;
		this.mZCount = z;
		this.mDipoleRadius = dipoleRadius;
		this.mPackingFraction = packingFraction;
		this.populateSequential();
//		this.RandomizeLattice();
	}
	
	public MagneticMedia(float spacing) 
	{
		super(0);
		this.mLatticeIndex = spacing;
		this.mRemnanace = 0.0f;
		this.mXCount = 1;
		this.mYCount = 1;
		this.mZCount = 1;
		this.populateSequential();
//		this.RandomizeLattice();
	}
	
	//**********populateSequential()**********
	public void populateSequential() 
	{
		int gridIndex = 0;						// gridIndex is the initial index number of the dipole being added to the ArrayList
		for (int k = 0; k < this.mZCount; k++) 
		{	for (int j = 0; j < this.mYCount; j++) 
			{	for (int i = 0; i < this.mXCount; i++) 
				{
					DipoleSphere3f newDipole = new DipoleSphere3f();	// newDipole is a temp Dipole object to be added to ArrayList
//					gridIndex = i + (j * (this.xCellCount)) + (k * (this.xCellCount) * (this.yCellCount));
					newDipole.set(i*this.mLatticeIndex, j*this.mLatticeIndex, k*this.mLatticeIndex);
					newDipole.setRadius(this.mDipoleRadius);
					this.add(gridIndex, newDipole);
					gridIndex = gridIndex + 1;
				}
			}
		}
	}

	//**********addDipoleAt()**********
	// Add dipole at the specified coords to the end of the current particle assembly
	public void addDipoleAt(int x, int y, int z) 
	{
		DipoleSphere3f dipolefTemp = new DipoleSphere3f(x*this.mLatticeIndex, y*this.mLatticeIndex, z*this.mLatticeIndex);	// newDipole is a temp Dipole object to be added to ArrayList
		this.add(dipolefTemp);
	}

	//**********addDipoleAt()**********
	// Add dipole at the specified coords to the end of the current particle assembly
	public void addDipoleAt(Point3f point3fDipoleCoord) 
	{
		DipoleSphere3f dipolefTemp = new DipoleSphere3f(point3fDipoleCoord);	// newDipole is a temp Dipole object to be added to ArrayList
		this.add(dipolefTemp);
	}

	
	
	//**********randomizeLattice()**********
	// Randomize dipoles of the current particle assembly
	public void randomizeLattice() 
	{
		int randomNum;
		DipoleSphere3f dipolefTemp = new DipoleSphere3f(); 
				
		for (int i = 0; i < this.getCellCount(); i++) 
		{
			randomNum = i + (int)(Math.random()*(this.getCellCount() - i));	// genereate random number
			dipolefTemp = this.get(randomNum);	// save Dipolef at randomNum to a temp Dipolef; put breakpoint here		
			this.set(randomNum, this.get(i));	// move dipole at i to randomNum 
			this.set(i, dipolefTemp);			// move dipole at randomNum to i
		}
	}

	//**********recordToM()**********
	public float recordToM (float hApplied) 
	{
		float m = 0.0f;
//		System.out.println("n,    m");
		for (int i = 0; i < this.getCellCount(); i++) 
			{ 
			this.get(i).setM(this.fixateDipole(i, hApplied)); 
			m = m + this.get(i).getM(); 
//			System.out.println(i + " , " + m/(i+1));
			}
//		for (int i = 0; i < this.getCellCount(); i++) 
//			{ m = m + this.get(i).getM(); }
		return m/this.getCellCount();
	}

	//**********fixateM()**********
	//** Fix the dipole orientation for a single particle **
	public float fixateDipole(int i, float floatHApplied) 
	{
		float floatB = floatHApplied;
		for (int j = 0; j < i; j++) 
		{
			floatB = floatB + this.get(i).getHInteraction(this.get(j));
		}
		if (floatB > 0.0f)	{return( MonteCarloHysteresisPanel.SATURATION_M);}
		else				{return(-MonteCarloHysteresisPanel.SATURATION_M);}
	}
	
	
	//**********calculateNetM()**********
	//** Calculate the net magnetism, M, for the entire particle assembly **
	public float calculateNetM() 
	{
		this.mRemnanace = 0.0f;
		for (int i = 0; i < this.size(); i++) 
		{
			mRemnanace += this.get(i).getM();
		}
		return (mRemnanace/(float)this.size());
	}

	public float getDipoleRadius() 
	{
		return mDipoleRadius;
	}

	public void setDipoleRadius(float dipoleRadius) 
	{
		this.mDipoleRadius = dipoleRadius;
	}

	//	Setters and Getters
	public float getM() 
	{
		return this.mRemnanace;
	}
	public void setM(float mNet) 
	{
		this.mRemnanace = mNet;
	}

	public float getA() 
	{
		return mLatticeIndex;
	}
	public void setA(float latticeConstant) 
	{
		this.mLatticeIndex = latticeConstant;
	}

	public int getCellCount() 
	{
		return(this.size());
	}
/*
	public float gethDC() {
		return hDC;
	}
*/

	//******************** getPackingFraction() ********************
	public float getPackingFraction() 
	{
		return mPackingFraction;
	}

	//******************** setPackingFraction() ********************
	public void setPackingFraction(float packingFraction) 
	{
		this.mPackingFraction = packingFraction;
	}

	public int getXCount() {
		return mXCount;
	}

	public int getYCount() {
		return mYCount;
	}

	public int getZCount() {
		return mZCount;
	}

/*
	public void sethDC(float hDC) {
		this.hDC = hDC;
	}
*/

/*	public ArrayList<Dipole> getDipoleArray() {
		return dipole;
	}

	public void setDipoleArray(ArrayList<Dipole> dipole) {
		this.dipole = dipole;
	}

	public Dipole getDipole(int listIndex) {
		return dipole.get(listIndex);
	}

	public void setDipole(int listIndex, Dipole d0) {
		dipole.set(listIndex, d0);
	}
*/

}
