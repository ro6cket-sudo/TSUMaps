package com.example.tsumaps.core.decision_tree

import kotlin.math.log2

class LunchDecisionTree(
    private var metricType: MetricType,
    private var maxDepth: Int = 4
) {
    var root: TreeNode? = null
    private val targetColumn = "recommended_place"

    var featureNames: List<String> = emptyList()

    fun parseCsv(cscText: String): List<DataRow> {
        val lines = cscText.trim().lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) throw IllegalArgumentException("CSV пуст.")

        val headers = lines.first().split(",").map { it.trim() }
        val dataset = mutableListOf<DataRow>()

        featureNames = headers.filter { it != targetColumn }



        for (i in 1 until lines.size) {
            val values = lines[i].split(",").map { it.trim() }
            if (values.size < headers.size) continue
            val features = mutableMapOf<String, String>()
            var target = ""

            for (j in headers.indices) {
                if (headers[j] == targetColumn) {
                    target = values[j].trim()
                } else {
                    features[headers[j]] = values[j].trim()
                }
            }
            dataset.add(DataRow(features, target))
        }
        return dataset
    }

    private fun calculateGini(data: List<DataRow>): Float {
        if (data.isEmpty()) {
            return 0.0F
        }
        val counts = data.groupingBy { it.target }.eachCount()
        var gini = 1.0F
        for (count in counts.values) {
            val prob = count.toFloat() / data.size
            gini -= prob * prob
        }
        return gini
    }

    private fun calculateEntropy(data: List<DataRow>): Float {
        if (data.isEmpty()) {
            return 0.0F
        }
        val counts = data.groupingBy { it.target }.eachCount()
        var entropy = 0.0f
        for (count in counts.values) {
            val prob = count.toFloat() / data.size
            if (prob > 0) {
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

    fun train(data: List<DataRow>, depth: Int, metric: MetricType) {
        this.maxDepth = depth
        this.metricType = metric
        val initialFeatures = featureNames.toSet()
        root = buildTree(data, 0, initialFeatures)
    }

    private fun buildTree(
        data: List<DataRow>,
        depth: Int,
        availableFeatures: Set<String>
    ): TreeNode {
        val majorityClass =
            data.groupingBy { it.target }.eachCount().maxByOrNull { it.value }?.key ?: ""

        val currentImpurity = calculateImpurity(data)

        if (data.isEmpty() || data.map { it.target }.distinct().size == 1
            || depth >= maxDepth || currentImpurity == 0.0f
        ) {
            return TreeNode(isLeaf = true, prediction = majorityClass)
        }

        var bestFeature = ""
        var bestImpurity = Float.MAX_VALUE

        for (feature in availableFeatures) {
            val impurity = calculateSplitImpurity(data, feature)
            if (impurity < bestImpurity) {
                bestImpurity = impurity
                bestFeature = feature
            }
        }

        val infGain = currentImpurity - bestImpurity
        if (infGain <= 0.001f) {
            return TreeNode(isLeaf = true, prediction = majorityClass)
        }

        val node = TreeNode(feature = bestFeature, planB = majorityClass)
        val branches = data.groupBy { it.features[bestFeature]!! }

        val remainingFeatures = availableFeatures - bestFeature

        for ((value, subset) in branches) {
            node.children[value] = buildTree(subset, depth + 1, remainingFeatures)
        }
        return node
    }

    private fun calculateSplitImpurity(data: List<DataRow>, feature: String): Float {
        if (data.isEmpty()) {
            return 0.0F
        }
        var splitImpurity = 0.0f
        val group = data.groupBy { it.features[feature] }
        for (subset in group.values) {
            val normalize = subset.size.toFloat() / data.size
            splitImpurity += normalize * calculateImpurity(subset)
        }
        return splitImpurity
    }

    fun predict(input: Map<String, String>): Pair<String, List<String>> {
        val path = mutableListOf<String>()
        var currentNode = root ?: return Pair("Нет данных", path)

        while (!currentNode.isLeaf) {
            val feature = currentNode.feature!!
            val value = input[feature] ?: "unknow"
            path.add("$feature: $value")

            val nextNode = currentNode.children[value]
            if (nextNode == null) {
                path.add("Неизвестное значение $value, идем по Plan B (большинство)")
                return Pair(currentNode.planB, path)
            }
            currentNode = nextNode
        }
        return Pair(currentNode.prediction ?: "Ошибка", path)
    }

    private fun pruning(node: TreeNode) {
        if (node.isLeaf) {
            return
        }

        for (child in node.children.values) {
            pruning(child)
        }

        if (node.children.values.isNotEmpty() && node.children.values.all { it.isLeaf }) {
            val firstPrediction = node.children.values.first().prediction
            if (node.children.values.all { it.prediction == firstPrediction }) {
                node.isLeaf = true
                node.prediction = firstPrediction
                node.children.clear()
                node.feature = null
            }
        }
    }

    fun optimize() {
        root?.let { pruning(it) }
    }
}