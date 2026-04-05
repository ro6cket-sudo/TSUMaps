package com.example.tsumaps.core.algorithms

import com.example.tsumaps.core.FoodItem
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.algorithms.genetic.GeneticEvent
import com.example.tsumaps.core.algorithms.genetic.GeneticOptimizer
import kotlinx.coroutines.flow.Flow
import kotlin.math.min
import kotlin.random.Random

class Genetic(
    private val populationSize: Int = 150,
    private val maxIterations: Int = 500,
    private val mutationProb: Float = 0.03f,
    private val eliteCount: Int = 5,
    private val tournamentSize: Int = 5,
    private val convergenceLimit: Int = 80,
    private val cellSize: Float = 2f
) : GeneticOptimizer {
    private val speedMetersPerMinute = 5000f / 60f
    private val minutesPerCell = cellSize / speedMetersPerMinute

    override fun findOptimalRoute(
        places: List<Place>,
        order: Set<FoodItem>,
        currentTime: Int,
        distanceMatrix: Array<IntArray>,
        startDistances: IntArray
    ): Flow<GeneticEvent> {
        TODO("Not yet implemented")
    }

    private fun initPopulation(
        n: Int,
        size: Int,
        menuMasks: IntArray,
        requiredMask: Int,
        distanceMatrix: Array<IntArray>,
        startDistance: IntArray,
        rng: Random
    ): Array<IntArray> = Array(size) {idx ->
        if (idx == 0) greedyChromosome(n, menuMasks, requiredMask, distanceMatrix, startDistance)
        else IntArray(n) { it }.also { arr -> arr.shuffle(rng) }
    }

    private fun buildRelevantMatrix(
        fullMatrix: Array<IntArray>,
        indices: List<Int>
    ) : Array<IntArray> {
        val size = indices.size
        return Array(size) { i ->
            IntArray(size) { j -> fullMatrix[indices[i]][indices[j]]}
        }
    }

    private fun greedyChromosome(
        n: Int,
        menuMasks: IntArray,
        requiredMask: Int,
        distanceMatrix: Array<IntArray>,
        startDistances: IntArray
    ) : IntArray {
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

        chromosome[0] = 0
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
    ) : Int {
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
                val minutesLeft = places[idx].closeTime - timeNowInt
                if (minutesLeft < 30) {
                    totalCost -= 500
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

    private fun tournamentSelect(
        population: Array<IntArray>,
        fitness: IntArray,
        rng: Random
    ) : IntArray {
        var bestIdx = rng.nextInt(population.size)
        repeat(tournamentSize - 1) {
            val candidate = rng.nextInt(population.size)
            if (fitness[candidate] < fitness[bestIdx]) bestIdx = candidate
        }
        return population[bestIdx]
    }

    private fun orderCrossover( //8c in https://www.geeksforgeeks.org/dsa/genetic-algorithms/
        parent1: IntArray,
        parent2: IntArray,
        destination: IntArray,
        inChildVersion: IntArray,
        crossoverVersion: Int,
        rng: Random
    ) {
        val size = parent1.size

        var start = rng.nextInt(size)
        var end = rng.nextInt(size)

        if (start > end) { val temp = start; start = end; end = temp }
        if (start == end) end = min(size, end + 1)

        for (i in start until end) {
            destination[i] = parent1[i]
            inChildVersion[parent1[i]] = crossoverVersion
        }

        var pos = end % size

        for (gen in parent2) {
            if (inChildVersion[gen] != crossoverVersion) {
                destination[pos] = gen
                inChildVersion[gen] = crossoverVersion
                pos = (pos + 1) % size
            }
        }
    }

    private fun mutate(chromosome: IntArray, rng: Random) {
        val i = rng.nextInt(chromosome.size)
        val j = rng.nextInt(chromosome.size)
        val tmp = chromosome[i]
        chromosome[i] = chromosome[j]
        chromosome[j] = chromosome[i]
    }

    private fun chromosomeToRoute(
        chromosome: IntArray,
        places: List<Place>,
        menuMasks: IntArray,
        requiredMask: Int
    ) : List<Place> {
        val result = ArrayList<Place> (chromosome.size)
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