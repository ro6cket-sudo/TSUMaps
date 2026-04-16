package com.example.tsumaps.core.algorithms.astar

import com.example.tsumaps.core.Point

sealed class PathfindingEvent {
    data class NodeClosed(val point: Point) : PathfindingEvent()
    data class NodesOpened(val points: List<Point>) : PathfindingEvent()
    data class PathFound(val path: List<Point>?) : PathfindingEvent()
}