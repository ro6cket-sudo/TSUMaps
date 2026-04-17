package com.example.tsumaps.ui.screens.scoreScreec

import androidx.compose.runtime.Composable


fun Draw(
    x: Float,
    y: Float,
    gridSize: Int,
    grid: MutableList<MutableList<Int>>,
    canvasSize: Int
) {
    if (x <= 1f || y <= 1f || x >= canvasSize - 1f || y >= canvasSize - 1f) {
        return
    }

    val cellSize = canvasSize.toFloat() / gridSize

    for (i in -2..2) {
        for (j in -2..2) {
            val newX = (x / cellSize).toInt() + i
            val newY = (y / cellSize).toInt() + j
            if (newX in 0 until gridSize && newY in 0 until gridSize) {
                grid[newX][newY] = 1
            }
        }
    }
}