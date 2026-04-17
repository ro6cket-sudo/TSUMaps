package com.example.tsumaps.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Place
import com.example.tsumaps.core.PlaceStorage
import com.example.tsumaps.core.PlaceType
import com.example.tsumaps.core.Point
import com.example.tsumaps.core.algorithms.astar.AStarFinder
import com.example.tsumaps.core.algorithms.astar.PathfindingEvent
import com.example.tsumaps.core.algorithms.ants.AntColony
import com.example.tsumaps.core.algorithms.ants.AntEvent
import com.example.tsumaps.core.algorithms.ants.AntsDistanceMatrix
import com.example.tsumaps.core.algorithms.cluster.ClusteredPlace
import com.example.tsumaps.core.algorithms.cluster.Clustering
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.sqrt

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

    val visiblePlaces: List<Place>
        get() = PlaceStorage.places

    var selectedPlace by mutableStateOf<Place?>(null)
        private set

    var clusteredPlaces by mutableStateOf<List<ClusteredPlace>>(emptyList())
        private set

    var isClusteringActive by mutableStateOf(false)
        private set

    fun clearToast() { toastMessage = null }

    init { loadMapData() }

    fun clearSelectedPlace() { selectedPlace = null }

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
            val radius = 1
            for (dx in -radius..radius) {
                for (dy in -radius..radius) {
                    val nx = point.x + dx
                    val ny = point.y + dy
                    if (nx in 0 until MapConstants.GRID_WIDTH &&
                        ny in 0 until MapConstants.GRID_HEIGHT
                    ) {
                        val newPoint = Point.of(nx, ny)
                        if (!customObstacles.contains(newPoint)) customObstacles.add(newPoint)
                    }
                }
            }
            return
        }
        if (!isSelectionMode) return
        if (point.x !in 0 until MapConstants.GRID_WIDTH ||
            point.y !in 0 until MapConstants.GRID_HEIGHT) return

        val nearestRoad = findNearestRoad(point.x, point.y, 5, grid)
        if (nearestRoad == null) { toastMessage = "Здесь нет прохода!"; return }
        if (nearestRoad.x != point.x || nearestRoad.y != point.y)
            toastMessage = "Точка смещена на ближайшую дорогу"

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
        } else {
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
        } else {
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
                        if (dist < minDistance) { minDistance = dist; bestPoint = Point.of(nx, ny) }
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
                    is PathfindingEvent.NodesOpened -> openNodes.addAll(event.points)
                    is PathfindingEvent.NodeClosed -> {
                        openNodes.remove(event.point)
                        closedNodes.add(event.point)
                    }
                    is PathfindingEvent.PathFound -> event.path?.let { finalPath.addAll(it) }
                }
            }
        }
    }

    fun toggleClustering() {
        if (isClusteringActive) {
            clusteredPlaces = emptyList()
            isClusteringActive = false
        } else {
            clusteredPlaces = Clustering.kMeans(PlaceStorage.places, clusterCount)
            isClusteringActive = true
        }
    }

    fun onPlaceTap(gridX: Int, gridY: Int) {
        val tapRadius = 10
        var bestPlace: Place? = null
        var bestDist = Double.MAX_VALUE

        val searchList = if (isAntsMode)
            PlaceStorage.places.filter { it.type == PlaceType.LANDMARK }
        else
            PlaceStorage.places

        for (place in searchList) {
            val (px, py) = MapConstants.latLonToGrid(place.lat, place.lon)
            val dX = (gridX - px).toDouble()
            val dY = (gridY - py).toDouble()
            val dist = sqrt(dX * dX + dY * dY)
            if (dist < tapRadius && dist < bestDist) { bestDist = dist; bestPlace = place }
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

    fun incrementClusterCount() { clusterCount = (clusterCount + 1).coerceAtMost(10) }
    fun decrementClusterCount() { clusterCount = (clusterCount - 1).coerceAtLeast(2) }

    var currentIterSkip by mutableIntStateOf(1)
        private set

    fun decreaseSpeed() {
        currentIterSkip = (currentIterSkip - 1).coerceAtLeast(1)
        pathFinder.setAnimationSkip(currentIterSkip)
    }

    fun increaseSpeed() {
        currentIterSkip = (currentIterSkip + 1).coerceAtMost(30)
        pathFinder.setAnimationSkip(currentIterSkip)
    }


    val landmarkPlaces: List<Place>
        get() = PlaceStorage.places.filter { it.type == PlaceType.LANDMARK }

    var isAntsMode by mutableStateOf(false)
        private set

    var isAntsPickingStart by mutableStateOf(false)
        private set

    var antsStartPoint by mutableStateOf<Point?>(null)
        private set

    val antsSelectedPlaces = mutableStateListOf<Place>()

    var antsIsRunning by mutableStateOf(false)
        private set

    var antsIsAnimating by mutableStateOf(false)
        private set

    var antsRoute by mutableStateOf<List<Place>>(emptyList())
        private set

    var antsPath by mutableStateOf<List<Point>>(emptyList())
        private set

    var antsTotalDistanceMeters by mutableIntStateOf(0)
        private set

    private val metersPerCell = 2
    private var antsJob: Job? = null

    fun toggleAntsMode() {
        isAntsMode = !isAntsMode
        if (!isAntsMode) isAntsPickingStart = false
    }

    fun antsStartPickingStart() {
        if (!isAntsMode) return
        isAntsPickingStart = true
        isSelectionMode = false
        isObstacleMode = false
    }

    fun antsCancelPickingStart() { isAntsPickingStart = false }

    fun antsSetStart(point: Point) {
        val grid = mapGrid ?: return
        val snapped = findNearestRoad(point.x, point.y, 10, grid)
        if (snapped == null) { toastMessage = "Здесь нет прохода для старта"; return }
        antsStartPoint = snapped
        isAntsPickingStart = false
        antsRoute = emptyList()
        antsPath = emptyList()
        antsTotalDistanceMeters = 0
    }

    fun antsTogglePlace(place: Place) {
        val existing = antsSelectedPlaces.indexOfFirst { it.id == place.id }
        if (existing >= 0) antsSelectedPlaces.removeAt(existing)
        else antsSelectedPlaces.add(place)
        antsRoute = emptyList()
        antsPath = emptyList()
        antsTotalDistanceMeters = 0
    }

    fun antsIsPlaceSelected(place: Place): Boolean =
        antsSelectedPlaces.any { it.id == place.id }

    fun antsClearSelection() {
        antsSelectedPlaces.clear()
        antsRoute = emptyList()
        antsPath = emptyList()
        antsTotalDistanceMeters = 0
    }

    fun antsClearAll() {
        antsJob?.cancel()
        antsSelectedPlaces.clear()
        antsStartPoint = null
        antsRoute = emptyList()
        antsPath = emptyList()
        antsTotalDistanceMeters = 0
        isAntsPickingStart = false
        antsIsRunning = false
        antsIsAnimating = false
    }

    val antsCanRun: Boolean
        get() = !antsIsRunning &&
                antsStartPoint != null &&
                antsSelectedPlaces.size >= 2 &&
                mapGrid != null

    fun antsRun() {
        val start = antsStartPoint ?: run { toastMessage = "Сначала выберите стартовую точку"; return }
        val places = antsSelectedPlaces.toList()
        if (places.size < 2) { toastMessage = "Выберите хотя бы 2 достопримечательности"; return }
        val grid = mapGrid ?: return

        antsJob?.cancel()
        antsJob = viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                antsIsRunning = true
                antsRoute = emptyList()
                antsPath = emptyList()
                antsTotalDistanceMeters = 0
            }

            val placePoints = places.map { placeGridPoint(it, grid) }
            val obstacles = customObstacles.toList()
            val matrixCalc = AntsDistanceMatrix(
                pathFinderFactory = {
                    AStarFinder().apply { setBaseMap(grid); setObstacles(obstacles) }
                }
            )
            val (matrix, startDistances) = matrixCalc.calculate(placePoints, start)

            val colony = AntColony()
            var bestRoute: List<Place> = emptyList()
            colony.findOptimalRoute(places, matrix, startDistances).collect { event ->
                when (event) {
                    is AntEvent.NewBestRoute -> {
                        bestRoute = event.route
                        withContext(Dispatchers.Main) { antsRoute = event.route }
                    }
                    is AntEvent.OptimizationFinished -> bestRoute = event.finalRoute
                    AntEvent.NoSolutionFound -> withContext(Dispatchers.Main) {
                        toastMessage = "Муравьиный алгоритм не нашёл маршрут"
                    }
                    is AntEvent.IterationRoute -> Unit
                }
            }

            val fullPath = buildAntsFullPath(start, bestRoute, placePoints, places, grid, obstacles)
            val meters = fullPath.size * metersPerCell

            withContext(Dispatchers.Main) {
                antsRoute = bestRoute
                antsPath = fullPath
                antsTotalDistanceMeters = meters
                antsIsRunning = false
            }
        }
    }

    fun antsRunAnimated() {
        val start = antsStartPoint ?: run { toastMessage = "Сначала выберите стартовую точку"; return }
        val places = antsSelectedPlaces.toList()
        if (places.size < 2) { toastMessage = "Выберите хотя бы 2 достопримечательности"; return }
        val grid = mapGrid ?: return

        antsJob?.cancel()
        antsJob = viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                antsIsRunning = true
                antsIsAnimating = true
                antsRoute = emptyList()
                antsPath = emptyList()
                antsTotalDistanceMeters = 0
            }

            val placePoints = places.map { placeGridPoint(it, grid) }
            val obstacles = customObstacles.toList()
            val matrixCalc = AntsDistanceMatrix(
                pathFinderFactory = {
                    AStarFinder().apply { setBaseMap(grid); setObstacles(obstacles) }
                }
            )
            val (matrix, startDistances) = matrixCalc.calculate(placePoints, start)

            val colony = AntColony(
                beta = 2.0,
                emitEveryIteration = true,
                iterationDelayMs = 180L
            )

            var bestRoute: List<Place> = emptyList()
            colony.findOptimalRoute(places, matrix, startDistances).collect { event ->
                when (event) {
                    is AntEvent.IterationRoute -> withContext(Dispatchers.Main) {
                        antsRoute = event.route
                    }
                    is AntEvent.NewBestRoute -> bestRoute = event.route
                    is AntEvent.OptimizationFinished -> bestRoute = event.finalRoute
                    AntEvent.NoSolutionFound -> withContext(Dispatchers.Main) {
                        toastMessage = "Муравьиный алгоритм не нашёл маршрут"
                    }
                }
            }

            val fullPath = buildAntsFullPath(start, bestRoute, placePoints, places, grid, obstacles)
            val meters = fullPath.size * metersPerCell

            withContext(Dispatchers.Main) {
                antsRoute = bestRoute
                antsPath = fullPath
                antsTotalDistanceMeters = meters
                antsIsRunning = false
                antsIsAnimating = false
            }
        }
    }

    private fun placeGridPoint(place: Place, grid: BooleanArray): Point {
        val (x, y) = MapConstants.latLonToGrid(place.lat, place.lon)
        return findNearestRoad(x, y, 10, grid) ?: Point.of(x, y)
    }

    private suspend fun buildAntsFullPath(
        start: Point,
        route: List<Place>,
        allPlacePoints: List<Point>,
        allPlaces: List<Place>,
        grid: BooleanArray,
        obstacles: List<Point>
    ): List<Point> = withContext(Dispatchers.Default) {
        if (route.isEmpty()) return@withContext emptyList()

        val finder = AStarFinder().apply { setBaseMap(grid); setObstacles(obstacles) }
        val idToPoint = HashMap<Int, Point>(allPlaces.size)
        for (i in allPlaces.indices) idToPoint[allPlaces[i].id] = allPlacePoints[i]

        val result = ArrayList<Point>()
        var prev = start

        for (place in route) {
            val target = idToPoint[place.id] ?: continue
            val segment = finder.findPath(prev, target) ?: continue
            if (result.isEmpty()) result.addAll(segment)
            else for (k in 1 until segment.size) result.add(segment[k])
            prev = target
        }
        val backSegment = finder.findPath(prev, start)
        if (backSegment != null)
            for (k in 1 until backSegment.size) result.add(backSegment[k])

        result
    }
}