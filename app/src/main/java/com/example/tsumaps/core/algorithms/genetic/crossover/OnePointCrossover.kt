package com.example.tsumaps.core.algorithms.genetic.crossover

import kotlin.random.Random

class OnePointCrossover : Crossover {
    override fun cross(
        parent1: IntArray,
        parent2: IntArray,
        destination: IntArray,
        inChildVersion: IntArray,
        crossoverVersion: Int,
        rng: Random
    ) {
        val size = parent1.size

        val point = rng.nextInt(size)

        for (i in 0 until point) {
            destination[i] = parent1[i]
            inChildVersion[parent1[i]] = crossoverVersion
        }

        var pos = point

        for (gen in parent2) {
            if (inChildVersion[gen] != crossoverVersion) {
                destination[pos] = gen
                inChildVersion[gen] = crossoverVersion
                pos = (pos + 1) % size
            }
        }
    }
}