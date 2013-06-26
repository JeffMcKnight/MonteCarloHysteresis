/**
 * 
 */
package com.jeffmcknight.magneticmontecarlo;

/**
 * @author jeffmcknight
 *
 */
//--------------------------------
class RecordPoint
{
float m;
float h;

//******************** constructor - RecordPoint() ********************
RecordPoint()
{
	h=0;
	m=0;
}

RecordPoint(float initH)
{
	h = initH;
	m = 0;
}

//******************** getM() ********************
public float getM() 
{
	return m;
}

//******************** setM() ********************
public void setM(float paramM) 
{
	this.m = paramM;
}

//******************** getH() ********************
public float getH() 
{
	return h;
}

//******************** setH() ********************
public void setH(float paramH) 
{
	this.h = paramH;
}

} //END ******************** constructor - RecordPoint() ********************

