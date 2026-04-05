package com.example.tsumaps.core.algorithms.genetic

import com.example.tsumaps.core.FoodItem
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.Point
import kotlinx.coroutines.flow.Flow


interface GeneticOptimizer {
    fun findOptimalRoute (
        places: List<Place>,
        order: Set<FoodItem>,
        currentTime: Int,
        distanceMatrix: Array<IntArray>,
        startDistances: IntArray
    ): Flow<GeneticEvent>
}