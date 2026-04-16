package com.example.tsumaps.core.algorithms.genetic.mutation

import kotlin.random.Random

class SwapMutation : Mutation {
    override fun mutate(chromosome: IntArray, rng: Random) {
        val i = rng.nextInt(chromosome.size)
        val j = rng.nextInt(chromosome.size)
        val tmp = chromosome[i]
        chromosome[i] = chromosome[j]
        chromosome[j] = tmp
    }
}