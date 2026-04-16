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
    var tree by mutableStateOf<LunchDecisionTree?>(null)
    var isOptimized by mutableStateOf(false)

    val userSelections = mutableStateMapOf<String, String>()
    var predictionResult by mutableStateOf<Pair<String, List<String>>?>(null)

    fun buildingTree(){
        val decisionTree = LunchDecisionTree(MetricType.ENTROPY)
        val data = decisionTree.parseCsv(csvText)
        decisionTree.train(data)
        if (isOptimized) decisionTree.optimize()
        tree = decisionTree
    }

    fun makePrediction(){
        predictionResult = tree?.predict(userSelections)
    }
}