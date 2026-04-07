package com.example.tsumaps.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.AStarFinder
import com.example.tsumaps.core.algorithms.PathfindingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.sqrt
import kotlinx.coroutines.Job

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val pathFinder = AStarFinder()
    private var mapGrid: BooleanArray? = null
    var startPoint by mutableStateOf<Point?>(null)
    var endPoint by mutableStateOf<Point?>(null)
    var calculatedPath by mutableStateOf<List<Point>>(emptyList())
    var isSearching by mutableStateOf(false)

    var toastMessage by mutableStateOf<String?>(null)
        private set

    var openNodes = mutableStateListOf<Point>()
    var closedNodes = mutableStateListOf<Point>()
    var finalPath = mutableStateListOf<Point>()
    var isSelectionMode by mutableStateOf(false)
    var isObstacleMode by mutableStateOf(false)
    val customObstacles = mutableStateListOf<Point>()
    private var pathfindingJob: Job? = null
    fun clearToast() { toastMessage = null }
    init {
        loadMapData()
    }

    private fun loadMapData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = getApplication<Application>().assets
                    .open("map_array.json")
                    .bufferedReader()
                    .use { it.readText() }

                val jsonObject = JSONObject(jsonString)
                val dataArray = jsonObject.getJSONArray("data")

                val booleanGrid = BooleanArray(dataArray.length())

                for (i in 0 until dataArray.length()) {
                    booleanGrid[i] = dataArray.getInt(i) == 0
                }
                mapGrid = booleanGrid
                pathFinder.setBaseMap(booleanGrid)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleSelectionMode() {
        isSelectionMode = true
        startPoint = null
        endPoint = null
        pathfindingJob?.cancel()
        openNodes.clear()
        closedNodes.clear()
        finalPath.clear()
    }

    fun toggleObstacleMode() {
        isObstacleMode = !isObstacleMode
        if (isObstacleMode) isSelectionMode = false
        toastMessage = if (isObstacleMode) "Режим рисования стен включен" else "Режим стен выключен"
    }

    fun clearObstacles() {
        customObstacles.clear()
        pathFinder.clearDynamicObstacles()
        toastMessage = "Стены очищены"
    }


    fun onMapClick(point: Point) {
        val grid = mapGrid ?: return
        if (isObstacleMode) {
            val raduis = 1

            for (dx in -raduis..raduis) {
                for (dy in -raduis..raduis) {
                    val nx = point.x + dx
                    val ny = point.y + dy

                    if (nx in 0 until MapConstants.GRID_WIDTH &&
                        ny in 0 until MapConstants.GRID_HEIGHT) {
                        val newPoint = Point.of(nx,ny)
                        if (!customObstacles.contains(newPoint)) {
                            customObstacles.add(newPoint)
                        }
                    }
                }
            }
            return
        }

        if (!isSelectionMode) return


        if (point.x !in 0 until MapConstants.GRID_WIDTH ||
            point.y !in 0 until MapConstants.GRID_HEIGHT) {
            return
        }

        val nearestRoad = findNearestRoad(point.x, point.y, 5, grid)

        if (nearestRoad == null) {
            toastMessage = "Здесь нет прохода!"
            return
        }

        if (nearestRoad.x != point.x || nearestRoad.y != point.y) {
            toastMessage = "Точка смещена на ближайшую дорогу"
        }

        if (startPoint == null || (startPoint != null && endPoint != null)) {
            startPoint = nearestRoad
            endPoint = null
            calculatedPath = emptyList()
        } else {
            endPoint = nearestRoad
            isSelectionMode = false
        }
    }

    fun onBuildPathClick() {
        if (startPoint != null && endPoint != null) {
            pathFinder.clearDynamicObstacles()
            pathFinder.setObstacles(customObstacles.toList())
            startAnimatedPath(startPoint!!, endPoint!!)
        }
        else {
            toastMessage = "Сначала установите обе точки"
        }
    }

    private fun findNearestRoad(startX: Int, startY: Int, radius: Int, grid: BooleanArray): Point? {
        if (grid[startY * MapConstants.GRID_WIDTH + startX]) return Point.of(startX, startY)

        var bestPoint: Point? = null
        var minDistance = Double.MAX_VALUE

        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val nx = startX + dx
                val ny = startY + dy
                if (nx in 0 until MapConstants.GRID_WIDTH && ny in 0 until MapConstants.GRID_HEIGHT) {
                    val index = ny * MapConstants.GRID_WIDTH + nx
                    if (grid[index]) {
                        val dist = sqrt((dx * dx + dy * dy).toDouble())
                        if (dist < minDistance) {
                            minDistance = dist
                            bestPoint = Point.of(nx, ny)
                        }
                    }
                }
            }
        }
        return bestPoint
    }
    fun startAnimatedPath(start: Point, end: Point) {
        pathfindingJob?.cancel()

        openNodes.clear()
        closedNodes.clear()
        finalPath.clear()

        pathfindingJob = viewModelScope.launch {
            pathFinder.findPathAnimated(start, end).collect { event ->
                when (event) {
                    is PathfindingEvent.NodesOpened -> {
                        openNodes.addAll(event.points)
                    }

                    is PathfindingEvent.NodeClosed -> {
                        openNodes.remove(event.point)
                        closedNodes.add(event.point)
                    }

                    is PathfindingEvent.PathFound -> {
                        event.path?.let { finalPath.addAll(it) }
                    }
                }
            }
        }
    }
}
