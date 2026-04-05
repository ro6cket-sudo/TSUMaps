package com.example.tsumaps.ui.screens.scoreScreec

import androidx.compose.runtime.Composable


fun Draw(
    x: Float,
    y: Float,
    gridSize: Int,
    grid: MutableList<MutableList<Int>>,
    canvasSize: Int
){
    val cellSize = canvasSize.toFloat() / gridSize

    for (i in -2..2){
        for (j in -2..2){
            val newX = (x / cellSize).toInt()
            val newY = (y / cellSize).toInt()
            if (newX in 0 until gridSize && newY in 0 until gridSize){
                grid[newX][newY] =1
            }
        }
    }
}