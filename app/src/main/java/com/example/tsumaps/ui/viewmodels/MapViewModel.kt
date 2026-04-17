package com.example.tsumaps.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.PlaceStorage
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.astar.AStarFinder
import com.example.tsumaps.core.algorithms.astar.PathfindingEvent
import com.example.tsumaps.core.algorithms.cluster.ClusterMetricType
import com.example.tsumaps.core.algorithms.cluster.ClusteredPlace
import com.example.tsumaps.core.algorithms.cluster.Clustering
import com.example.tsumaps.core.algorithms.cluster.EuclideanMetric
import com.example.tsumaps.core.algorithms.cluster.KMedoids
import com.example.tsumaps.core.algorithms.cluster.ManhattanMetric
import com.example.tsumaps.core.algorithms.cluster.PathDistanceMetric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

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
    private val pathDistanceMetric = PathDistanceMetric()
    var selectedMetric by mutableStateOf(ClusterMetricType.EUCLIDEAN)
        private set

    var isComputingClusters by mutableStateOf(false)
        private set


    val visiblePlaces: List<Place>
        get() = PlaceStorage.places

    var selectedPlace by  mutableStateOf<Place?>(null)
        private set

    var clusteredPlaces by mutableStateOf<List<ClusteredPlace>>(emptyList())
        private set

    var isClusteringActive by mutableStateOf(false)
        private set

    fun clearToast() { toastMessage = null }
    init {
        loadMapData()
    }

    fun clearSelectedPlace() {
        selectedPlace = null
    }

    private fun loadMapData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val booleanGrid = BooleanArray(MapConstants.GRID_WIDTH * MapConstants.GRID_HEIGHT)
                var index = 0

                getApplication<Application>().assets.open("map_array.json").bufferedReader().use { reader ->
                    var ch = reader.read()
                    while (ch != -1 && ch.toChar() != '[') ch = reader.read()

                    var value: Int
                    ch = reader.read()
                    while (ch != -1 && index < booleanGrid.size) {
                        val c = ch.toChar()
                        value = (c - '0')
                        if (c.isDigit()) {
                            if (value == 0) {
                                booleanGrid[index++] = true
                            } else {
                                booleanGrid[index++] = false
                            }
                        }
                        ch = reader.read()
                    }
                }
                mapGrid = booleanGrid
                pathFinder.setBaseMap(booleanGrid)
                pathDistanceMetric.initialize(booleanGrid)

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

    fun buildPath() {
        val start = startPoint
        val end = endPoint

        openNodes.clear()
        closedNodes.clear()
        finalPath.clear()

        if (start != null && end != null) {
            viewModelScope.launch(Dispatchers.Default) {
                isSearching = true

                val path = pathFinder.findPath(start, end)

                withContext(Dispatchers.Main) {
                    calculatedPath = path ?: emptyList()
                    isSearching = false
                    if (path == null) toastMessage = "Путь не найден"
                }
            }
        }
        else {
            toastMessage = "Сначала установите точки"
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

    fun toggleClustering() {
        if (isClusteringActive) {
            clusteredPlaces = emptyList()
            isClusteringActive = false
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                isComputingClusters = true
            }

            val metric = when (selectedMetric) {
                ClusterMetricType.EUCLIDEAN -> EuclideanMetric()
                ClusterMetricType.MANHATTAN -> ManhattanMetric()
                ClusterMetricType.PEDESTRIAN -> {
                    val total = PlaceStorage.places.size * (PlaceStorage.places.size - 1) / 2
                    pathDistanceMetric
                }
            }

            val result = KMedoids.cluster(PlaceStorage.places, clusterCount, metric)

            withContext(Dispatchers.Main) {
                clusteredPlaces = result
                isClusteringActive = true
                isComputingClusters = false
            }
        }
    }

    fun onPlaceTap(gridX: Int, gridY: Int) {
        val tapRadius = 10
        var bestPlace: Place? = null
        var bestDict = Double.MAX_VALUE
        for (place in PlaceStorage.places) {
            val (px, py) = MapConstants.latLonToGrid(place.lat, place.lon)
            val dX = (gridX -px).toDouble()
            val dY = (gridY -py).toDouble()
            val dist = sqrt(dX*dX + dY*dY)
            if (dist < tapRadius && dist < bestDict) {
                bestDict = dist
                bestPlace = place
            }
        }
        selectedPlace = bestPlace
    }

    fun clearPath() {
        pathfindingJob?.cancel()
        openNodes.clear()
        closedNodes.clear()
        finalPath.clear()
        calculatedPath = emptyList()
        startPoint = null
        endPoint = null
        toastMessage = "Маршрут очищен"
    }

    var clusterCount by mutableIntStateOf(5)
        private set

    fun incrementClusterCount() {
        clusterCount = (clusterCount + 1).coerceAtMost(10)
    }

    fun decrementClusterCount() {
        clusterCount = (clusterCount - 1).coerceAtLeast(2)
    }

    fun setClusterMetric(type: ClusterMetricType) {
        if (selectedMetric == type) return
        selectedMetric = type
        clusteredPlaces = emptyList()
        isClusteringActive = false
    }
}
