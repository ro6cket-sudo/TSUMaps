package com.example.tsumaps.core.DigitRecognizer

fun scale(original: List<List<Int>>, newWidth: Int, newHeight: Int): List<List<Int>> {
    val oldHeight = original.size
    val oldWidth = original[0].size
    val scaled = MutableList(newHeight) { MutableList(newWidth) {0} }

    val scaleWidth = (oldWidth - 1).toFloat() / (newWidth - 1)
    val scaleHeight = (oldHeight - 1).toFloat() / (newHeight - 1)

    for (y in 0 until newHeight){
        for (x in 0 until newWidth){
            val scaledX = (x * scaleWidth)
            val scaledY = (y * scaleHeight)

            val x1 = scaledX.toInt()
            val y1 = scaledY.toInt()
            val x2 = (x1 + 1).coerceAtMost(oldWidth - 1)
            val y2 = (y1 + 1).coerceAtMost(oldHeight - 1)

            val dx = scaledX - x1
            val dy = scaledY - y1

            val newPixel = (1 - dx) * (1 - dy) * original[y1][x1] +
                    dx * (1 - dy) * original[y1][x2] +
                    (1 - dx) * dy * original[y2][x1] +
                    dx * dy * original[y2][x2]
            scaled[y][x] =  if (newPixel >= 0.5) 1 else 0
        }
    }
    return scaled
}