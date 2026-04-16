package com.example.tsumaps.core

object MapConstants {
    const val GRID_WIDTH = 452
    const val GRID_HEIGHT= 469

    const val BOTTOM_RIGHT_LAT = 56.46397962115171
    const val BOTTOM_RIGHT_LON = 84.95782628709338
    const val TOP_LEFT_LAT = 56.47373331148776
    const val TOP_LEFT_LON = 84.94064127427714

    fun latLonToGrid(lat: Double, lon: Double): Pair<Int, Int> {
        val gridX = ((lon - TOP_LEFT_LON) / (BOTTOM_RIGHT_LON - TOP_LEFT_LON) * GRID_WIDTH)
            .toInt().coerceIn(0, GRID_WIDTH - 1)
        val gridY = ((lat - TOP_LEFT_LAT) / (BOTTOM_RIGHT_LAT - TOP_LEFT_LAT) * GRID_HEIGHT)
            .toInt().coerceIn(0, GRID_HEIGHT - 1)
        return Pair(gridX, gridY)
    }
}