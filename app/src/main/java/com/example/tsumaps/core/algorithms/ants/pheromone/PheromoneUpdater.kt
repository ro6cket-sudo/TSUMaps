package com.example.tsumaps.core.algorithms.ants.pheromone

interface PheromoneUpdater {
    fun update(
        pheromone: Array<DoubleArray>,
        routes: Array<IntArray>,
        costs: IntArray,
        startIdx: Int,
        evaporationRate: Double,
        pheromoneDeposit: Double,
        bestRoute: IntArray,
        bestCost: Int
    )
}