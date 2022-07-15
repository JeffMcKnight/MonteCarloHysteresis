/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

import javax.vecmath.Point3f;

/**
 * @author jeffmcknight
 *
 */
@SuppressWarnings("serial")
// No need to serialize objects of this type.
class DipoleEllipsoid3f extends DipoleSphere3f {
	
	DipoleSphere3f[] ellipsoid3f;

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param r
	 * @param m
	 */
	public DipoleEllipsoid3f(float x, float y, float z, float r, float m) {
		super(x, y, z, r, m);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param point3fNew
	 */
	public DipoleEllipsoid3f(Point3f point3fNew) {
		super(point3fNew);
		// TODO Auto-generated constructor stub
	}

} // DipoleEllipsoid3f - END
