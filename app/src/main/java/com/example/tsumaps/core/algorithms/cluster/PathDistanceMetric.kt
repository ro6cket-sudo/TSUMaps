package com.example.tsumaps.core.algorithms.cluster

import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.astar.AStarFinder
import kotlin.math.sqrt

class PathDistanceMetric : DistanceMetric {
    override val name = "Пешеходное"
    private var finder: AStarFinder? = null
    private val cache = HashMap<Long, Double>()

    var isInitialized = false
        private set

    override fun distance(a: Place, b: Place): Double {
        val key = cacheKey(a.id, b.id)
        cache[key]?.let { return it }
        return computeAndCache(a, b)
    }

    fun initialize(grid: BooleanArray) {
        if (finder == null) finder = AStarFinder()
        finder!!.setBaseMap(grid)
        isInitialized = true
        cache.clear()
    }

    private fun cacheKey(id1: Int, id2: Int): Long {
        val lo = minOf(id1, id2).toLong()
        val hi = maxOf(id1, id2).toLong()
        return lo * 10000L + hi
    }

    private fun euclidean(ax: Int, ay: Int, bx: Int, by: Int): Double {
        val dx = (ax - bx).toDouble()
        val dy = (ay - by).toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    private fun computeAndCache(a: Place, b: Place): Double {
        val (ax, ay) = MapConstants.latLonToGrid(a.lat, a.lon)
        val (bx, by) = MapConstants.latLonToGrid(b.lat, b.lon)
        val path = finder?.findPath(Point.of(ax, ay), Point.of(bx, by))
        val dist = path?.size?.toDouble() ?: (euclidean(ax, ay, bx, by))
        cache[cacheKey(a.id, b.id)] = dist
        cache[cacheKey(b.id, a.id)] = dist
        return dist
    }

    fun precompute(
        places: List<Place>,
        onProgress: (computed: Int, total: Int) -> Unit = { _, _ -> }
    ) {
        val total = places.size * (places.size - 1) / 2
        var computed = 0
        for (i in places.indices) {
            for (j in i + 1 until places.size) {
                computeAndCache(places[i], places[j])
                computed++
                if (computed % 50 == 0) onProgress(computed, total)
            }
        }
    }
}