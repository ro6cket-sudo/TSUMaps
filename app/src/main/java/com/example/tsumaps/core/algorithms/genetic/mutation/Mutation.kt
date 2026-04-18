package com.example.tsumaps.core.algorithms.genetic.mutation

import kotlin.random.Random

// 9 in https://www.geeksforgeeks.org/dsa/genetic-algorithms/
interface Mutation {
    fun mutate(chromosome: IntArray, rng: Random)
}