package com.example.tsumaps.core.algorithms

data class Node(val x: Int, val y: Int) {
    var gCost: Int = Int.MAX_VALUE
    var hCost: Int = 0;
    var parent: Node? = null

    val fCost: Int get() = gCost + hCost
}