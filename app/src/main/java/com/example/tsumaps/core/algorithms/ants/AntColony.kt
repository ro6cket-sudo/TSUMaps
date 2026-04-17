package com.example.tsumaps.core.algorithms.ants

import com.example.tsumaps.core.Place
import com.example.tsumaps.core.algorithms.ants.pheromone.PheromoneUpdater
import com.example.tsumaps.core.algorithms.ants.pheromone.StandardPheromoneUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.math.pow
import kotlin.random.Random


class AntColony(
    private val antsCount: Int = 30,
    private val maxIterations: Int = 300,
    private val alpha: Double = 1.0,
    private val beta: Double = 3.0,
    private val evaporationRate: Double = 0.15,
    private val pheromoneDeposit: Double = 100.0,
    private val initialPheromone: Double = 1.0,
    private val convergenceLimit: Int = 60,
    private val pheromoneUpdater: PheromoneUpdater = StandardPheromoneUpdater(),
    private val emitEveryIteration: Boolean = false,
    private val iterationDelayMs: Long = 0L
) : AntColonyOptimizer {

    override fun findOptimalRoute(
        places: List<Place>,
        distanceMatrix: Array<IntArray>,
        startDistances: IntArray
    ): Flow<AntEvent> = flow {
        val n = places.size

        if (n == 0) {
            emit(AntEvent.NoSolutionFound)
            return@flow
        }

        if (n == 1) {
            val cost = startDistances[0] * 2
            emit(
                AntEvent.OptimizationFinished(
                    finalRoute = listOf(places[0]),
                    totalCost = cost,
                    iterations = 0
                )
            )
            return@flow
        }

        val size = n + 1
        val startIdx = n

        val dist = Array(size) { IntArray(size) }
        for (i in 0 until n) {
            val src = distanceMatrix[i]
            val dst = dist[i]
            for (j in 0 until n) {
                dst[j] = src[j]
            }
            dist[startIdx][i] = startDistances[i]
            dist[i][startIdx] = startDistances[i]
        }

        val pheromone = Array(size) { DoubleArray(size) { initialPheromone } }
        for (i in 0 until size) pheromone[i][i] = 0.0

        val heuristic = Array(size) { i ->
            DoubleArray(size) { j ->
                if (i == j) 0.0
                else {
                    val d = dist[i][j]
                    if (d <= 0) 0.0 else 1.0 / d
                }
            }
        }

        val rng = Random(System.currentTimeMillis())

        val antRoutes = Array(antsCount) { IntArray(n) }
        val antCosts = IntArray(antsCount)
        val visited = BooleanArray(size)
        val probabilities = DoubleArray(n)

        var bestRoute = greedyRoute(n, startIdx, dist)
        var bestCost = routeCost(bestRoute, startIdx, dist)
        var bestIteration = 0
        var noImprovementCount = 0

        emit(
            AntEvent.NewBestRoute(
                iteration = 0,
                route = bestRoute.map { places[it] },
                totalCost = bestCost
            )
        )

        for (iteration in 0 until maxIterations) {
            if (!currentCoroutineContext().isActive) break

            for (a in 0 until antsCount) {
                for (i in 0 until size) visited[i] = false
                visited[startIdx] = true

                var currentIdx = startIdx
                var cost = 0

                for (step in 0 until n) {
                    val nextIdx = selectNext(
                        currentIdx = currentIdx,
                        visited = visited,
                        pheromone = pheromone,
                        heuristic = heuristic,
                        probabilities = probabilities,
                        n = n,
                        rng = rng
                    )
                    antRoutes[a][step] = nextIdx
                    cost += dist[currentIdx][nextIdx]
                    visited[nextIdx] = true
                    currentIdx = nextIdx
                }

                cost += dist[currentIdx][startIdx]
                antCosts[a] = cost
            }

            var bestAntIdx = 0
            for (a in 1 until antsCount) {
                if (antCosts[a] < antCosts[bestAntIdx]) bestAntIdx = a
            }

            val isNewBest = antCosts[bestAntIdx] < bestCost
            if (isNewBest) {
                bestCost = antCosts[bestAntIdx]
                bestRoute = antRoutes[bestAntIdx].copyOf()
                bestIteration = iteration
                noImprovementCount = 0

                emit(
                    AntEvent.NewBestRoute(
                        iteration = iteration,
                        route = bestRoute.map { places[it] },
                        totalCost = bestCost
                    )
                )
            } else {
                noImprovementCount++
            }

            if (emitEveryIteration) {
                emit(
                    AntEvent.IterationRoute(
                        iteration = iteration,
                        route = antRoutes[bestAntIdx].map { places[it] },
                        cost = antCosts[bestAntIdx],
                        isGlobalBest = isNewBest
                    )
                )
                if (iterationDelayMs > 0L) delay(iterationDelayMs)
            }

            if (noImprovementCount >= convergenceLimit) break

            pheromoneUpdater.update(
                pheromone = pheromone,
                routes = antRoutes,
                costs = antCosts,
                startIdx = startIdx,
                evaporationRate = evaporationRate,
                pheromoneDeposit = pheromoneDeposit,
                bestRoute = bestRoute,
                bestCost = bestCost
            )
        }

        emit(
            AntEvent.OptimizationFinished(
                finalRoute = bestRoute.map { places[it] },
                totalCost = bestCost,
                iterations = bestIteration
            )
        )
    }.flowOn(Dispatchers.Default)

    private fun selectNext(
        currentIdx: Int,
        visited: BooleanArray,
        pheromone: Array<DoubleArray>,
        heuristic: Array<DoubleArray>,
        probabilities: DoubleArray,
        n: Int,
        rng: Random
    ): Int {
        val pheroRow = pheromone[currentIdx]
        val heurRow = heuristic[currentIdx]

        var total = 0.0
        for (j in 0 until n) {
            if (visited[j]) {
                probabilities[j] = 0.0
                continue
            }
            val tau = pheroRow[j].pow(alpha)
            val eta = heurRow[j].pow(beta)
            val p = tau * eta
            probabilities[j] = p
            total += p
        }

        if (total <= 0.0 || total.isNaN()) {
            for (j in 0 until n) if (!visited[j]) return j
            return 0
        }

        val threshold = rng.nextDouble() * total
        var acc = 0.0
        for (j in 0 until n) {
            acc += probabilities[j]
            if (acc >= threshold) return j
        }
        for (j in n - 1 downTo 0) if (!visited[j]) return j
        return 0
    }

    private fun greedyRoute(n: Int, startIdx: Int, dist: Array<IntArray>): IntArray {
        val route = IntArray(n)
        val used = BooleanArray(n)
        var current = startIdx

        for (step in 0 until n) {
            var bestIdx = -1
            var bestDist = Int.MAX_VALUE
            for (j in 0 until n) {
                if (used[j]) continue
                val d = dist[current][j]
                if (d < bestDist) {
                    bestDist = d
                    bestIdx = j
                }
            }
            route[step] = bestIdx
            used[bestIdx] = true
            current = bestIdx
        }
        return route
    }

    private fun routeCost(route: IntArray, startIdx: Int, dist: Array<IntArray>): Int {
        var prev = startIdx
        var total = 0
        for (idx in route) {
            total += dist[prev][idx]
            prev = idx
        }
        total += dist[prev][startIdx]
        return total
    }
}
