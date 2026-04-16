package com.example.tsumaps.core.algorithms.genetic.selections

import kotlin.random.Random

class LinearRankSelection : Selection {
    override fun select(
        population: Array<IntArray>,
        tournamentSize: Int,
        fitness: IntArray,
        rng: Random
    ): IntArray {
        val n = population.size

        val sorted = (0 until n).sortedBy { fitness[it] }

        val totalWeight = n * (n + 1) / 2
        var pick = rng.nextInt(totalWeight)

        for (k in 0 until n) {
            pick -= (n - k)
            if (pick < 0) return population[sorted[k]]
        }
        return population[sorted[n-1]]
    }
}