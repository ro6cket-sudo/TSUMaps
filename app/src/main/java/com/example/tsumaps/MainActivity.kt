package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.tsumaps.ui.viewmodels.MapViewModel
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

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
                 onPointSelected: (Point) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var imageLayoutInfo by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    imageLayoutInfo?.let { coords ->
                        val imageWidth = coords.size.width.toFloat()
                        val cellSize = imageWidth / MapConstants.GRID_WIDTH

                        val adjustedX = (tapOffset.x - offset.x) / scale
                        val adjustedY = (tapOffset.y - offset.y) / scale

                        val gridX = (adjustedX / cellSize).toInt()
                        val gridY = (adjustedY / cellSize).toInt()

                        if (gridX in 0 until MapConstants.GRID_WIDTH &&
                            gridY in 0 until MapConstants.GRID_HEIGHT) {
                                       onPointSelected(Point.of(gridX, gridY))}
            }
        }
    }
                .transformable(state = state),
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .onGloballyPositioned { imageLayoutInfo = it }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y)
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
                    drawCircle(Color.Green, radius = 10f, center = Offset(it.x * cellSize, it.y * cellSize))
                }

                endPoint?.let {
                    drawCircle(Color.Red, radius = 10f, center = Offset(it.x * cellSize, it.y * cellSize))
                }

                if (path.isNotEmpty()) {
                    val drawPath = androidx.compose.ui.graphics.Path().apply {
                        val start = path.first()
                        moveTo(start.x * cellSize, start.y * cellSize)

                        path.drop(1).forEach { point ->
                            lineTo(point.x * cellSize, point.y * cellSize)
                        }
                    }

                    drawPath(
                        path = drawPath,
                        color = Color.Red,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}