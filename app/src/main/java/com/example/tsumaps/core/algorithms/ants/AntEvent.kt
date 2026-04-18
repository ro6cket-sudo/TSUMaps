package com.example.tsumaps.core.algorithms.ants

import com.example.tsumaps.core.Place

sealed class AntEvent {
    data class NewBestRoute(
        val iteration: Int,
        val route: List<Place>,
        val totalCost: Int
    ) : AntEvent()

    data class IterationRoute(
        val iteration: Int,
        val route: List<Place>,
        val cost: Int,
        val isGlobalBest: Boolean
    ) : AntEvent()

    data class OptimizationFinished(
        val finalRoute: List<Place>,
        val totalCost: Int,
        val iterations: Int
    ) : AntEvent()

    data object NoSolutionFound : AntEvent()
}