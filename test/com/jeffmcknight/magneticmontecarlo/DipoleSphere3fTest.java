package com.jeffmcknight.magneticmontecarlo;

//import javax.vecmath.Point3f;
//import javax.vecmath.Vector3d;

import junit.framework.TestCase;

public class DipoleSphere3fTest extends TestCase 
{
	DipoleSphere3f dipole000 = new DipoleSphere3f(0.0f,0.0f,0.0f,0.5f,1.0f);
	DipoleSphere3f dipole001 = new DipoleSphere3f(1.0f,0.0f,0.0f,0.5f,1.0f);
	DipoleSphere3f dipole100 = new DipoleSphere3f(0.0f,0.0f,1.0f,0.5f,1.0f);
	DipoleSphere3f dipole101 = new DipoleSphere3f(1.0f,0.0f,1.0f,0.5f,1.0f);
	DipoleSphere3f dipole111 = new DipoleSphere3f(1.0f,1.0f,1.0f,0.5f,1.0f);
	

	public void testDipole() 
	{
//		Test zero-argument constructor; assumes grid spacing = 1
		assertEquals(1.0, dipole000.getM(), 0);
		assertEquals(0.5235987912027583, dipole000.getVolume(), 0.0001f);
	
//		Test one-argument constructor
//		assertEquals(0.0, d1.getM(), 0);
//		assertEquals(4.1887903296220665, d1.getVolume(), 0.0001f);

//		Test two-argument constructor
//		assertEquals(0.0, d2.getM(), 0);
//		assertEquals(4.1887903296220665, d2.getVolume(), 0.0001f);
//		assertTrue(d2.getPosition().equals(z));
	}

	//**********testGetHInteraction()**********
	//** Calculate H and then cancel out r^3, M, & V **
	public void testGetHInteraction() 
	{
		// theta = 0 -> 3cos^2 - 1 = 2       (r=1)
		assertEquals(2.0f, (dipole100.getHInteraction(dipole000)) * Math.pow(1.0f,1.5f)/(dipole000.getM() * dipole000.getVolume()), 0.001f);
		// theta = 90 -> 3cos^2 - 1 = -1     (r=1)
 		assertEquals(-1.0f, (dipole001.getHInteraction(dipole000)) * Math.pow(1.0f,1.5f)/(dipole000.getM() * dipole000.getVolume()), 0.001f);
		// theta = 45 -> 3cos^2 - 1 = 0.5    (r=√2)
		assertEquals(0.5f, (dipole101.getHInteraction(dipole000)) * Math.pow(2.0f,1.5f)/(dipole000.getM() * dipole000.getVolume() ), 0.001f);
		// theta = 54.74 -> 3cos^2 - 1 = 0.0 (r=√3)  
		assertEquals(0.0f, (dipole111.getHInteraction(dipole000)) * Math.pow(3.0f,1.5f)/(dipole000.getM() * dipole000.getVolume() ), 0.001f);
	}

}
