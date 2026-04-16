package com.example.tsumaps.core.algorithms.genetic

import com.example.tsumaps.core.Place

sealed class GeneticEvent {
    data class NewBestRoute(
        val iteration: Int,
        val route: List<Place>,
        val totalCost: Int
    ) : GeneticEvent()

    data class EvolutionFinished(
        val finalRoute: List<Place>,
        val totalCost: Int,
        val iterations: Int
    ) : GeneticEvent()

    data object NoSolutionFound : GeneticEvent()
}