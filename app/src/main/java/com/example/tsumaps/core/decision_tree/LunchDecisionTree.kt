package com.example.tsumaps.core.decision_tree

class LunchDecisionTree(...) {
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

            for (j in headers)
        }
    }
}