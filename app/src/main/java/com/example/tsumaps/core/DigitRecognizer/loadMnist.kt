package com.example.tsumaps.core.DigitRecognizer

import android.content.Context
import java.io.DataInputStream

public fun loadMnistUbyte(maxImages: Int, context: Context): List<Pair<List<List<Int>>, Int>> {
    val dataset = mutableListOf<Pair<List<List<Int>>, Int>>()
    val labelsStream = DataInputStream(context.assets.open("train-labels.idx1-ubyte"))
    val imagesStream = DataInputStream(context.assets.open("train-images.idx3-ubyte"))

    val magicLabels = labelsStream.readInt()
    val numberLabels = labelsStream.readInt()

    val magicImages = imagesStream.readInt()
    val numberImages = imagesStream.readInt()
    val rows = imagesStream.readInt()
    val columns = imagesStream.readInt()

    val countToRead = if (maxImages < numberImages) maxImages else numberImages
    for (i in 0 until countToRead) {
        val label = labelsStream.readUnsignedByte()
        val image = mutableListOf<List<Int>>()
        for (row in 0 until rows) {
            val rowPixels = mutableListOf<Int>()
            for (column in 0 until columns) {
                rowPixels.add(imagesStream.readUnsignedByte())
            }
            image.add(rowPixels)
        }
        dataset.add(Pair(image, label))
    }
    labelsStream.close()
    imagesStream.close()

    return dataset
}