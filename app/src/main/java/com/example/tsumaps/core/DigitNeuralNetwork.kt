package com.example.tsumaps.core

import android.content.Context
import androidx.compose.ui.graphics.Path
import org.json.JSONObject
import kotlin.math.exp
import kotlin.math.max

class DigitNeuralNetwork(context: Context) {
    private val inputSize: Int = 2500
    private val hiddenSize: Int = 128
    private val outputSize: Int = 10

    var w1 = Array(inputSize) { FloatArray(hiddenSize) }
    var b1 = FloatArray(hiddenSize)
    var w2 = Array(hiddenSize) { FloatArray(outputSize) }
    var b2 = FloatArray(outputSize)

    init {
        loadWeightsFromJson(context)
    }

    fun loadWeightsFromJson(context: Context){
        val jsonString = context.assets.open("weights.json").bufferedReader().use {it.readText()}
        val json = JSONObject(jsonString)

        val w1json = json.getJSONArray("w1")
        for (i in 0 until inputSize){
            val row = w1json.getJSONArray(i)
            for (j in 0 until hiddenSize){
                w1[i][j] = row.getDouble(j).toFloat()
            }
        }

        val b1json = json.getJSONArray("b1")
        for (i in 0 until hiddenSize){
            b1[i] = b1json.getDouble(i).toFloat()
        }

        val w2json = json.getJSONArray("w2")
        for (i in 0 until hiddenSize){
            val row = w2json.getJSONArray(i)
            for (j in 0 until outputSize){
                w2[i][j] = row.getDouble(j).toFloat()
            }
        }

        val b2json = json.getJSONArray("b2")
        for (i in 0 until outputSize){
            b2[i] = b2json.getDouble(i).toFloat()
        }
    }

    private fun relu(x: Float) : Float = max(0f, x)
    private fun reluRewrite(x: Float) = if (x > 0) 1f else 0f

    private fun softmax(input: FloatArray): FloatArray {
        val maxVal = input.maxOrNull() ?: 0f
        val e = FloatArray(input.size) {exp(input[it]) - maxVal}
        val sum = e.sum()
        return FloatArray(input.size) {e[it] / sum}
    }

    fun claccify(grid: List<List<Int>>): Int{
        val input = FloatArray(inputSize)
        for (i in 0 until 50){
            for ( j in 0 until 50){
                input[j * 50 + i] = grid[i][j].toFloat()
            }
        }
        val (_, output) = NeuralNetwork(input)
        return output.indices.maxByOrNull { output[it] } ?: -1
    }

    private fun NeuralNetwork(input: FloatArray): Pair<FloatArray, FloatArray> {
        val hidden = FloatArray(hiddenSize)
        for (j in 0 until hiddenSize){
            var sum = b1[j]
            for (i in 0 until inputSize){
                sum += input[i] * w1[i][j]
            }
            hidden[j] = relu(sum)
        }
        val outputRow = FloatArray(outputSize)
        for (j in 0 until outputSize){
            var sum = b2[j]
            for (i in  0 until hiddenSize) {
                sum += hidden[i] * w2[i][j]
            }
            outputRow[j] = sum
        }
        return Pair(hidden, softmax(outputRow))
    }
}