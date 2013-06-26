package com.jeffmcknight.magneticmontecarlo;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

public class DipoleSphere3f extends Point3f 
{
	private static final float floatPackingFraction = 0.5f; // 1.0 is 100% packing fraction
	private static final float floatDefaultDipoleR = 0.5f * floatPackingFraction * MonteCarloHysteresisPanel.defaultIndexA;
	private static final Vector3f z = new Vector3f(0.0f, 0.0f, 1.0f);					//	define ^Z
	private static final long serialVersionUID = 5659235650411637264L;
	private float m;				// dipole moment
	private float radius;			// dipole radius
	private float volume;			// dipole volume

	
	//**********Constructors**********
	public DipoleSphere3f() 
	{
		super();
		this.m= 0.0f;
		this.radius = DipoleSphere3f.floatDefaultDipoleR; 
//		this.setVolume((float) (Math.pow(DipoleSphere3f.floatDefaultDipoleR, 3)));
		this.setVolume((float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)));
//		this.volume = (float) ((4.0f/3.0f) * Math.PI * Math.pow(this.radius, 3));
	}

	public DipoleSphere3f(float x, float y, float z) 
	{
		super(x, y, z);
		this.m = 0.0f;
		this.radius = DipoleSphere3f.floatDefaultDipoleR; 
		this.setVolume((float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)));
	}

	public DipoleSphere3f(float x, float y, float z, float r) 
	{
		super(x, y, z);
		this.m = 0.0f;
		this.radius = r; 
		this.setVolume((float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)));
	}

	public DipoleSphere3f(float x, float y, float z, float r, float m) 
	{
		super(x, y, z);
		this.m = m;
		this.radius = r; 
		this.setVolume((float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)));
	}

	public DipoleSphere3f(Point3f point3fNew) 
	{
		super(point3fNew);
		this.m= 0.0f;
		this.radius = DipoleSphere3f.floatDefaultDipoleR; 
		this.setVolume((float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)));
	}

	public DipoleSphere3f(Point3f point3fNew, float floatNewM) 
	{
		super(point3fNew);
		this.m = floatNewM;
		this.radius = DipoleSphere3f.floatDefaultDipoleR; 
		this.setVolume((float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)));
	}
	//**********Constructors [End]**********


	//**********Methods**********
	
	//**********getHInteraction()**********
	//** Calculate H at "this" particle due to dipole1 **
	public float getHInteraction (DipoleSphere3f dipoleFixed) 
	{
		Vector3f vectorFocus = new Vector3f();							//	relative vector between dipoles
		double r = this.distance(dipoleFixed);								//	distance between dipoles
		double theta;													//	angle between ^Z and relative dipole vector
				
//		relVector.sub(dipole0.get(), dipole1.get());	// get vector pointing to interaction dipole
		vectorFocus.sub(this, dipoleFixed);	// get vector pointing to interaction dipole
		theta = vectorFocus.angle(z);
//		return (float)(dipole1.getM()* dipole1.getVolume() * 3.0f * Math.pow(Math.cos(theta),2)/Math.pow(r,3));	// H = MV(3cos^2(theta))/r^3

		// H = MV(3cos^2(theta)-1)/r^3
		//		@ theta =  0 	: 3cos^2(theta)-1 =  2.0
		//		@ theta = 30 	: 3cos^2(theta)-1 =  1.25
		//		@ theta = 35.26	: 3cos^2(theta)-1 =  1.0 
		//		@ theta = 45 	: 3cos^2(theta)-1 =  0.5 [corner to opposite corner, adjacent side]
		//		@ theta = 54.74 - 3cos^2(theta)-1 =  0.0 [corner to opposite corner, opposite side]
		//		@ theta = 60 	- 3cos^2(theta)-1 = -0.25
		//		@ theta = 90 	: 3cos^2(theta)-1 = -1.0
		return (float)(dipoleFixed.getM()* dipoleFixed.getVolume() * ((3.0f * Math.pow(Math.cos(theta),2))-1.0f)/Math.pow(r,3));	
	}

	//**********Getters and Setters **********
	public float getM() 
	{
		return m;
	}
	public void setM(float m) 
	{
		this.m = m;
	}

	public void setMUp() 
	{
		this.m = MonteCarloHysteresisPanel.saturationM;
	}

	public void setMDown() 
	{
		this.m = -MonteCarloHysteresisPanel.saturationM;
	}

	public float getRadius() 
	{
		return radius;
	}

	public void setRadius(float radius) 
	{
		this.radius = radius;
	}

	public float getVolume() 
	{
//		return (float) ((4.0f/3.0f) * Math.PI * Math.pow(this.getRadius(), 3)); 
		return (float) volume; 
	}

	public void setVolume(float volume) 
	{
		this.volume = volume;
	}

}
