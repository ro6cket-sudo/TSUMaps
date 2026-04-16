package com.example.tsumaps.core.algorithms.cluster

import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Place
import kotlin.random.Random

data class ClusteredPlace(
    val place: Place,
    val clusterIndex: Int
)

object Clustering {
    fun kMeans(places: List<Place>, k: Int): List<ClusteredPlace> {
        if (places.isEmpty()) return emptyList()
        val actualK = k.coerceAtMost(places.size)
        val maxIterations = 100

        val points = places.map { place ->
            val (gx, gy) = MapConstants.latLonToGrid(place.lat, place.lon)
            doubleArrayOf(gx.toDouble(), gy.toDouble())
        }

        val shuffled = points.indices.shuffled(Random(42))
        val centroids = Array(actualK) {i -> points[shuffled[i]].copyOf()}
        val assignments = IntArray(points.size)

        repeat(maxIterations) {
            var changed = false
            for (i in points.indices) {
                val nearest = findNearest(points[i],centroids)
                if (assignments[i] != nearest) {
                    assignments[i] = nearest
                    changed = true
                }
            }

            if (!changed) return buildResult(places, assignments)

            for (c in 0 until actualK) {
                var sumX= 0.0
                var sumY = 0.0
                var count = 0
                for (i in points.indices) {
                    if (assignments[i] == c) {
                        sumX += points[i][0]
                        sumY += points[i][1]
                        count++
                    }
                }
                if (count > 0) {
                    centroids[c][0] = sumX / count
                    centroids[c][1] = sumY / count
                }
            }
        }
        return buildResult(places,assignments)
    }

    private fun findNearest(point: DoubleArray, centroids: Array<DoubleArray>): Int {
        var bestIdx = 0
        var bestDist = Double.MAX_VALUE
        for (i in centroids.indices) {
            val dx = point[0] - centroids[i][0]
            val dy = point[1] - centroids[i][1]
            val dist = dx * dx + dy * dy
            if (dist < bestDist) {
                bestDist = dist
                bestIdx = i
            }
        }
        return bestIdx
    }

    private fun buildResult(places: List<Place>, assignments: IntArray): List<ClusteredPlace> {
        return places.mapIndexed { i, place ->
            ClusteredPlace(place, assignments[i])
        }
    }
}