package com.example.tsumaps.core.DigitRecognizer

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.File
import kotlin.random.Random

class TrainableDigitNeuralNetwork(private val context: Context) : DigitNeuralNetwork(context){

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

    private fun shiftImage(grid: List<List<Int>>): List<List<Int>>{
        val shiftX = Random.nextInt(-3, 4)
        val shiftY = Random.nextInt(-3, 4)
        val newGrid = MutableList(50) { MutableList(50) {0} }

        for (y in 0 until 50){
            for (x in 0 until 50){
                val oldX = x - shiftX
                val oldY = y - shiftY
                if (oldX in 0 until 50 && oldY in 0 until 50){
                    newGrid[y][x] = grid[oldY][oldX]
                }
            }
        }
        return newGrid
    }

    private fun trainSingleImage(grid: List<List<Int>>, correctNumber: Int, learningSpeed: Float){
        val L2 = 0.0001F

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

        for (out in 0 until OUTPUT_SIZE){
            for (hid in 0 until HIDDEN_SIZE) {
                val gradient = outputError[out] * hidden[hid] + (L2 * w2[hid][out])
                w2[hid][out] -= learningSpeed * gradient
            }
            b2[out] -= learningSpeed * outputError[out]
        }

        for (hid in 0 until HIDDEN_SIZE){
            for (inId in 0 until INPUT_SIZE){
                val gradient = hiddenError[hid] * input[inId] + (L2 * w1[inId][hid])
                w1[inId][hid] -= learningSpeed * gradient
            }
            b1[hid] -= learningSpeed * hiddenError[hid]
        }
    }

    fun trainEpoch(dataset: List<Pair<List<List<Int>>, Int>>, epochs: Int){
        Log.d("AI", "ОБУЧЕНИЕ НАЧАЛОСЬ! Всего картинок: ${dataset.size}, Эпох: $epochs")

        for (epoch in 1..epochs){
            val shuffledDataset = dataset.shuffled()
            var correctAnswers = 0
            for ((grid, number) in shuffledDataset){
                if (claccify(grid) == number){
                    correctAnswers++
                }
                val shiftGrid = shiftImage(grid)
                trainSingleImage(shiftGrid, number, 0.01f)
            }
            val accuracy = (correctAnswers.toFloat() / dataset.size) * 100
            Log.d("AI", "Эпоха $epoch завершена. Точность: $accuracy")
        }
        Log.d("AI", "Сохраняем в json...")
        saveWeightsToJson()
    }

    fun trainOnFullMnist(context: Context, epochs: Int) {
        Log.d("AI", "Начинаем обучение на 60000 картинках")

        for (epoch in 1..epochs){
            var correctAnswers = 0
            var totalProcessed = 0

            val labelsStream = DataInputStream(context.assets.open("train-labels.idx1-ubyte"))
            val imagesStream = DataInputStream(context.assets.open("train-images.idx3-ubyte"))

            val magicLabels = labelsStream.readInt()
            val numberLabels = labelsStream.readInt()

            val magicImages = imagesStream.readInt()
            val numberImages = imagesStream.readInt()
            val rows = imagesStream.readInt()
            val columns = imagesStream.readInt()

            for (i in 0 until numberImages){
                val label = labelsStream.readUnsignedByte()
                val image = mutableListOf<List<Int>>()
                for (row in 0 until rows){
                    val rowPixels = mutableListOf<Int>()
                    for (column in 0 until columns){
                        rowPixels.add(imagesStream.readUnsignedByte())
                    }
                    image.add(rowPixels)
                }

                val newImage = scale(image, 50, 50)
                if (claccify(newImage) == label){
                    correctAnswers++
                }
                trainSingleImage(shiftImage(newImage), label, 0.01f)
                totalProcessed++

                if (totalProcessed % 5000 == 0){
                    val tempAccuracy = (correctAnswers.toFloat() / totalProcessed) * 100
                    Log.d("AI", "Эпоха $epoch: обработано $totalProcessed/60000... " +
                            "Точность: $tempAccuracy")
                }
            }
            labelsStream.close()
            imagesStream.close()

            val accuracy = (correctAnswers.toFloat() / numberImages) * 100
            Log.d("AI", "Эпоха $epoch завершена! Итоговая точность: $accuracy")
            saveWeightsToJson()
        }
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

        try{
            val file = File(context.filesDir, "weights(python).json")
            file.writeText(json.toString())
            android.util.Log.d("AI", "Файл успешно сохранен по пути:${file.absolutePath}")
        }catch (e: Exception){
            e.printStackTrace()
            android.util.Log.d("AI", "Ошибка при сохранение файла", e)
        }
    }
}