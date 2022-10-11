package com.jeffmcknight.magneticmontecarlo.ui

import info.monitorenter.gui.chart.ITracePoint2D
import info.monitorenter.gui.chart.traces.Trace2DReplacing
import info.monitorenter.gui.chart.traces.Trace2DSimple

/**
 * Similar to [Trace2DReplacing], except it updates a [ITracePoint2D]s instead of replacing them
 */
class Trace2DUpdating : Trace2DSimple() {
    /**
     * Add [p] if no other [ITracePoint2D] with the same [ITracePoint2D.getX] exists in [m_points];
     * if a matching [ITracePoint2D] does exist, we keep that [ITracePoint2D] and update its
     * [ITracePoint2D.getY]
     * @param p the new [ITracePoint2D] to add or update
     */
    override fun addPointInternal(p: ITracePoint2D): Boolean {
        val index = m_points.indexOfFirst { it.x == p.x }
        return if (index != -1) {
            val old = m_points[index]
            old.setLocation(old.x, p.y)
            false
        } else {
            m_points.add(p)
            true
        }
    }
}
