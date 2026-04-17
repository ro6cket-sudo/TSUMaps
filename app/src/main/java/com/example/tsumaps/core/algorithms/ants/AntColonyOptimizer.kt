package com.example.tsumaps.core.algorithms.ants

import com.example.tsumaps.core.Place
import kotlinx.coroutines.flow.Flow


interface AntColonyOptimizer {
    fun findOptimalRoute(
        places: List<Place>,
        distanceMatrix: Array<IntArray>,
        startDistances: IntArray
    ): Flow<AntEvent>
}