package com.example.tsumaps.core.algorithms.genetic.crossover

import kotlin.math.min
import kotlin.random.Random

interface Crossover {
    fun cross(
        parent1: IntArray,
        parent2: IntArray,
        destination: IntArray,
        inChildVersion: IntArray,
        crossoverVersion: Int,
        rng: Random
    )
}