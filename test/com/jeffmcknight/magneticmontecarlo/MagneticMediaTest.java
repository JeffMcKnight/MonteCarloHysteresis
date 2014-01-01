package com.jeffmcknight.magneticmontecarlo;

//import java.util.ArrayList;

import javax.vecmath.Point3f;
//import javax.vecmath.Point3i;
//import javax.vecmath.Tuple3f;
//import javax.vecmath.Tuple3i;

//import javax.vecmath.Vector3d;

import junit.framework.TestCase;


public class MagneticMediaTest extends TestCase {
//	private float spacing = 1.0f;
//	private int sampleIndex = 0;
//	private Tuple3i tuple0 = new Tuple3i(1,2,3);
	
//	private SDP size0;
//	private Point3f point0 = new Point3f(1.0f, 0.0f, 1.0f);
//	private Point3i pi0 = new Point3i(1,1,1);
	
	//********************testPopulateLattice()********************
	public void testPopulateLattice() 
	{
		Point3f point000 = new Point3f(0.0f, 0.0f, 0.0f);
		Point3f point110 = new Point3f(0.0f, 1.0f, 1.0f);

		MagneticMedia sdpTrivial = new MagneticMedia();
		assertEquals(1, sdpTrivial.getCellCount());

		MagneticMedia sdpUnitCube = new MagneticMedia(2,2,2);
		assertEquals(8, sdpUnitCube.getCellCount());
		assertTrue( "Lattice populate at 0,0,0 failed", sdpUnitCube.get(0).equals(point000) );
		assertTrue( "Lattice populate at 0,1,1 failed", sdpUnitCube.get(6).equals(point110) );
	}

	//********************testAddDipoleAt()********************
	public void testAddDipoleAt() 
	{
		Point3f point000 = new Point3f(0.0f, 0.0f, 0.0f);
		Point3f point001 = new Point3f(1.0f, 0.0f, 0.0f);
		Point3f point100 = new Point3f(0.0f, 0.0f, 1.0f);
		MagneticMedia sdpMinimal = new MagneticMedia(0, 0, 0);

		sdpMinimal.clear();
		assertEquals(0, sdpMinimal.size());

		sdpMinimal.addDipoleAt(point000);
		sdpMinimal.addDipoleAt(point001);
		sdpMinimal.addDipoleAt(point100);
		assertEquals(3, sdpMinimal.size());
		assertTrue( "Wrong dipole at index 0", sdpMinimal.get(0).equals(point000) );
		assertTrue( "Wrong dipole at index 1", sdpMinimal.get(1).equals(point001) );		
		assertTrue( "Wrong dipole at index 2", sdpMinimal.get(2).equals(point100) );		
	}

/*	
//********************testFixateM()********************
	public void testFixateM() 
	{
		SDP sdpMinimal = new SDP(0);
		sdpMinimal.AddDipoleAt(0, 0, 0);
		sdpMinimal.AddDipoleAt(1, 0, 0);
		for (int i = 0; i < sdpMinimal.size(); i++) 
		{
			if ((i%2)==0)	{sdpMinimal.get(i).setMUp();	}
			else			{sdpMinimal.get(i).setMDown();	};
		}		
	}
*/	

	
	public void testCalculateNetM() 
	{
		float floatHApplied = 0.001f;
		int intAxisLength;

		Point3f point000 = new Point3f(0.0f, 0.0f, 0.0f);
//		Point3f point001 = new Point3f(1.0f, 0.0f, 0.0f);
//		Point3f point100 = new Point3f(0.0f, 0.0f, 1.0f);
		Point3f point10r2m = new Point3f(1.41f, 0.0f, 1.0f);
		Point3f point10r2p = new Point3f(1.42f, 0.0f, 1.0f);
		Point3f point110 = new Point3f(0.0f, 1.0f, 1.0f);
//		Point3f point111 = new Point3f(1.0f, 1.0f, 1.0f);

		MagneticMedia sdpCorner = new MagneticMedia(0, 0, 1);
		sdpCorner.clear();
		assertEquals(0, sdpCorner.size());
		sdpCorner.addDipoleAt(point000);
		sdpCorner.addDipoleAt(point110);
		assertEquals(2, sdpCorner.size());
		assertEquals(100.0f, sdpCorner.recordToM(floatHApplied), 0.001f);

		MagneticMedia sdpRoot2Minus = new MagneticMedia(0, 0, 1);
		sdpRoot2Minus.clear();
		assertEquals(0, sdpRoot2Minus.size());
		sdpRoot2Minus.addDipoleAt(point000);
		sdpRoot2Minus.addDipoleAt(point10r2m);
		assertEquals(2, sdpRoot2Minus.size());
		assertEquals(100.0f, sdpRoot2Minus.recordToM(floatHApplied), 0.001f);

	
		MagneticMedia sdpRoot2Plus = new MagneticMedia(0, 0, 1);
		sdpRoot2Plus.clear();
		assertEquals(0, sdpRoot2Plus.size());
		sdpRoot2Plus.addDipoleAt(point000);
		sdpRoot2Plus.addDipoleAt(point10r2p);
		assertEquals(2, sdpRoot2Plus.size());
		assertEquals(0.0f, sdpRoot2Plus.recordToM(floatHApplied), 0.001f);
	
		floatHApplied = -0.001f;
		assertEquals(-100.0f, sdpRoot2Minus.recordToM(floatHApplied), 0.001f);
		assertEquals(   0.0f, sdpRoot2Plus.recordToM(floatHApplied), 0.001f);

		
//		***************Test netM for a collection of dipoles along a linear axis*************************
		floatHApplied = 0.001f;
		intAxisLength = 1000;
		MagneticMedia sdpXaxis = new MagneticMedia(intAxisLength, 1, 1);
		MagneticMedia sdpYaxis = new MagneticMedia(1, intAxisLength, 1);
		MagneticMedia sdpZaxis = new MagneticMedia(1, 1, intAxisLength);

//		Size should be number of dipoles along axis
		assertEquals(intAxisLength, sdpXaxis.size());
		assertEquals(intAxisLength, sdpYaxis.size());
		assertEquals(intAxisLength, sdpZaxis.size());

		
//		Along X axis, net M should be zero for an even number of dipoles and M/intAxisLength for an odd number of dipoles
		assertEquals((intAxisLength % 2)*MonteCarloHysteresisPanel.SATURATION_M/intAxisLength, sdpXaxis.recordToM(floatHApplied), 0.001f);
//		Along Y axis, net M should be zero for an even number of dipoles and M/intAxisLength for an odd number of dipoles
		assertEquals((intAxisLength % 2)*MonteCarloHysteresisPanel.SATURATION_M/intAxisLength, sdpYaxis.recordToM(floatHApplied), 0.001f);
//		Along Z axis, net M should be MonteCarloHysteresisPanel.floatDipoleM
		assertEquals(MonteCarloHysteresisPanel.SATURATION_M, sdpZaxis.recordToM(floatHApplied), 0.001f);

	
	}

	
}
