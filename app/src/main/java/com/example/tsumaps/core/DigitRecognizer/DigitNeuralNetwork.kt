package com.example.tsumaps.core.DigitRecognizer

import android.content.Context
import org.json.JSONObject
import kotlin.math.exp
import kotlin.math.max

open class DigitNeuralNetwork(context: Context) {
    companion object {
        protected const val INPUT_SIZE = 2500
        protected const val HIDDEN_SIZE = 128
        protected const val OUTPUT_SIZE = 10
    }

    protected var w1 = Array(INPUT_SIZE) { FloatArray(HIDDEN_SIZE) }
    protected var b1 = FloatArray(HIDDEN_SIZE)
    protected var w2 = Array(HIDDEN_SIZE) { FloatArray(OUTPUT_SIZE) }
    protected var b2 = FloatArray(OUTPUT_SIZE)

    init {
        loadWeightsFromJson(context)
    }

    fun loadWeightsFromJson(context: Context){
        val jsonString = context.assets.open("weights(python).json").bufferedReader().use {it.readText()}
        val json = JSONObject(jsonString)

        val w1json = json.getJSONArray("w1")
        for (i in 0 until INPUT_SIZE){
            val row = w1json.getJSONArray(i)
            for (j in 0 until HIDDEN_SIZE){
                w1[i][j] = row.getDouble(j).toFloat()
            }
        }

        val b1json = json.getJSONArray("b1")
        for (i in 0 until HIDDEN_SIZE){
            b1[i] = b1json.getDouble(i).toFloat()
        }

        val w2json = json.getJSONArray("w2")
        for (i in 0 until HIDDEN_SIZE){
            val row = w2json.getJSONArray(i)
            for (j in 0 until OUTPUT_SIZE){
                w2[i][j] = row.getDouble(j).toFloat()
            }
        }

        val b2json = json.getJSONArray("b2")
        for (i in 0 until OUTPUT_SIZE){
            b2[i] = b2json.getDouble(i).toFloat()
        }
    }

    private fun relu(x: Float) : Float = max(0f, x)
    protected fun reluDifferential(x: Float) = if (x > 0) 1f else 0f

    private fun softmax(input: FloatArray): FloatArray {
        val maxVal = input.maxOrNull() ?: 0f
        val e = FloatArray(input.size) { exp(input[it] - maxVal)}
        val sum = e.sum()
        return FloatArray(input.size) {e[it] / sum}
    }

    fun claccify(grid: List<List<Int>>): Int{
        val input = FloatArray(INPUT_SIZE)
        for (i in 0 until 50){
            for ( j in 0 until 50){
                input[j * 50 + i] = grid[i][j].toFloat()
            }
        }
        val (_, output) = NeuralNetwork(input)
        return output.indices.maxByOrNull { output[it] } ?: -1
    }

    protected fun NeuralNetwork(input: FloatArray): Pair<FloatArray, FloatArray> {
        val hidden = FloatArray(HIDDEN_SIZE)
        for (j in 0 until HIDDEN_SIZE){
            var sum = b1[j]
            for (i in 0 until INPUT_SIZE){
                sum += input[i] * w1[i][j]
            }
            hidden[j] = relu(sum)
        }
        val outputRow = FloatArray(OUTPUT_SIZE)
        for (j in 0 until OUTPUT_SIZE){
            var sum = b2[j]
            for (i in  0 until HIDDEN_SIZE) {
                sum += hidden[i] * w2[i][j]
            }
            outputRow[j] = sum
        }
        return Pair(hidden, softmax(outputRow))
    }
}