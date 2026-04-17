package com.example.tsumaps.core.algorithms.cluster

import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Place
import kotlin.math.abs

class ManhattanMetric: DistanceMetric {
    override val name = "Матхэттен"

    override fun distance(a: Place, b: Place): Double {
        val (ax, ay) = MapConstants.latLonToGrid(a.lat, a.lon)
        val (bx, by) = MapConstants.latLonToGrid(b.lat, b.lon)
        return (abs(ax- bx) + abs(ay - by)).toDouble()
    }
}