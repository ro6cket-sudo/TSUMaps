package com.example.tsumaps.core.algorithms.genetic

import com.example.tsumaps.core.Place
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.astar.PathFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class DistanceMatrixCalc(
    private val pathFinderFactory: () -> PathFinder
) {
    suspend fun calculateMatrix(
        places: List<Place>,
        startPoint: Point
    ): Pair<Array<IntArray>, IntArray> =
        withContext(Dispatchers.Default) {
            val size = places.size
            val matrix = Array(size) { IntArray(size) }

            val matrixJobs = (0 until size).flatMap { i ->
                (0 until i).map { j ->
                    async {
                        val localFinder = pathFinderFactory()
                        val path = localFinder.findPath(places[i].point, places[j].point)
                        val dist = path?.size ?: 100000
                        Triple(i, j, dist)
                    }
                }
            }

            val startJobs = (0 until size).map { i ->
                async {
                    val localFinder = pathFinderFactory()
                    val path = localFinder.findPath(startPoint, places[i].point)
                    val dist = path?.size ?: 100000
                    Pair(i, dist)
                }
            }

            matrixJobs.awaitAll().forEach { (i, j, dist) ->
                matrix[i][j] = dist
                matrix[j][i] = dist
            }

            val startDistances = IntArray(size)
            startJobs.awaitAll().forEach { (i, dist) ->
                startDistances[i] = dist
            }
            Pair(matrix, startDistances)
        }
}