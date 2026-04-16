package com.example.tsumaps.core.decision_tree

import kotlin.math.log2

class LunchDecisionTree(
    private val metricType: MetricType,
    private val maxDepth: Int = 4
) {
    var root: TreeNode? = null
    private val targetColumn = "recommended_place"

    fun parceCsv(cscText: String): List<DataRow> {
        val lines = cscText.trim().lines().filter {it.isNotBlank()}
        val headers = lines.first().split(",").map {it.trim()}
        val dataset = mutableListOf<DataRow>()

        for (i in 1 until lines.size){
            val values = lines[i].split(",").map {it.trim()}
            val features = mutableMapOf<String, String>()
            var target = ""

            for (j in headers.indices){
                if (headers[j] == targetColumn){
                    target = values[j]
                }
                else{
                    features[headers[j]] = values[i]
                }
            }
            dataset.add(DataRow(features, target))
        }
        return dataset
    }

    private fun calculateGini(data: List<DataRow>): Float {
        if (data.isEmpty()){
            return 0.0F
        }
        val counts = data.groupingBy { it.target }.eachCount()
        var gini = 0.5F
        for (count in counts.values){
            val prob = count.toFloat() / data.size
            gini -= prob * prob
        }
        return gini
    }

    private fun calculateEntropy(data: List<DataRow>): Float {
        if (data.isEmpty()){
            return 0.0F
        }
        val counts = data.groupingBy { it.target }.eachCount()
        var entropy = 0.0f
        for (count in counts.values){
            val prob = count.toFloat() / data.size
            if (prob > 0 ){
                entropy -= prob * log2(prob)
            }
        }
        return entropy
    }

    private fun calculateImpurity(data: List<DataRow>): Float {
        if (metricType == MetricType.GINI) return calculateGini(data)
        if (metricType == MetricType.ENTROPY) return calculateEntropy(data)
        return (calculateGini(data) * 2 + calculateEntropy(data)) / 2.0F
    }

    private fun calculateSplitImpurity(data: List<DataRow>, feature: String): Float {
        if (data.isEmpty()){
            return 0.0F
        }
        var splitImpurity = 0.0f
        val group = data.groupBy { it.features[feature] }
        for (subset in group.values){
            val normalize = subset.size.toFloat() / data.size
            splitImpurity += normalize * calculateImpurity(subset)
        }
        return splitImpurity
    }
}