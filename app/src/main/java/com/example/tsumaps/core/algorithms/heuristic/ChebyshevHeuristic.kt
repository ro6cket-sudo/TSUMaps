package com.example.tsumaps.core.algorithms.heuristic

import kotlin.math.abs
import kotlin.math.max
import com.example.tsumaps.core.Point

class ChebyshevHeuristic : Heuristic {
    override fun calc(from: Point, to: Point): Int {
        val dx = abs(to.x - from.x)
        val dy = abs(to.y - from.y)

        return 10 * max(dx, dy);
    }
}