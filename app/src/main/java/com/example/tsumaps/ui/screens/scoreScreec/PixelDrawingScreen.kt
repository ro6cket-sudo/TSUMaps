package com.example.tsumaps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.ui.screens.scoreScreec.Draw
import com.example.tsumaps.ui.screens.scoreScreec.addDraw
import com.example.tsumaps.ui.theme.TsuBlue

@Composable
fun PixelDrawingScreen() {
    val gridSize = 50
    val context = LocalContext.current
    val digitRecognizer = remember { DigitRecognizer(context) }
    val grid = remember {
        mutableStateListOf<MutableList<Int>>().apply {
            repeat(gridSize){
                add(mutableStateListOf<Int>().apply { repeat(gridSize) { add(0) } })
            }
        }
    }

    var lastX by remember { mutableStateOf(-1f) }
    var lastY by remember { mutableStateOf(-1f) }
    var score by remember { mutableStateOf<Int?>(null) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Поставьте оценку от 0 до 9", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color.LightGray)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            lastX = offset.x
                            lastY = offset.y
                            Draw(offset.x, offset.y, gridSize, grid, size.width)
                        },
                        onDrag =  { change, dragAmount ->
                            val x = change.position.x
                            val y = change.position.y
                            addDraw(lastX, x, lastY, y, gridSize, grid, size.width)
                            lastX = x
                            lastY = y
                        },
                        onDragEnd = {
                            score = digitRecognizer.classify(grid)
                        })
                }){
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / gridSize
                for (i in 0 until gridSize){
                    for (j in 0 until gridSize){
                        if (grid[i][j] == 1){
                            drawRect(
                                color = TsuBlue,
                                topLeft = Offset(i*cellSize, j*cellSize),
                                size = Size(cellSize + 1f, cellSize + 1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (score != null) "Ваша оценка: $score" else "Нарисуйте цифру...",
            fontSize = 24.sp,
            color = TsuBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    for (x in 0 until gridSize) {
                        for (y in 0 until gridSize) {
                            grid[x][y] = 0
                        }
                    }
                }
            ) {
                Text("Очистить")
            }
        }
    }
}

