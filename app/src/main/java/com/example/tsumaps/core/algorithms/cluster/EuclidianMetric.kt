package com.example.tsumaps.core.algorithms.cluster

import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Place
import kotlin.math.sqrt


class EuclideanMetric: DistanceMetric {
    override val name = "По прямой"

    override fun distance(a: Place, b: Place): Double {
        val (ax, ay) = MapConstants.latLonToGrid(a.lat, a.lon)
        val (bx, by) = MapConstants.latLonToGrid(b.lat, b.lon)
        val dx = (ax-bx).toDouble()
        val dy = (ay - by).toDouble()
        return sqrt(dx*dx + dy*dy)
    }
}