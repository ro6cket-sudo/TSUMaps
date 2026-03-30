package com.example.tsumaps.core.algorithms.heuristic

import com.example.tsumaps.core.Point

interface Heuristic {
    fun calc(from: Point, to: Point): Int
}