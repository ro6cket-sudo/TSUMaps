package com.example.tsumaps.core.algorithms.genetic.selections

import kotlin.random.Random

class TournamentSelection : Selection {
    override fun select(
        population: Array<IntArray>,
        tournamentSize: Int,
        fitness: IntArray,
        rng: Random
    ): IntArray {
        var bestIdx = rng.nextInt(population.size)
        repeat(tournamentSize - 1) {
            val candidate = rng.nextInt(population.size)
            if (fitness[candidate] < fitness[bestIdx]) bestIdx = candidate
        }
        return population[bestIdx]
    }
}