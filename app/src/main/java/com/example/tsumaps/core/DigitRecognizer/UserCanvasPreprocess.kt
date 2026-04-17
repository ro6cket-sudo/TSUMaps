package com.example.tsumaps.core.DigitRecognizer

fun preprocessCanvas(grid: List<List<Int>>): List<List<Int>> {
    val Y = grid.size
    val X = grid[0].size
    var minX = X
    var maxX = 0
    var minY = Y
    var maxY = 0
    var hasPixels = false

    for (y in 0 until Y) {
        for (x in 0 until X) {
            if (grid[y][x] > 0) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                hasPixels = true
            }
        }
    }

    if (!hasPixels) return grid

    val digitWidth = maxX - minX + 1
    val digitHeight = maxY - minY + 1

    val croppedDigit = MutableList(digitHeight) { y ->
        MutableList(digitWidth) { x -> grid[minY + y][minX + x] }
    }

    val scaledDigit = scale(croppedDigit, 40, 40)

    val finalGrid = MutableList(Y) { MutableList(50) { 0 } }
    for (y in 0 until 40){
        for (x in 0 until 40) {
            finalGrid[y + 5][x + 5] = scaledDigit[y][x]
        }
    }
    return finalGrid
}