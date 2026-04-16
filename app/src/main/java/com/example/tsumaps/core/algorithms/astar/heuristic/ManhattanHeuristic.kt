package com.example.tsumaps.core.algorithms.astar.heuristic

import kotlin.math.abs
import com.example.tsumaps.core.Point

class ManhattanHeuristic : Heuristic {
    override fun calc(from: Point, to: Point): Int {
        val dx = abs(to.x - from.x)
        val dy = abs(to.y - from.y)

        return dx + dy;
    }
}