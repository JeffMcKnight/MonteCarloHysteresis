/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

import java.util.ArrayList;

import javax.vecmath.Point3f;

/**
 * @author jeffmcknight
 *
 */
class DipoleEllipsoid3f extends DipoleSphere3f 
{
	DipoleSphere3f[] ellipsoid3f;
	/**
	 * 
	 */
	public DipoleEllipsoid3f() 
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public DipoleEllipsoid3f(float x, float y, float z) 
	{
		super(x, y, z);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param r
	 */
	public DipoleEllipsoid3f(float x, float y, float z, float r) 
	{
		super(x, y, z, r);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param r
	 * @param m
	 */
	public DipoleEllipsoid3f(float x, float y, float z, float r, float m) 
	{
		super(x, y, z, r, m);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param point3fNew
	 */
	public DipoleEllipsoid3f(Point3f point3fNew) 
	{
		super(point3fNew);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param point3fNew
	 * @param floatNewM
	 */
	public DipoleEllipsoid3f(Point3f point3fNew, float floatNewM) 
	{
		super(point3fNew, floatNewM);
		// TODO Auto-generated constructor stub
	}

} // DipoleEllipsoid3f - END
