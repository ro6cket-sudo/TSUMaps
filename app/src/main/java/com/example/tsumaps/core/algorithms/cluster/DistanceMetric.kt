package com.example.tsumaps.core.algorithms.cluster

import com.example.tsumaps.core.Place

interface DistanceMetric {
    val name: String
    fun distance(a: Place, b: Place): Double
}

enum class ClusterMetricType(val label: String) {
    EUCLIDEAN("По прямой"),
    MANHATTAN("Манхэттан"),
    PEDESTRIAN("Пешеходное")
}