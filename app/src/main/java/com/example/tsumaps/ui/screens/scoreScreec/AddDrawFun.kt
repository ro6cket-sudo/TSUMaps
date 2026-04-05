package com.example.tsumaps.ui.screens.scoreScreec

import androidx.compose.runtime.Composable
import kotlin.math.abs
import kotlin.math.max

fun addDraw(
    x0: Float,
    x1: Float,
    y0: Float,
    y1: Float,
    gridSize: Int,
    grid: MutableList<MutableList<Int>>,
    canvasSize: Int
){
    val cellSize = canvasSize.toFloat() / gridSize
    val dist = max(abs(x1 - x0), abs(y1- y0))
    val steps = (dist / (cellSize / 4)).toInt()
    for (i in 0..steps){
        val a = i.toFloat() / steps
        val x = x0 + (x1 - x0) * a
        val y = y0 + (y1 - y0) * a
        Draw(x, y, gridSize, grid, canvasSize)
    }
}