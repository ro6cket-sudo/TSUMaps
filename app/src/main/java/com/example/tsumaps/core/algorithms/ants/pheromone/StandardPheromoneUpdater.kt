package com.example.tsumaps.core.algorithms.ants.pheromone


class StandardPheromoneUpdater : PheromoneUpdater {
    override fun update(
        pheromone: Array<DoubleArray>,
        routes: Array<IntArray>,
        costs: IntArray,
        startIdx: Int,
        evaporationRate: Double,
        pheromoneDeposit: Double,
        bestRoute: IntArray,
        bestCost: Int
    ) {
        val size = pheromone.size
        val keep = 1.0 - evaporationRate

        for (i in 0 until size) {
            val row = pheromone[i]
            for (j in 0 until size) {
                row[j] *= keep
            }
        }

        for (a in routes.indices) {
            val cost = costs[a]
            if (cost <= 0) continue
            val amount = pheromoneDeposit / cost
            val route = routes[a]

            var prev = startIdx
            for (idx in route) {
                pheromone[prev][idx] += amount
                pheromone[idx][prev] += amount
                prev = idx
            }
            pheromone[prev][startIdx] += amount
            pheromone[startIdx][prev] += amount
        }
    }
}