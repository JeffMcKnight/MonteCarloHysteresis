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
	private float m;					// Magnetic Remnanace for SDP assembly
	private float a;					// Lattice constant, i.e.: spacing between particles on lattice grid
	private int xCellCount;				// cell count in x direction
	private int yCellCount;				// cell count in y direction
	private int zCelCount;				// cell count in z direction
	private float dipoleRadius;
	private float packingFraction;
//	private int particlesCount;			// Number of dipoles particles in lattice
//	private Tuple3i gridDimensions;		// set lattice dimensions
//	private float hDC;					// Applied DC magnetic field
//	@SuppressWarnings("rawtypes")
//	private ArrayList arrayList;			// Set of dipole particles in SDP assembly
	
	
	
//**********Constructors**********
	public MagneticMedia() 
	{
		super(0);			// create empty arraylist
		this.a = MonteCarloHysteresisPanel.DEFAULT_INDEX_A;
		this.m = 0.0f;
		this.xCellCount = 1;
		this.yCellCount = 1;
		this.zCelCount = 1;
		this.populateSequential();
//		this.RandomizeLattice();
	}
		
	public MagneticMedia(int intSize) 
	{
		super(intSize);			// create with "intSize" elements; assumes cube with 1 unit per side
		this.a = MonteCarloHysteresisPanel.DEFAULT_INDEX_A;
		this.m = 0.0f;
		this.xCellCount = 1;
		this.yCellCount = 1;
		this.zCelCount = 1;
		this.populateSequential();
//		this.RandomizeLattice();
	}
		
	public MagneticMedia(int x, int y, int z) 
	{
		super(0);
		this.a = MonteCarloHysteresisPanel.DEFAULT_INDEX_A;
		this.m = 0.0f;
		this.xCellCount = x;
		this.yCellCount = y;
		this.zCelCount = z;
		this.populateSequential();
//		this.RandomizeLattice();
	}
	
	public MagneticMedia(int x, int y, int z, float packingFraction, float dipoleRadius) 
	{
		super(0);
		this.a = 2f * dipoleRadius / packingFraction ;
		this.m = 0.0f;
		this.xCellCount = x;
		this.yCellCount = y;
		this.zCelCount = z;
		this.dipoleRadius = dipoleRadius;
		this.packingFraction = packingFraction;
		this.populateSequential();
//		this.RandomizeLattice();
	}
	
	public MagneticMedia(float spacing) 
	{
		super(0);
		this.a = spacing;
		this.m = 0.0f;
		this.xCellCount = 1;
		this.yCellCount = 1;
		this.zCelCount = 1;
		this.populateSequential();
//		this.RandomizeLattice();
	}
	
	//**********populateSequential()**********
	public void populateSequential() 
	{
		int gridIndex = 0;						// gridIndex is the initial index number of the dipole being added to the ArrayList
		for (int k = 0; k < this.zCelCount; k++) 
		{	for (int j = 0; j < this.yCellCount; j++) 
			{	for (int i = 0; i < this.xCellCount; i++) 
				{
					DipoleSphere3f newDipole = new DipoleSphere3f();	// newDipole is a temp Dipole object to be added to ArrayList
//					gridIndex = i + (j * (this.xCellCount)) + (k * (this.xCellCount) * (this.yCellCount));
					newDipole.set(i*this.a, j*this.a, k*this.a);
					newDipole.setRadius(this.dipoleRadius);
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
		DipoleSphere3f dipolefTemp = new DipoleSphere3f(x*this.a, y*this.a, z*this.a);	// newDipole is a temp Dipole object to be added to ArrayList
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
		this.m = 0.0f;
		for (int i = 0; i < this.size(); i++) 
		{
			m += this.get(i).getM();
		}
		return (m/(float)this.size());
	}

	public float getDipoleRadius() 
	{
		return dipoleRadius;
	}

	public void setDipoleRadius(float dipoleRadius) 
	{
		this.dipoleRadius = dipoleRadius;
	}

	//	Setters and Getters
	public float getM() 
	{
		return this.m;
	}
	public void setM(float mNet) 
	{
		this.m = mNet;
	}

	public float getA() 
	{
		return a;
	}
	public void setA(float latticeConstant) 
	{
		this.a = latticeConstant;
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
		return packingFraction;
	}

	//******************** setPackingFraction() ********************
	public void setPackingFraction(float packingFraction) 
	{
		this.packingFraction = packingFraction;
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
