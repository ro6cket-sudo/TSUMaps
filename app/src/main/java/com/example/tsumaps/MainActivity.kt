package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.tsumaps.core.MapConstants
import com.example.tsumaps.core.Point
import com.example.tsumaps.ui.theme.TSUMapsTheme
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tsumaps.ui.viewmodels.MapViewModel
import androidx.compose.ui.geometry.Size
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
                 path: List<Point>,
                 startPoint: Point?,
                 endPoint: Point?,
                 viewModel: MapViewModel = viewModel(),
                 onPointSelected: (Point) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }
    Box(
        modifier = modifier
            .onGloballyPositioned { containerSize = it.size }
            .pointerInput(viewModel.isObstacleMode, scale, offset) {
                if (viewModel.isObstacleMode) {
                    detectDragGestures { change, _ ->
                        change.consume()

                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        val cellSize = size.width.toFloat() / MapConstants.GRID_WIDTH

                        val touchX = change.position.x
                        val touchY = change.position.y

                        val mapX = (touchX - centerX - offset.x) / scale + centerX
                        val mapY = (touchY - centerY - offset.y) / scale + centerY

                        val gridX = (mapX / cellSize).toInt().coerceIn(0, MapConstants.GRID_WIDTH - 1)
                        val gridY = (mapY / cellSize).toInt().coerceIn(0, MapConstants.GRID_HEIGHT - 1)

                        viewModel.onMapClick(Point.of(gridX, gridY))
                    }
                } else {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        val cellSize = size.width.toFloat() / MapConstants.GRID_WIDTH

                        val mapX = (tapOffset.x - centerX - offset.x) / scale + centerX
                        val mapY = (tapOffset.y - centerY - offset.y) / scale + centerY

                        val gridX = (mapX / cellSize).toInt().coerceIn(0, MapConstants.GRID_WIDTH - 1)
                        val gridY = (mapY / cellSize).toInt().coerceIn(0, MapConstants.GRID_HEIGHT - 1)

                        viewModel.onMapClick(Point.of(gridX, gridY))
                    }
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = state)
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
            }

    }
}