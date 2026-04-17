package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.tsumaps.ui.screens.MainScreen
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Point
import com.example.tsumaps.ui.theme.TSUMapsTheme
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tsumaps.ui.viewmodels.MapViewModel
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.positionChanged
import com.example.tsumaps.core.PlaceStorage
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TSUMapsTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun TsuMapScreen(modifier: Modifier = Modifier,
                 startPoint: Point?,
                 endPoint: Point?,
                 viewModel: MapViewModel = viewModel()
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(viewModel.isObstacleMode) {
                awaitEachGesture {
                    val touchSlop = viewConfiguration.touchSlop
                    val down = awaitFirstDown(requireUnconsumed = false)

                    var isTap = true
                    var lastPosition = down.position
                    var lastCentroid = Offset.Zero
                    var isZooming = false
                    var skipFrames = 0

                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count {it.pressed}

                        when {
                            pressedCount >= 2 -> {
                                isTap = false
                                isZooming = true
                                skipFrames = 0

                                val centroid = event.calculateCentroid(useCurrent = true)
                                val centroidJump = if (lastCentroid == Offset.Zero) Float.MAX_VALUE else
                                    sqrt((centroid.x - lastCentroid.x) * (centroid.x - lastCentroid.x) +
                                            (centroid.y - lastCentroid.y) * (centroid.y - lastCentroid.y)
                                    )

                                if (lastCentroid != Offset.Zero && centroidJump < 150f) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val oldScale = scale
                                    val newScale = (oldScale * zoomChange).coerceIn(0.5f, 10f)

                                    offset = Offset(
                                        x = centroid.x - (centroid.x - offset.x) / oldScale * newScale + panChange.x,
                                        y = centroid.y - (centroid.y - offset.y) / oldScale * newScale + panChange.y
                                    )
                                    scale = newScale
                                }
                                lastCentroid = centroid
                                lastPosition = centroid
                                event.changes.forEach { it.consume() }
                            }

                            else -> {
                                if (isZooming) {
                                    skipFrames++
                                    if (skipFrames >= 3) {
                                        isZooming = false
                                        lastCentroid = Offset.Zero
                                        event.changes.firstOrNull()?.let {
                                            lastPosition = it.position
                                        }
                                    }
                                    event.changes.forEach { it.consume() }
                                }
                                else {
                                    val change = event.changes.firstOrNull() ?: continue

                                    if (change.positionChanged()) {
                                        val dragDist = sqrt ((change.position.x - down.position.x) *
                                                (change.position.x - down.position.x) +
                                                (change.position.y - down.position.y) *
                                                (change.position.y - down.position.y)
                                        )
                                        if (dragDist > touchSlop) isTap = false
                                    }

                                    if (!isTap && change.pressed) {
                                        change.consume()
                                        if (viewModel.isObstacleMode) {
                                            val cellSize = size.width.toFloat() / MapConstants.GRID_WIDTH
                                            val mapX = (change.position.x - offset.x) / scale
                                            val mapY = (change.position.y - offset.y) / scale
                                            val gridX = (mapX / cellSize).toInt().coerceIn(0, MapConstants.GRID_WIDTH - 1)
                                            val gridY = (mapY / cellSize).toInt().coerceIn(0, MapConstants.GRID_HEIGHT - 1)
                                            viewModel.onMapClick(Point.of(gridX, gridY))
                                        }
                                        else {
                                            offset += change.position - lastPosition
                                        }
                                    }
                                    lastPosition = change.position

                                    if (!change.pressed && isTap) {
                                        val cellSize = size.width.toFloat() / MapConstants.GRID_WIDTH
                                        val mapX = (down.position.x - offset.x) / scale
                                        val mapY = (down.position.y - offset.y) / scale
                                        val gridX = (mapX / cellSize).toInt().coerceIn(0, MapConstants.GRID_WIDTH - 1)
                                        val gridY = (mapY / cellSize).toInt().coerceIn(0, MapConstants.GRID_HEIGHT - 1)
                                        viewModel.onPlaceTap(gridX, gridY)
                                        if (viewModel.selectedPlace == null) {
                                            viewModel.onMapClick(Point.of(gridX, gridY))
                                        }
                                    }
                                }
                            }
                        }
                    } while (event.changes.any {it.pressed})
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
                transformOrigin = TransformOrigin(0f, 0f)
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.tsu_map),
                contentDescription = "Карта ТГУ",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / MapConstants.GRID_WIDTH

                startPoint?.let {
                    drawCircle(Color.Green, radius = 10f, center = Offset(it.x * cellSize, it.y * cellSize)) }
                endPoint?.let {
                    drawCircle(Color.Red, radius = 10f, center = Offset(it.x * cellSize, it.y * cellSize))}
                viewModel.closedNodes.forEach { pt ->
                    drawRect(
                        color = Color(0x44FF0000),
                        topLeft = Offset(pt.x * cellSize, pt.y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }

                viewModel.openNodes.forEach { pt ->
                    drawRect(
                        color = Color(0x66FFFF00),
                        topLeft = Offset(pt.x * cellSize, pt.y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }

                viewModel.customObstacles.forEach { pt ->
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(pt.x * cellSize, pt.y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }

                if (viewModel.finalPath.isNotEmpty()) {
                    viewModel.finalPath.forEach { pt ->
                        drawRect(
                            color = Color.Blue,
                            topLeft = Offset(pt.x * cellSize, pt.y * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }

                if (viewModel.isClusteringActive) {
                    val clusterColors = listOf(
                        Color.Red, Color(0xFF8BC34A), Color.Green, Color.Magenta, Color.Cyan,
                        Color(0xFFFF9800),Color(0xFFBCAAA4),Color(0xFFA5D6A7),Color(0xFFE91E63),
                        Color(0xFFB0BEC5)
                    )

                    viewModel.clusteredPlaces.forEach { cp ->
                        val (gridX, gridY) = MapConstants.latLonToGrid(cp.place.lat, cp.place.lon)
                        val cx = gridX * cellSize
                        val cy = gridY * cellSize
                        val color = clusterColors[cp.clusterIndex % clusterColors.size]
                        drawCircle(Color.White, radius = 5f, center = Offset(cx, cy))
                        drawCircle(color, radius = 3f, center = Offset(cx, cy))
                    }
                } else {
                    viewModel.visiblePlaces.forEach { place ->
                        val (gridX, gridY) = MapConstants.latLonToGrid(place.lat, place.lon)
                        val cx = gridX * cellSize
                        val cy = gridY * cellSize
                        drawCircle(Color.White, radius = 5f, center = Offset(cx, cy))
                        drawCircle(Color.Cyan, radius = 3f, center = Offset(cx, cy))
                    }
                }
            }

    }
}