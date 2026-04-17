package com.example.tsumaps.ui.screens.decisionTreeScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tsumaps.core.decision_tree.LunchDecisionTree
import com.example.tsumaps.core.decision_tree.MetricType

class DecisionTreeViewModel : ViewModel() {
    var csvText by mutableStateOf("")
    var maxDepth by mutableStateOf(4f)
    var selectionMetric by mutableStateOf(MetricType.ENTROPY)
    var tree by mutableStateOf<LunchDecisionTree?>(null)
    var isOptimized by mutableStateOf(false)

    var availableFeatures by mutableStateOf<List<String>>(emptyList())
    val userSelections = mutableStateMapOf<String, String>()
    var predictionResult by mutableStateOf<Pair<String, List<String>>?>(null)
    var errorMassage by mutableStateOf<String?>(null)

    fun buildingTree(){
        try {
            errorMassage = null
            predictionResult = null
            userSelections.clear()

            val decisionTree = LunchDecisionTree(MetricType.ENTROPY)
            val data = decisionTree.parseCsv(csvText)
            decisionTree.train(data, maxDepth.toInt(), selectionMetric)
            if (isOptimized) decisionTree.optimize()
            tree = decisionTree
            availableFeatures = decisionTree.featureNames
        }catch (error: Exception){
            errorMassage = "Ошибка парсинга или построения: ${error.toString()}"
            tree = null
        }
    }

    fun makePrediction(){
        predictionResult = tree?.predict(userSelections)
    }
}