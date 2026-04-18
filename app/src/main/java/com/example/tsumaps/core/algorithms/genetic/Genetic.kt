package com.example.tsumaps.core.algorithms.genetic

import com.example.tsumaps.core.FoodItem
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.algorithms.genetic.crossover.Crossover
import com.example.tsumaps.core.algorithms.genetic.crossover.OrderCrossover
import com.example.tsumaps.core.algorithms.genetic.mutation.Mutation
import com.example.tsumaps.core.algorithms.genetic.mutation.SwapMutation
import com.example.tsumaps.core.algorithms.genetic.selections.LinearRankSelection
import com.example.tsumaps.core.algorithms.genetic.selections.ProbTournamentSelection
import com.example.tsumaps.core.algorithms.genetic.selections.Selection
import com.example.tsumaps.core.algorithms.genetic.selections.TournamentSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.random.Random

class Genetic(
    private val populationSize: Int = 150,
    private val maxIterations: Int = 500,
    private val mutationProb: Float = 0.03f,
    private val eliteCount: Int = 5,
    private val tournamentSize: Int = 5,
    private val convergenceLimit: Int = 80,
    private val cellSize: Float = 2f,
    private val mutation: Mutation = SwapMutation(),
    private val crossover: Crossover = OrderCrossover(),
    private val selection: Selection = TournamentSelection()
) : GeneticOptimizer {
    private val speedMetersPerMinute = 5000f / 60f
    private val minutesPerCell = cellSize / speedMetersPerMinute

    override fun findOptimalRoute(
        places: List<Place>,
        order: Set<FoodItem>,
        currentTime: Int,
        distanceMatrix: Array<IntArray>,
        startDistances: IntArray
    ): Flow<GeneticEvent> = flow {
        val relevantIndices = places.indices.filter { i ->
            places[i].isOpen(currentTime) && places[i].menu.any { it in order }
        }

        val relevantPlaces = relevantIndices.map { places[it] }
        val relevantDistMatrix = buildRelevantMatrix(distanceMatrix, relevantIndices)
        val relevantStartDist =
            IntArray(relevantIndices.size) { i -> startDistances[relevantIndices[i]] }


        if (relevantPlaces.isEmpty()) {
            emit(GeneticEvent.NoSolutionFound)
            return@flow
        }

        val n = relevantPlaces.size
        val menuMasks = IntArray(n) { i ->
            relevantPlaces[i].menu.fold(0) { acc, item -> acc or item.bit }
        }
        val requiredMask = order.fold(0) { acc, item -> acc or item.bit }

        val totalAvailable = menuMasks.fold(0) { acc, mask -> acc or mask }

        if (totalAvailable and requiredMask != requiredMask) {
            emit(GeneticEvent.NoSolutionFound)
            return@flow
        }

        val rng = Random(System.currentTimeMillis())

        var current = initPopulation(
            n, populationSize, menuMasks, requiredMask, relevantDistMatrix, relevantStartDist, rng
        )

        var next = Array(populationSize) { IntArray(n) }

        val fitnessArr = IntArray(populationSize)

        val eliteVisitVersion = IntArray(populationSize)
        var eliteVersion = 0

        val inChildVersion = IntArray(n)
        var crossoverVersion = 0

        var bestChromosome = current[0].copyOf()
        var bestCost = Int.MAX_VALUE
        var bestIteration = 0
        var noImprovementCount = 0

        for (iteration in 0 until maxIterations) {
            if (!currentCoroutineContext().isActive) break

            for (i in 0 until populationSize) {
                fitnessArr[i] = evaluateFitness(
                    chromosome = current[i],
                    places = relevantPlaces,
                    menuMasks = menuMasks,
                    requiredMask = requiredMask,
                    distanceMatrix = relevantDistMatrix,
                    startDistance = relevantStartDist,
                    startTime = currentTime
                )
            }

            var bestIdxNow = 0
            for (i in 1 until populationSize) {
                if (fitnessArr[i] < fitnessArr[bestIdxNow]) bestIdxNow = i
            }
            val bestCostNow = fitnessArr[bestIdxNow]

            if (bestCostNow < bestCost) {
                bestCost = bestCostNow
                bestChromosome = current[bestIdxNow].copyOf()
                bestIteration = iteration
                noImprovementCount = 0

                emit(
                    GeneticEvent.NewBestRoute(
                        iteration = iteration,
                        route = chromosomeToRoute(
                            bestChromosome,
                            relevantPlaces,
                            menuMasks,
                            requiredMask
                        ),
                        totalCost = bestCost
                    )
                )
            } else noImprovementCount++

            if (noImprovementCount >= convergenceLimit) break


            eliteVersion++
            for (e in 0 until eliteCount) {
                var bestEliteIdx = -1
                for (i in 0 until populationSize) {
                    if (eliteVisitVersion[i] == eliteVersion) continue
                    if (bestEliteIdx == -1 || fitnessArr[i] < fitnessArr[bestEliteIdx]) {
                        bestEliteIdx = i
                    }
                }
                eliteVisitVersion[bestEliteIdx] = eliteVersion
                current[bestEliteIdx].copyInto(next[e])
            }

            for (i in eliteCount until populationSize) {
                val parent1 = selection.select(current, tournamentSize, fitnessArr, rng)
                val parent2 = selection.select(current, tournamentSize, fitnessArr, rng)

                crossoverVersion++
                crossover.cross(parent1, parent2, next[i], inChildVersion, crossoverVersion, rng)
                if (rng.nextFloat() < mutationProb) mutation.mutate(next[i], rng)
            }

            val tmp = current
            current = next
            next = tmp
        }

        emit(
            GeneticEvent.EvolutionFinished(
                finalRoute = chromosomeToRoute(
                    bestChromosome,
                    relevantPlaces,
                    menuMasks,
                    requiredMask
                ),
                totalCost = bestCost,
                iterations = bestIteration
            )
        )

    }.flowOn(Dispatchers.Default)

    private fun initPopulation(
        n: Int,
        size: Int,
        menuMasks: IntArray,
        requiredMask: Int,
        distanceMatrix: Array<IntArray>,
        startDistance: IntArray,
        rng: Random
    ): Array<IntArray> = Array(size) { idx ->
        if (idx == 0) greedyChromosome(n, menuMasks, requiredMask, distanceMatrix, startDistance)
        else IntArray(n) { it }.also { arr -> arr.shuffle(rng) }
    }

    private fun buildRelevantMatrix(
        fullMatrix: Array<IntArray>,
        indices: List<Int>
    ): Array<IntArray> {
        val size = indices.size
        return Array(size) { i ->
            IntArray(size) { j -> fullMatrix[indices[i]][indices[j]] }
        }
    }

    private fun greedyChromosome(
        n: Int,
        menuMasks: IntArray,
        requiredMask: Int,
        distanceMatrix: Array<IntArray>,
        startDistances: IntArray
    ): IntArray {
        val chromosome = IntArray(n)
        val visited = BooleanArray(n)
        var coveredMask = 0

        var firstIdx = -1
        var firstBestDist = Int.MAX_VALUE

        for (candidate in 0 until n) {
            val addNew = menuMasks[candidate] and requiredMask
            val dist = if (addNew != 0) startDistances[candidate]
            else startDistances[candidate] + 100000

            if (dist < firstBestDist) {
                firstBestDist = dist
                firstIdx = candidate
            }
        }

        chromosome[0] = firstIdx
        visited[firstIdx] = true
        coveredMask = menuMasks[firstIdx]
        var lastIdx = firstIdx

        for (step in 1 until n) {
            var bestIdx = -1
            var bestDist = Int.MAX_VALUE

            for (candidate in 0 until n) {
                if (visited[candidate]) continue

                val addNew = menuMasks[candidate] and requiredMask and coveredMask.inv()
                val dist = distanceMatrix[lastIdx][candidate]

                if (addNew != 0 && dist < bestDist) {
                    bestDist = dist
                    bestIdx = candidate
                }
            }

            if (bestIdx == -1) {
                for (candidate in 0 until n) {
                    if (visited[candidate]) continue

                    val dist = distanceMatrix[lastIdx][candidate]
                    if (dist < bestDist) {
                        bestDist = dist
                        bestIdx = candidate
                    }
                }
            }

            chromosome[step] = bestIdx
            visited[bestIdx] = true
            coveredMask = coveredMask or menuMasks[bestIdx]
            lastIdx = bestIdx
        }

        return chromosome
    }

    private fun evaluateFitness(
        chromosome: IntArray,
        places: List<Place>,
        menuMasks: IntArray,
        requiredMask: Int,
        distanceMatrix: Array<IntArray>,
        startDistance: IntArray,
        startTime: Int
    ): Int {
        var totalCost = 0
        var coveredMask = 0
        var timeNow = startTime.toFloat()
        var prevIdx = -1

        for (idx in chromosome) {
            val newItems = menuMasks[idx] and requiredMask and coveredMask.inv()
            if (newItems == 0) continue

            val distCells = if (prevIdx == -1) startDistance[idx]
            else distanceMatrix[prevIdx][idx]

            totalCost += distCells
            timeNow += distCells * minutesPerCell

            coveredMask = coveredMask or menuMasks[idx]

            val timeNowInt = timeNow.toInt()
            if (!places[idx].isOpen(timeNowInt)) {
                totalCost += 100000
            } else {
                val minutesLeft = places[idx].minutesUntilClose(timeNowInt)
                if (minutesLeft < 30) {
                    totalCost -= 300
                }
            }

            prevIdx = idx
            if (coveredMask and requiredMask == requiredMask) break
        }

        val missingMask = requiredMask and coveredMask.inv()
        if (missingMask != 0) {
            totalCost += Integer.bitCount(missingMask) * 100000
        }

        return totalCost
    }

    private fun chromosomeToRoute(
        chromosome: IntArray,
        places: List<Place>,
        menuMasks: IntArray,
        requiredMask: Int
    ): List<Place> {
        val result = ArrayList<Place>(chromosome.size)
        var coveredMask = 0

        for (idx in chromosome) {
            val newItems = menuMasks[idx] and requiredMask and coveredMask.inv()
            if (newItems != 0) {
                result.add(places[idx])
                coveredMask = coveredMask or menuMasks[idx]
                if (coveredMask and requiredMask == requiredMask) break;
            }
        }

        return result
    }
}