package com.example.tsumaps.core.DigitRecognizer

import android.content.Context
import android.util.Log
import androidx.annotation.experimental.Experimental
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun coach(context: Context) {
    withContext(Dispatchers.Default) {
        try {
//            val original = loadMnistUbyte(5000, context)
//            val scaled = mutableListOf<Pair<List<List<Int>>, Int>>()
//            for (item in original) {
//                val image = item.first
//                val label = item.second
//                val scaledImage = scale(image, 50, 50)
//                scaled.add(Pair(scaledImage, label))
//            }
//            val neuralNet = TrainableDigitNeuralNetwork(context)
//            neuralNet.trainEpoch(scaled, 15)

            val neuralNet = TrainableDigitNeuralNetwork(context)
            neuralNet.trainOnFullMnist(context, epochs = 5)
            Log.d("AI", "Обучение Завершено!")
        } catch (e: Exception) {
            Log.e("AI", "Ошибка во время обучения: ${e.toString()}", e)
        }
    }
}