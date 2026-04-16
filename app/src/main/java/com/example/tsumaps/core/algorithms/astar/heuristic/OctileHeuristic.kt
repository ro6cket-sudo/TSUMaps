package com.example.tsumaps.core.algorithms.astar.heuristic

import kotlin.math.abs
import kotlin.math.min
import com.example.tsumaps.core.Point

class OctileHeuristic : Heuristic {
    override fun calc(from: Point, to: Point): Int {
        val dx = abs(to.x - from.x)
        val dy = abs(to.y - from.y)

        return 10 * (dx + dy) - 6 * min(dx, dy);
    }
}