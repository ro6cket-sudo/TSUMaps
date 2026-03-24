package com.example.tsumaps.core.algorithms.heuristic

interface Heuristic {
    fun calc(x1: Int, y1: Int, x2: Int, y2: Int): Int
}