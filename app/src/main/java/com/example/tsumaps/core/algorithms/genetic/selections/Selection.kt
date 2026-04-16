package com.example.tsumaps.core.algorithms.genetic.selections

import kotlin.random.Random

interface Selection {
    fun select(
        population: Array<IntArray>,
        tournamentSize: Int,
        fitness: IntArray,
        rng: Random
    ) : IntArray
}