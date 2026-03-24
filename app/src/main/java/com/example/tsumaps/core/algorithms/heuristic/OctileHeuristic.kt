package com.example.tsumaps.core.algorithms.heuristic

class OctileHeuristic : Heuristic {
    override fun calc(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val dx = Math.abs(x2 - x1)
        val dy = Math.abs(y2 - y1)

        return 10 * (dx + dy) - 6 * minOf(dx, dy);
    }
}