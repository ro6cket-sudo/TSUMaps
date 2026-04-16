package com.example.tsumaps.core.algorithms.genetic.selections

import kotlin.random.Random

class ProbTournamentSelection (
    private val winProb: Float = 0.75f
) : Selection {
    override fun select(
        population: Array<IntArray>,
        tournamentSize: Int,
        fitness: IntArray,
        rng: Random
    ): IntArray {
        val candidates = IntArray(tournamentSize) { rng.nextInt(population.size) }

        val sorted = candidates.sortedBy { fitness[it] }

        for (k in 0 until tournamentSize - 1) {
            if (rng.nextFloat() < winProb) return population[sorted[k]]
        }
        return population[sorted[tournamentSize - 1]]
    }
}