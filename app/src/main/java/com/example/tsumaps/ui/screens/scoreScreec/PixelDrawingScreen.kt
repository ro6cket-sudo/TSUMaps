package com.example.tsumaps

import android.widget.Space
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.core.DigitRecognizer.DigitNeuralNetwork
import com.example.tsumaps.ui.screens.scoreScreec.Draw
import com.example.tsumaps.ui.screens.scoreScreec.addDraw
import com.example.tsumaps.ui.theme.TsuBlue
import com.example.tsumaps.ui.theme.TsuLightBlue

@Composable
fun PixelDrawingScreen() {
    val gridSize = 50
    val context = LocalContext.current
    val digitRecognizer = remember { DigitNeuralNetwork(context) }
    val grid = remember {
        mutableStateListOf<MutableList<Int>>().apply {
            repeat(gridSize) {
                add(mutableStateListOf<Int>().apply { repeat(gridSize) { add(0) } })
            }
        }
    }

    var lastX by remember { mutableStateOf(-1f) }
    var lastY by remember { mutableStateOf(-1f) }
    var score by remember { mutableStateOf<Int?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF8F9FAFF)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Интелектуальный ввод",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TsuBlue
        )

        Text(
            text = "Поставьте оценку от 0 до 9",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TsuBlue
        )

        Text(
            text = "Рисуйте крупно в центре рамки",
            fontSize = 16.sp,
            color = TsuLightBlue
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(320.dp)
                .shadow(10.dp, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFFDFE6E9))
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (offset.x > 1f && offset.y > 1f) {
                                lastX = offset.x
                                lastY = offset.y
                                Draw(offset.x, offset.y, gridSize, grid, size.width)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            val x = change.position.x
                            val y = change.position.y
                            change.consume()
                            if (x > 1f && y > 1f && lastX != -1f) {
                                addDraw(
                                    lastX, x, lastY, y,
                                    gridSize, grid, size.width
                                )
                                lastX = x
                                lastY = y
                            }
                        },
                        onDragEnd = {
                            lastX = -1f
                            lastY = -1f
                            score = digitRecognizer.claccify(grid)
                        })
                }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / gridSize
                for (i in 0 until gridSize) {
                    val p = i * cellSize
                    drawLine(
                        Color(0xFFF1F2F6),
                        Offset(p, 0f),
                        Offset(p, size.height),
                        1f
                    )
                    drawLine(
                        Color(0xFFF1F2F6),
                        Offset(0f, p),
                        Offset(size.width, p),
                        1f
                    )
                }

                for (i in 0 until gridSize) {
                    for (j in 0 until gridSize) {
                        if (grid[i][j] == 1) {
                            drawRect(
                                color = TsuBlue,
                                topLeft = Offset(i * cellSize, j * cellSize),
                                size = Size(cellSize + 0.5f, cellSize + 0.5f)
                            )
                        }
                    }
                }

//                val margin = size.width * 0.15f
//                drawRect(
//                    color = TsuBlue.copy(alpha =  0.3f),
//                    topLeft = Offset(margin, margin),
//                    size = Size(size.width - margin * 2, size.height - margin * 2),
//                    style = Stroke(
//                        width = 2.dp.toPx(),
//                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
//                    )
//                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(160.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "НЕЙРОСЕТЬ ВИДИТ:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = score?.toString() ?: "?",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = TsuBlue,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                score = null
                for (x in 0 until gridSize) {
                    for (y in 0 until gridSize) {
                        grid[x][y] = 0
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TsuBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(55.dp)
        ) {
            Text(
                "ОЧИСТИТЬ ХОЛСТ",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

