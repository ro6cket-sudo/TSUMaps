package com.example.tsumaps.core.algorithms.genetic.crossover

import kotlin.math.min
import kotlin.random.Random

class OrderCrossover : Crossover {
    override fun cross(
        parent1: IntArray,
        parent2: IntArray,
        destination: IntArray,
        inChildVersion: IntArray,
        crossoverVersion: Int,
        rng: Random
    ) {
        val size = parent1.size

        var start = rng.nextInt(size)
        var end = rng.nextInt(size)

        if (start > end) {
            val temp = start; start = end; end = temp
        }
        if (start == end) end = min(size, end + 1)

        for (i in start until end) {
            destination[i] = parent1[i]
            inChildVersion[parent1[i]] = crossoverVersion
        }

        var pos = end % size

        for (gen in parent2) {
            if (inChildVersion[gen] != crossoverVersion) {
                destination[pos] = gen
                inChildVersion[gen] = crossoverVersion
                pos = (pos + 1) % size
            }
        }
    }
}