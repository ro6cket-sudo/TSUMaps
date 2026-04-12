package com.example.tsumaps.core.DigitRecognizer

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

class TrainableDigitNeuralNetwork(context: Context) : DigitNeuralNetwork(context){

    init {
        if (w1[0][0] == 0f && w2[0][0] == 0f){
            randomizeWeights()
        }
    }

    private fun randomizeWeights(){
        for (i in 0 until INPUT_SIZE){
            for (j in 0 until HIDDEN_SIZE){
                w1[i][j] = Random.Default.nextFloat() * 0.2f - 0.1f
            }
        }

        for (i in 0 until HIDDEN_SIZE){
            for (j in 0 until OUTPUT_SIZE){
                w2[i][j] = Random.Default.nextFloat() * 0.2f - 0.1f
            }
        }
    }

    private fun trainSingleImage(grid: List<List<Int>>, correctNumber: Int, learningSpeed: Float){
        val input = FloatArray(INPUT_SIZE)
        for (i in 0 until 50){
            for ( j in 0 until 50){
                input[j * 50 + i] = grid[i][j].toFloat()
            }
        }

        val targetAnswer = FloatArray(OUTPUT_SIZE)
        targetAnswer[correctNumber] = 1f

        val (hidden, output) = NeuralNetwork(input)

        val outputError = FloatArray(OUTPUT_SIZE)
        for (i in 0 until OUTPUT_SIZE){
            outputError[i] = output[i] - targetAnswer[i]
        }

        val hiddenError = FloatArray(HIDDEN_SIZE)
        for (i in 0 until HIDDEN_SIZE){
            var errorSum = 0f
            for (j in 0 until OUTPUT_SIZE){
                errorSum += outputError[j] * w2[i][j]
            }
            hiddenError[i] = errorSum * reluDifferential(hidden[i])
        }

        for (i in 0 until OUTPUT_SIZE){
            for (j in 0 until HIDDEN_SIZE){
                w2[j][i] -= learningSpeed * outputError[j] * hidden[j]
            }
            b2[i] -= learningSpeed * outputError[i]
        }

        for (i in 0 until HIDDEN_SIZE){
            for (j in 0 until INPUT_SIZE) {
                w1[j][i] -= learningSpeed * hiddenError[j] * input[j]
            }
            b2[i] -= learningSpeed * hiddenError[i]
        }
    }

    public fun trainEpoch(dataset: List<Pair<List<List<Int>>, Int>>, epochs: Int){
        for (epoch in 1..epochs){
            val shuffledDataset = dataset.shuffled()
            var correctAnswers = 0
            for ((grid, number) in shuffledDataset){
                if (claccify(grid) == number){
                    correctAnswers++
                }
                trainSingleImage(grid, number, 0.01f)
            }
        }
        saveWeightsToJson()
    }

    private fun saveWeightsToJson() {
        val json = JSONObject()
        val w1json = JSONArray()
        for (i in 0 until INPUT_SIZE){
            val row = JSONArray()
            for (j in 0 until HIDDEN_SIZE){
                row.put(w1[i][j])
            }
            w1json.put(row)
        }
        json.put("w1", w1json)

        val b1json = JSONArray()
        for (i in 0 until HIDDEN_SIZE){
            b1json.put(b1[i])
        }
        json.put("b1", b1json)

        val w2json = JSONArray()
        for (i in 0 until HIDDEN_SIZE){
            val row = JSONArray()
            for (j in 0 until OUTPUT_SIZE){
                row.put(w2[i][j])
            }
            w2json.put(row)
        }
        json.put("w2", w2json)

        val b2json = JSONArray()
        for (i in 0 until OUTPUT_SIZE){
            b2json.put(b2[i])
        }
        json.put("b2", b2json)

        File("weights.json").writeText(json.toString())
    }
}