package com.example.tsumaps.core.algorithms.genetic.mutation

import kotlin.random.Random

class InversionMutation : Mutation {
    override fun mutate(chromosome: IntArray, rng: Random) {
        var i = rng.nextInt(chromosome.size)
        var j = rng.nextInt(chromosome.size)

        if (i > j) { val tmp = i; i = j; j = tmp }

        while (i < j) {
            val tmp = chromosome[i]
            chromosome[i] = chromosome[j]
            chromosome[j] = tmp
            i++
            j--
        }
    }
}