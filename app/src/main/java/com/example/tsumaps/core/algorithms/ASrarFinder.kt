package com.example.tsumaps.core.algorithms

import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.algorithms.heuristic.ChebyshevHeuristic
import com.example.tsumaps.core.algorithms.heuristic.Heuristic
import java.util.PriorityQueue

class ASrarFinder (
    private val width: Int = MapConstants.GRID_WIDTH,
    private val height: Int = MapConstants.GRID_HEIGHT,
    private val heuristic: Heuristic = ChebyshevHeuristic()
) {
    private val walkableGrid = Array(width) { BooleanArray(height) { true } }

    fun setObstacles(obstacleCoords: List<Pair<Int, Int>>) {
        obstacleCoords.forEach{ (x, y) ->
            if (isValid(x, y)) walkableGrid[x][y] = false
        }
    }

    fun findPath(startX: Int, startY: Int, endX: Int, endY: Int) : List<Pair<Int, Int>>? {
        if (!isValid(startX, startY) || !isValid(endX, endY)) return null
        if (!walkableGrid[startX][startY] || !walkableGrid[endX][endY]) return null

        val openSet = PriorityQueue<Node>(compareBy { it.fCost })

        val allNodes = Array(width) { arrayOfNulls<Node>(height) }
        val nodeState = Array(width) { ByteArray(height) { 0 } }


        val startNode = Node(startX, startY).apply { gCost = 0 }
        allNodes[startX][startY] = startNode
        openSet.add(startNode)
        nodeState[startX][startY] = 1

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()!!

            if (current.x == endX && current.y == endY) {
                return retracePath(current)
            }

            nodeState[current.x][current.y] = 2

            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue

                    val nx = current.x + dx
                    val ny = current.y + dy

                    if (isValid(nx, ny) && walkableGrid[nx][ny] && nodeState[nx][ny] != 2.toByte()) {
                        val stepCost = if (Math.abs(dx) + Math.abs(dy) == 2) 14 else 10
                        val tentativeGCost = current.gCost + stepCost

                        val neighbor = allNodes[nx][ny] ?: Node(nx, ny).also { allNodes[nx][ny] = it }

                        if (tentativeGCost < neighbor.gCost) {
                            neighbor.parent = current
                            neighbor.gCost = tentativeGCost
                            neighbor.hCost = heuristic.calc(nx, ny, endX, endY)

                            if (nodeState[nx][ny] != 1.toByte()) {
                                openSet.add(neighbor)
                                nodeState[nx][ny] = 1
                            } else {
                                openSet.remove(neighbor)
                                openSet.add(neighbor)
                            }
                        }
                    }

                }
            }

        }
        return null
    }

    private fun isValid(x: Int, y: Int) = x in 0 until width && y in 0 until height
    private fun isValid(node: Node) = node.x in 0 until width && node.y in 0 until height

    private fun retracePath(node: Node): List<Pair<Int, Int>> {
        val path = mutableListOf<Pair<Int, Int>>()
        var current: Node? = node

        while (current != null) {
            path.add(current.x to current.y)
            current = current.parent
        }
        return path.reversed()
    }
}