package com.example.tsumaps.core.algorithms.cluster

import com.example.tsumaps.core.Place
import kotlin.random.Random

object KMedoids {
    fun cluster(places: List<Place>,k: Int, metric: DistanceMetric): List<ClusteredPlace> {
        if (places.isEmpty()) return emptyList()
        val n = places.size
        val actualK = k.coerceAtMost(n)
        val dist = Array(n) { DoubleArray(n) }

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val d = metric.distance(places[i], places[j])
                dist[i][j] = d
                dist[j][i] = d
            }
        }

        val medoids = initPlusPlus(n, actualK, dist)
        val assignments = IntArray(n)

        repeat(100) {
            var changed = false

            for (i in 0 until n) {
                val nearest = medoids.indices.minByOrNull {c -> dist[i][medoids[c]]} ?: 0
                if (assignments[i] != nearest) {
                    assignments[i] = nearest
                    changed = true
                }
            }

            if (!changed) return buildResult(places, assignments)

            recoverEmptyClusters(n,actualK, medoids,assignments,dist)

            for (c in 0 until actualK) {
                val clusterIndices = (0 until n).filter {assignments[it] == c}
                if (clusterIndices.isEmpty()) continue
                val newMedoid = clusterIndices.minByOrNull { i ->
                    clusterIndices.sumOf { j -> dist[i][j] }
                } ?: continue
                medoids[c] = newMedoid
            }
        }
        return buildResult(places,assignments)
    }


    private fun recoverEmptyClusters (
        n: Int, k: Int,
        medoids: MutableList<Int>,
        assignments: IntArray,
        dist: Array<DoubleArray>
    ) {
        for (c in 0 until k) {
            if ((0 until n).any { assignments[it] == c}) continue

            val farthest = (0 until n).maxByOrNull { i -> dist[i][medoids[assignments[i]]] } ?: continue
            medoids[c] = farthest
            assignments[farthest] = c
        }
    }

    private fun buildResult(places: List<Place>, assignments: IntArray): List<ClusteredPlace> =
        places.mapIndexed { index, place -> ClusteredPlace(place,assignments[index]) }

    private fun initPlusPlus(n: Int,k: Int, dist: Array<DoubleArray>): MutableList<Int> {
        val rng = Random(42)
        val medoids = mutableListOf(rng.nextInt(n))

        repeat(k) {
            val weights = DoubleArray(n) { i ->
                if (medoids.contains(i)) 0.0
                else {
                    val nearest = medoids.minOf{ m -> dist[i][m]}
                    nearest * nearest
                }
            }
            val total = weights.sum()
            if (total == 0.0) {
                val unused = (0 until n).first { !medoids.contains(it)}
                medoids.add(unused)
                return@repeat
            }
            var r = rng.nextDouble() * total
            var chosen = 0
            for (i in weights.indices) {
                r -= weights[i]
                if (r <= 0.0) { chosen = i; break}
            }
            medoids.add(chosen)
        }
        return medoids
    }
}