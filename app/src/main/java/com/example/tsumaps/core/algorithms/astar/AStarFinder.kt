package com.example.tsumaps.core.algorithms.astar

import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.astar.heuristic.Heuristic
import com.example.tsumaps.core.algorithms.astar.heuristic.OctileHeuristic
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.PriorityQueue


class AStarFinder (
    private val width: Int = MapConstants.GRID_WIDTH,
    private val height: Int = MapConstants.GRID_HEIGHT,
    private val size: Int = width * height,
    private val heuristic: Heuristic = OctileHeuristic()
) : PathFinder {
    private var baseGrid = BooleanArray(size) { true }
    private val walkableGrid = BooleanArray(size) { true }
    private val gCost = IntArray(size) { Int.MAX_VALUE }
    private val parents = IntArray(size) { -1 }
    private val nodeState = IntArray(size)
    private var iteration = 1

    @Volatile
    private var iterSkip: Int = 50

    override fun setBaseMap(walkableMap: BooleanArray) {
        this.baseGrid = walkableMap
        System.arraycopy(baseGrid, 0, walkableGrid, 0, baseGrid.size)
    }

    override fun setObstacles(obstacles: List<Point>) {
        obstacles.forEach { point ->
            if (isValid(point)) walkableGrid[point.y * width + point.x] = false
        }
    }

    override fun clearDynamicObstacles() {
        System.arraycopy(baseGrid, 0, walkableGrid, 0, baseGrid.size)
    }

    override fun findPath(start: Point, end: Point): List<Point>? {
        if (!isValid(start) || !isValid(end)) return null

        val startIdx = start.y * width + start.x
        val endIdx = end.y * width + end.x

        if (!walkableGrid[startIdx] || !walkableGrid[endIdx]) return null

        iteration += 2

        val openSet = PriorityQueue<Int> { a, b ->
            val fA = gCost[a] + heuristic.calc(Point.of(a % width, a / width), end)
            val fB = gCost[b] + heuristic.calc(Point.of(b % width, b / width), end)
            fA.compareTo(fB)
        }

        gCost[startIdx] = 0
        parents[startIdx] = -1
        openSet.add(startIdx)
        nodeState[startIdx] = iteration

        while (openSet.isNotEmpty()) {
            val currIdx = openSet.poll()!!

            if (nodeState[currIdx] == iteration + 1) continue

            if (currIdx == endIdx) return retracePath(startIdx, endIdx)

            nodeState[currIdx] = iteration + 1

            val cx = currIdx % width
            val cy = currIdx / width

            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue

                    val nx = cx + dx
                    val ny = cy + dy
                    val nIdx = ny * width + nx

                    if (!isValid(nx, ny) || !walkableGrid[nIdx] || nodeState[nIdx] == iteration + 1) continue

                    val stepCost = if (dx != 0 && dy != 0) 14 else 10
                    val newGCost = gCost[currIdx] + stepCost

                    if (nodeState[nIdx] != iteration || newGCost < gCost[nIdx]) {
                        parents[nIdx] = currIdx
                        gCost[nIdx] = newGCost
                        nodeState[nIdx] = iteration

                        openSet.add(nIdx)
                    }
                }
            }
        }
        return null
    }

    override fun findPathAnimated(start: Point, end: Point, delayMs: Long) : Flow<PathfindingEvent> = flow {
        if (!isValid(start) || !isValid(end)) {
            emit(PathfindingEvent.PathFound(null))
            return@flow
        }

        val startIdx = start.y * width + start.x
        val endIdx = end.y * width + end.x

        if (!walkableGrid[startIdx] || !walkableGrid[endIdx]) {
            emit(PathfindingEvent.PathFound(null))
            return@flow
        }

        iteration += 2
        var iterCount = 0

        val openSet = PriorityQueue<Int> { a, b ->
            val fA = gCost[a] + heuristic.calc(Point.of(a % width, a / width), end)
            val fB = gCost[b] + heuristic.calc(Point.of(b % width, b / width), end)
            fA.compareTo(fB)
        }

        gCost[startIdx] = 0
        parents[startIdx] = -1
        openSet.add(startIdx)
        nodeState[startIdx] = iteration

        emit(PathfindingEvent.NodesOpened(listOf(start)))

        while (openSet.isNotEmpty()) {
            val currIdx = openSet.poll()!!

            if (nodeState[currIdx] == iteration + 1) continue

            val cx = currIdx % width
            val cy = currIdx / width

            emit(PathfindingEvent.NodeClosed(Point.of(cx, cy)))

            if (currIdx == endIdx) {
                emit(PathfindingEvent.PathFound(retracePath(startIdx, endIdx)))
                return@flow
            }

            nodeState[currIdx] = iteration + 1

            val newOpened = mutableListOf<Point>()

            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue

                    val nx = cx + dx
                    val ny = cy + dy
                    val nIdx = ny * width + nx

                    if (!isValid(
                            nx,
                            ny
                        ) || !walkableGrid[nIdx] || nodeState[nIdx] == iteration + 1
                    ) continue

                    val stepCost = if (dx != 0 && dy != 0) 14 else 10
                    val newGCost = gCost[currIdx] + stepCost

                    if (nodeState[nIdx] != iteration || newGCost < gCost[nIdx]) {
                        val isNew = nodeState[nIdx] != iteration

                        parents[nIdx] = currIdx
                        gCost[nIdx] = newGCost
                        nodeState[nIdx] = iteration
                        openSet.add(nIdx)

                        if (isNew) {
                            newOpened.add(Point.of(nx, ny))
                        }
                    }
                }
            }
            if (newOpened.isNotEmpty()) {
                emit(PathfindingEvent.NodesOpened(newOpened))
            }

            if (iterCount++ % iterSkip == 0)
                delay(delayMs)
        }
        emit(PathfindingEvent.PathFound(null))
    }

    private fun isValid(x: Int, y: Int) = x in 0 until width && y in 0 until height
    private fun isValid(point: Point) = point.x in 0 until width && point.y in 0 until height

    private fun retracePath(startIdx: Int, endIdx: Int): List<Point> {
        var length = 0
        var counter: Int = endIdx
        while (counter != -1) {
            length++
            if (counter == startIdx) break
            counter = parents[counter]
        }

        val path = ArrayList<Point>(length)
        var current: Int = endIdx

        while (current != -1) {
            path.add(Point.of(current % width, current / width))
            if (current == startIdx) break
            current = parents[current]
        }
        path.reverse()
        return path
    }

    override fun setAnimationSkip(skip: Int) {
        this.iterSkip = skip.coerceAtLeast(1) * 5
    }
}