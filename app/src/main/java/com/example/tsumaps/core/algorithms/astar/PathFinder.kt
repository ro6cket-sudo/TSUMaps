package com.example.tsumaps.core.algorithms.astar

import com.example.tsumaps.core.Point
import kotlinx.coroutines.flow.Flow

interface PathFinder {
    fun setBaseMap(walkableMap: BooleanArray)
    fun setObstacles(obstacles: List<Point>)
    fun clearDynamicObstacles()
    fun findPath(start: Point, end: Point): List<Point>?
    fun findPathAnimated(start: Point, end: Point, delayMs: Long = 50L): Flow<PathfindingEvent>
    fun setAnimationSkip(skip: Int)
}